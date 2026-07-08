import re
with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "r") as f:
    content = f.read()

upload_progress_class = """
import okio.BufferedSink
import okio.source

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val onProgress: (Int) -> Unit
) : RequestBody() {
    override fun contentType() = contentType
    override fun contentLength() = file.length()
    override fun writeTo(sink: BufferedSink) {
        val length = file.length()
        val buffer = ByteArray(8192)
        var uploaded = 0L
        file.inputStream().use { input ->
            var read: Int
            var lastProgress = 0
            while (input.read(buffer).also { read = it } != -1) {
                uploaded += read
                sink.write(buffer, 0, read)
                val progress = ((uploaded.toFloat() / length) * 100).toInt()
                if (progress - lastProgress >= 5 || progress == 100) {
                    onProgress(progress)
                    lastProgress = progress
                }
            }
        }
    }
}
"""

if "ProgressRequestBody" not in content:
    content = content.replace("class WhisperXClient {", upload_progress_class + "\nclass WhisperXClient {")

upload_target = """                .addFormDataPart(
                    "files",
                    file.name,
                    file.asRequestBody("audio/*".toMediaType())
                )"""

upload_replacement = """                .addFormDataPart(
                    "files",
                    file.name,
                    ProgressRequestBody(file, "audio/*".toMediaType()) { percent ->
                        onProgress("جاري رفع الملف الصوتي للخادم... $percent%")
                        SystemDiagnosticTracker.addLog("UPLOAD", "رفع الملف الصوتي: $percent%")
                    }
                )"""

content = content.replace(upload_target, upload_replacement)

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "w") as f:
    f.write(content)
