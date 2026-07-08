import re

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "r") as f:
    content = f.read()

# Replace the direct predictReq execute with a retry loop
replacement = """
        var predictRes: okhttp3.Response? = null
        var retryCount = 0
        while (retryCount < 10) {
            val req = Request.Builder()
                .url("$baseUrl/gradio_api/call/process")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            predictRes = client.newCall(req).execute()
            if (predictRes.isSuccessful) {
                break
            } else if (predictRes.code == 503) {
                SystemDiagnosticTracker.addLog("WHISPERX", "السيرفر في وضع النوم (503). جاري إيقاظه... محاولة ${retryCount + 1}/10")
                onProgress("السيرفر نائم، جاري إيقاظه... يرجى الانتظار")
                kotlinx.coroutines.delay(10000) // Wait 10 seconds before retrying
                retryCount++
                predictRes.close()
            } else {
                SystemDiagnosticTracker.addLog("WHISPERX", "فشل الاتصال بالسيرفر: ${predictRes.code}")
                throw Exception("Predict API failed: ${predictRes.code}")
            }
        }
        
        if (predictRes == null || !predictRes.isSuccessful) {
            throw Exception("Predict API failed after retries (Server might be down)")
        }
"""

content = content.replace("""        val predictReq = Request.Builder()
            .url("$baseUrl/gradio_api/call/process")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val predictRes = client.newCall(predictReq).execute()
        if (!predictRes.isSuccessful) {
            SystemDiagnosticTracker.addLog("WHISPERX", "فشل الاتصال بالسيرفر: ${predictRes.code}")
            throw Exception("Predict API failed: ${predictRes.code}")
        }""", replacement)

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "w") as f:
    f.write(content)

