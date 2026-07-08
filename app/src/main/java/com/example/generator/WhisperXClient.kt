package com.example.generator

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch


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

class WhisperXClient {
    companion object {
        private val cache = mutableMapOf<String, Pair<Long, ProcessResult>>()
        
        fun getCachedResult(url: String): ProcessResult? {
            val cached = cache[url]
            if (cached != null) {
                // Check if it is less than 8 minutes old (480,000 ms)
                if (System.currentTimeMillis() - cached.first < 480_000L) {
                    return cached.second
                } else {
                    cache.remove(url)
                }
            }
            return null
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .header("Referer", "https://qalam249-whisperx-frontend.hf.space/")
                .method(original.method, original.body)
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val baseUrl = "https://qalam249-whisperx-frontend.hf.space"

    suspend fun processAudio(
        file: File?,
        urlInput: String,
        arabicText: String,
        onProgress: (String) -> Unit
    ): ProcessResult = withContext(Dispatchers.IO) {
        if (urlInput.isNotBlank()) {
            val cachedResult = getCachedResult(urlInput)
            if (cachedResult != null) {
                SystemDiagnosticTracker.addLog("WHISPERX", "تم العثور على معلومات مسبقة في الذاكرة لتوفير الوقت.")
                onProgress("تم استرجاع معلومات المعالجة من الذاكرة المؤقتة...")
                return@withContext cachedResult
            }
        }
        
        var fileDataObj: JSONObject? = null
        if (file != null && file.exists()) {
            onProgress("جاري رفع الملف الصوتي للخادم...")
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "files",
                    file.name,
                    ProgressRequestBody(file, "audio/*".toMediaType()) { percent ->
                        onProgress("جاري رفع الملف الصوتي للخادم... $percent%")
                        SystemDiagnosticTracker.addLog("UPLOAD", "رفع الملف الصوتي: $percent%")
                    }
                )
                .build()
            val request = Request.Builder()
                .url("$baseUrl/gradio_api/upload")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to upload file: ${response.code}")
            val respStr = response.body?.string() ?: "[]"
            val jsonArray = JSONArray(respStr)
            if (jsonArray.length() > 0) {
                val serverPath = jsonArray.getString(0)
                fileDataObj = JSONObject().apply {
                    put("path", serverPath)
                    put("meta", JSONObject().put("_type", "gradio.FileData"))
                }
            }
        }

        SystemDiagnosticTracker.addLog("WHISPERX", "جاري إرسال الطلب إلى السيرفر المضيف (Space) للبدء...")
        onProgress("جاري إرسال الطلب إلى السيرفر المضيف للبدء...")
        val payload = JSONObject().apply {
            val dataArray = JSONArray()
            dataArray.put(fileDataObj ?: JSONObject.NULL)
            dataArray.put(urlInput)
            dataArray.put(arabicText)
            put("data", dataArray)
        }

        val predictReq = Request.Builder()
            .url("$baseUrl/gradio_api/call/process")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val predictRes = client.newCall(predictReq).execute()
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
            .build()

        var chunksJson = ""
        var outAudioUrl = ""
        var errorLog = ""
        var videoInfo = ""

        SystemDiagnosticTracker.addLog("WHISPERX", "جاري تتبع حالة العملية في السيرفر...")
        
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
                while (source != null && !source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("event: generating") || line.startsWith("event: update")) {
                    val dataLine = source.readUtf8Line() ?: ""
                    if (dataLine.startsWith("data:")) {
                        try {
                            val dataJson = dataLine.substring(5).trim()
                            val array = JSONArray(dataJson)
                            if (array.length() > 0) {
                                // Gradio often outputs lists of values or single strings for logs
                                val logMsg = array.optString(0, "")
                                if (logMsg.isNotBlank()) {
                                    if (logMsg.length > 100) {
                                        SystemDiagnosticTracker.addLog("WHISPERX_SPACE", "تحديث: جاري استخراج ومعالجة الصوت والنص... (بيانات متقدمة)")
                                        onProgress("تحديث: جاري استخراج ومعالجة الصوت والنص...")
                                    } else {
                                        SystemDiagnosticTracker.addLog("WHISPERX_SPACE", logMsg)
                                        onProgress(logMsg)
                                    }
                                }
                            }
                        } catch(e: Exception) {
                            SystemDiagnosticTracker.addLog("WHISPERX", "تحديث: المعالجة مستمرة...")
                            onProgress("المعالجة مستمرة في السيرفر...")
                        }
                    }
                } else if (line.startsWith("event: complete")) {
                    SystemDiagnosticTracker.addLog("WHISPERX", "تم استلام النتائج النهائية من السيرفر. جاري تحليل البيانات...")
                    onProgress("تم استلام النتائج النهائية من السيرفر.")
                    val dataLine = source.readUtf8Line()
                    if (dataLine != null && dataLine.startsWith("data:")) {
                        val dataJson = dataLine.substring(5).trim()
                        val dataArray = JSONArray(dataJson)
                        if (dataArray.length() >= 8) {
                            chunksJson = dataArray.optString(4, "[]")
                            val audioOpt = dataArray.opt(6)
                            if (audioOpt is JSONObject) {
                                if (audioOpt.has("url")) {
                                    outAudioUrl = audioOpt.getString("url")
                                    if (outAudioUrl.startsWith("/")) {
                                        outAudioUrl = "$baseUrl$outAudioUrl"
                                    }
                                } else if (audioOpt.has("path")) {
                                    outAudioUrl = "$baseUrl/file=${audioOpt.getString("path")}"
                                }
                            } else if (audioOpt is String && audioOpt.isNotBlank()) {
                                outAudioUrl = if (audioOpt.startsWith("http")) audioOpt else "$baseUrl/file=$audioOpt"
                            }
                            errorLog = dataArray.optString(7, "")
                            videoInfo = dataArray.optString(1, "")
                        }
                    }
                } else if (line.startsWith("event: error")) {
                    val dataLine = source.readUtf8Line()
                    throw Exception("Server Error: $dataLine")
                }
            }
        }
        } finally {
            isStreamActive = false
            progressJob.cancel()
        }
        
        if (errorLog.contains("❌") || errorLog.contains("خطأ") || chunksJson.isBlank()) {
            if (errorLog.isNotBlank()) {
                throw Exception(errorLog)
            } else {
                throw Exception("فشلت عملية الموائمة عبر WhisperX")
            }
        }

        val finalResult = ProcessResult(chunksJson, outAudioUrl, errorLog, videoInfo)
        if (urlInput.isNotBlank()) {
            cache[urlInput] = Pair(System.currentTimeMillis(), finalResult)
        }
        finalResult
    }
}

data class ProcessResult(
    val chunksJson: String,
    val audioUrl: String,
    val errorLog: String,
    val videoInfo: String = ""
)
