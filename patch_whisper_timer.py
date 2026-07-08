import re

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "r") as f:
    content = f.read()

target = """        SystemDiagnosticTracker.addLog("WHISPERX", "جاري تتبع حالة العملية في السيرفر...")
        client.newCall(streamReq).execute().use { streamRes ->
            val source = streamRes.body?.source()
            while (source != null && !source.exhausted()) {"""

replacement = """        SystemDiagnosticTracker.addLog("WHISPERX", "جاري تتبع حالة العملية في السيرفر...")
        
        var isStreamActive = true
        val progressJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            var seconds = 0
            while(isStreamActive) {
                kotlinx.coroutines.delay(5000)
                seconds += 5
                if(isStreamActive) {
                    SystemDiagnosticTracker.addLog("WHISPERX", "المعالجة مستمرة في السيرفر... ($seconds ثانية)")
                    onProgress("المعالجة مستمرة في السيرفر... ($seconds ثانية)")
                }
            }
        }
        
        try {
            client.newCall(streamReq).execute().use { streamRes ->
                val source = streamRes.body?.source()
                while (source != null && !source.exhausted()) {"""
content = content.replace(target, replacement)

target2 = """                } else if (line.startsWith("event: error")) {
                    val dataLine = source.readUtf8Line()
                    throw Exception("Server Error: $dataLine")
                }
            }
        }"""

replacement2 = """                } else if (line.startsWith("event: error")) {
                    val dataLine = source.readUtf8Line()
                    throw Exception("Server Error: $dataLine")
                }
            }
        }
        } finally {
            isStreamActive = false
            progressJob.cancel()
        }"""
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "w") as f:
    f.write(content)

