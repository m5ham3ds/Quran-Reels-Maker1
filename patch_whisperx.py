import re

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "r") as f:
    content = f.read()

target = """        onProgress("جاري معالجة الصوت والنص...")
        val payload = JSONObject().apply {"""

replacement = """        SystemDiagnosticTracker.addLog("WHISPERX", "جاري إرسال الطلب إلى السيرفر المضيف (Space) للبدء...")
        onProgress("جاري إرسال الطلب إلى السيرفر المضيف للبدء...")
        val payload = JSONObject().apply {"""
content = content.replace(target, replacement)

target2 = """        val predictRes = client.newCall(predictReq).execute()
        if (!predictRes.isSuccessful) throw Exception("Predict API failed: ${predictRes.code}")
        val predictBody = predictRes.body?.string() ?: ""
        val eventId = JSONObject(predictBody).getString("event_id")

        val streamReq = Request.Builder()
            .url("$baseUrl/gradio_api/call/process/$eventId")
            .get()
            .build()"""

replacement2 = """        val predictRes = client.newCall(predictReq).execute()
        if (!predictRes.isSuccessful) {
            SystemDiagnosticTracker.addLog("WHISPERX", "فشل الاتصال بالسيرفر: ${predictRes.code}")
            throw Exception("Predict API failed: ${predictRes.code}")
        }
        val predictBody = predictRes.body?.string() ?: ""
        val eventId = JSONObject(predictBody).getString("event_id")
        
        SystemDiagnosticTracker.addLog("WHISPERX", "نجح الاتصال بالسيرفر (EventID: $eventId). بدأ عملية الاستخراج والمعالجة...")
        onProgress("بدأ عملية الاستخراج والمعالجة في السيرفر...")

        val streamReq = Request.Builder()
            .url("$baseUrl/gradio_api/call/process/$eventId")
            .get()
            .build()"""
content = content.replace(target2, replacement2)

target3 = """        client.newCall(streamReq).execute().use { streamRes ->
            val source = streamRes.body?.source()
            while (source != null && !source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("event: complete")) {"""

replacement3 = """        SystemDiagnosticTracker.addLog("WHISPERX", "جاري تتبع حالة العملية في السيرفر...")
        client.newCall(streamReq).execute().use { streamRes ->
            val source = streamRes.body?.source()
            while (source != null && !source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("event: generating") || line.startsWith("event: update")) {
                    val dataLine = source.readUtf8Line() ?: ""
                    // Avoid spamming too much, but log if needed
                    SystemDiagnosticTracker.addLog("WHISPERX", "تحديث من السيرفر: $line")
                } else if (line.startsWith("event: complete")) {
                    SystemDiagnosticTracker.addLog("WHISPERX", "تم استلام النتائج النهائية من السيرفر. جاري تحليل البيانات...")
                    onProgress("تم استلام النتائج النهائية من السيرفر.")"""
content = content.replace(target3, replacement3)

with open("app/src/main/java/com/example/generator/WhisperXClient.kt", "w") as f:
    f.write(content)

