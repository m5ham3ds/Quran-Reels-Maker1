import re

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "r") as f:
    content = f.read()

target = """                                val logMsg = array.optString(0, "")
                                if (logMsg.isNotBlank()) {
                                    SystemDiagnosticTracker.addLog("WHISPERX_SPACE", logMsg)
                                    onProgress(logMsg)
                                }"""

replacement = """                                val logMsg = array.optString(0, "")
                                if (logMsg.isNotBlank()) {
                                    if (logMsg.length > 100) {
                                        SystemDiagnosticTracker.addLog("WHISPERX_SPACE", "تحديث: جاري استخراج ومعالجة الصوت والنص... (بيانات متقدمة)")
                                        onProgress("تحديث: جاري استخراج ومعالجة الصوت والنص...")
                                    } else {
                                        SystemDiagnosticTracker.addLog("WHISPERX_SPACE", logMsg)
                                        onProgress(logMsg)
                                    }
                                }"""
content = content.replace(target, replacement)

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "w") as f:
    f.write(content)
