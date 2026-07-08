import re
with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "r") as f:
    content = f.read()

download_target = """                    response.body?.byteStream()?.use { input ->
                        tmpFile.outputStream().use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } >= 0) {
                                checkCancellationAndPause()
                                output.write(buffer, 0, bytesRead)
                            }
                        }
                    }"""

download_replacement = """                    val totalBytes = response.body?.contentLength() ?: -1L
                    response.body?.byteStream()?.use { input ->
                        tmpFile.outputStream().use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var downloadedBytes = 0L
                            var lastProgress = 0
                            while (input.read(buffer).also { bytesRead = it } >= 0) {
                                checkCancellationAndPause()
                                output.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead
                                if (totalBytes > 0) {
                                    val progress = ((downloadedBytes.toFloat() / totalBytes) * 100).toInt()
                                    if (progress - lastProgress >= 5 || progress == 100) {
                                        SystemDiagnosticTracker.addLog("DOWNLOAD", "تنزيل الملف: $progress%")
                                        lastProgress = progress
                                    }
                                }
                            }
                        }
                    }"""

content = content.replace(download_target, download_replacement)

with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "w") as f:
    f.write(content)
