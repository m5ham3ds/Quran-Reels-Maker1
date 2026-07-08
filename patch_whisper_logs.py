import re

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "r") as f:
    content = f.read()

target = """                if (line.startsWith("event: generating") || line.startsWith("event: update")) {
                    val dataLine = source.readUtf8Line() ?: ""
                    // Avoid spamming too much, but log if needed
                    SystemDiagnosticTracker.addLog("WHISPERX", "تحديث من السيرفر: $line")"""

replacement = """                if (line.startsWith("event: generating") || line.startsWith("event: update")) {
                    val dataLine = source.readUtf8Line() ?: ""
                    if (dataLine.startsWith("data:")) {
                        try {
                            val dataJson = dataLine.substring(5).trim()
                            val array = JSONArray(dataJson)
                            if (array.length() > 0) {
                                // Gradio often outputs lists of values or single strings for logs
                                val logMsg = array.optString(0, "")
                                if (logMsg.isNotBlank()) {
                                    SystemDiagnosticTracker.addLog("WHISPERX_SPACE", logMsg)
                                    onProgress(logMsg)
                                }
                            }
                        } catch(e: Exception) {
                            SystemDiagnosticTracker.addLog("WHISPERX", "تحديث: المعالجة مستمرة...")
                            onProgress("المعالجة مستمرة في السيرفر...")
                        }
                    }"""
content = content.replace(target, replacement)

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "w") as f:
    f.write(content)
