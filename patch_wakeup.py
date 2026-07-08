import re

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "r") as f:
    content = f.read()

# Add wake up space
wakeup = """
    private suspend fun wakeUpSpace() {
        var retryCount = 0
        while (retryCount < 5) {
            try {
                val req = Request.Builder().url(baseUrl).get().build()
                val res = client.newCall(req).execute()
                val code = res.code
                res.close()
                if (code == 200 || code == 405 || code == 404) {
                    // It's awake
                    return
                }
                if (code == 503 || code == 504) {
                    SystemDiagnosticTracker.addLog("WHISPERX", "السيرفر نائم (503)، جاري إيقاظه...")
                    kotlinx.coroutines.delay(10000)
                    retryCount++
                } else {
                    return // unexpected code, but let it proceed
                }
            } catch (e: Exception) {
                SystemDiagnosticTracker.addLog("WHISPERX", "خطأ أثناء محاولة إيقاظ السيرفر: ${e.message}")
                kotlinx.coroutines.delay(5000)
                retryCount++
            }
        }
    }

    suspend fun processAudio(
"""

content = content.replace("    suspend fun processAudio(", wakeup)

process_audio = """    suspend fun processAudio(
        file: java.io.File?,
        urlInput: String,
        arabicText: String,
        onProgress: (String) -> Unit
    ): ProcessResult = kotlinx.coroutines.withContext(Dispatchers.IO) {
        
        onProgress("جاري التحقق من حالة السيرفر...")
        wakeUpSpace()
"""

content = re.sub(r'    suspend fun processAudio\([\s\S]*?\): ProcessResult = kotlinx\.coroutines\.withContext\(Dispatchers\.IO\) \{', process_audio, content)


with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "w") as f:
    f.write(content)
