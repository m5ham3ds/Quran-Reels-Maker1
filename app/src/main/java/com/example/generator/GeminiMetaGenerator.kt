package com.example.generator

import android.content.Context
import com.example.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PlatformMeta(val title: String, val description: String, val hashtags: String)

data class GeneratedMetaResult(
    val tiktok: PlatformMeta? = null,
    val instagram: PlatformMeta? = null,
    val facebook: PlatformMeta? = null,
    val youtube: PlatformMeta? = null
)

data class ClipAnalysisResult(
    val relevance: Float,
    val analysis: String,
    val error: String? = null,
    val surah: Int = 1,
    val startAyah: Int = 1,
    val endAyah: Int = 1,
    val reciterName: String = "",
    val title: String = "",
    val category: String = ""
)

class GeminiMetaGenerator {

    private fun getCacheFile(context: Context, url: String): java.io.File {
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
        return java.io.File(context.cacheDir, "whisper_cache_$hash.json")
    }


    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateSocialMeta(
        context: Context,
        surahName: String,
        startAyah: Int,
        endAyah: Int,
        reciterName: String,
        isTiktok: Boolean,
        isInstagram: Boolean,
        isFacebook: Boolean,
        isYoutube: Boolean
    ): GeneratedMetaResult? {
        return null
    }
    
    suspend fun analyzeClipUrl(
        context: Context,
        videoUrl: String,
        skipWhisperX: Boolean = false
    ): ClipAnalysisResult? = withContext(Dispatchers.IO) {
        val settingsManager = SettingsManager(context)
        val apiKey = settingsManager.geminiApiKey.first()
        var geminiModel = settingsManager.geminiModel.first().ifBlank { "gemini-1.5-pro" }
        
        if (apiKey.isBlank()) {
            return@withContext ClipAnalysisResult(0f, "", "Gemini API key is missing")
        }

        var transcription = ""
        var whisperError = ""
        var videoInfo = ""
        
        val cacheFile = getCacheFile(context, videoUrl)
        if (skipWhisperX) {
            SystemDiagnosticTracker.addLog("GEMINI", "ميزة تخطي WhisperX مفعلة. جاري فحص وجود معلومات مخزنة مسبقاً...")
            if (cacheFile.exists()) {
                try {
                    val cachedJson = JSONObject(cacheFile.readText())
                    transcription = cachedJson.optString("transcription", "")
                    videoInfo = cachedJson.optString("videoInfo", "")
                    SystemDiagnosticTracker.addLog("GEMINI", "تم العثور على معلومات مخزنة! تخطي المعالجة الصوتية.")
                } catch (e: Exception) {
                    return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "بيانات مخزنة تالفة")
                }
            } else {
                SystemDiagnosticTracker.addLog("GEMINI", "❌ لم يتم العثور على أي معلومات مخزنة لهذا الرابط. إيقاف العملية.")
                return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "ليست هناك اي معلومات لهذا الرابط. يرجى إيقاف ميزة التخطي وإعادة المحاولة.")
            }
        } else {
            SystemDiagnosticTracker.addLog("SYSTEM", "جاري تحويل الرابط إلى Space لمعالجة الصوت واستخراج النصوص: $videoUrl")
            try {
                val whisperClient = WhisperXClient()
                val result = whisperClient.processAudio(null, videoUrl, "") { progress ->
                    SystemDiagnosticTracker.addLog("WHISPER", progress)
                }
                
                videoInfo = result.videoInfo
                
                if (result.chunksJson.isNotBlank() && result.chunksJson != "[]") {
                    val chunksArray = JSONArray(result.chunksJson)
                    val textBuilder = java.lang.StringBuilder()
                    for (i in 0 until chunksArray.length()) {
                        val obj = chunksArray.getJSONObject(i)
                        textBuilder.append(obj.optString("text", "")).append(" ")
                    }
                    transcription = textBuilder.toString().trim()
                    SystemDiagnosticTracker.addLog("WHISPER", "نجح استخراج النص: ${transcription.take(50)}...")
                }
                
                // Save to cache
                val cacheObj = JSONObject().apply {
                    put("transcription", transcription)
                    put("videoInfo", videoInfo)
                }
                cacheFile.writeText(cacheObj.toString())
                SystemDiagnosticTracker.addLog("GEMINI", "تم حفظ معلومات الرابط في الذاكرة المؤقتة لاستخدامها لاحقاً.")
            } catch (e: Exception) {
                whisperError = e.message ?: "Unknown"
                SystemDiagnosticTracker.addLog("WHISPER", "فشل استخراج النصوص: ${e.message}")
            }
        }

        SystemDiagnosticTracker.addLog("GEMINI", "تم جلب المعلومات بنجاح وإعدادها. جاري الانتقال إلى إرسال المعلومات والبرومبت الاحترافي إلى نموذج ذكاء اصطناعي Gemini...")
        
        val prompt = """
            أنت خبير في التعرف على تلاوات القرآن الكريم.
            لدينا مقطع فيديو/صوت بهذا الرابط: $videoUrl
            والنص المستخرج منه (إن وجد): "$transcription"
            وبعض البيانات الوصفية من الفيديو (العنوان، الوصف، الكلمات المفتاحية):
            $videoInfo
            ملاحظة (إن وجدت مشكلة في جلب النص): $whisperError
            
            يرجى تحليل النص المستخرج (أو الاعتماد على الرابط والبيانات الوصفية) لاستخراج المعلومات التالية:
            1. رقم السورة (1 إلى 114).
            2. رقم آية البداية.
            3. رقم آية النهاية.
            4. اسم القارئ (مثل: مشاري العفاسي، عبدالباسط عبدالصمد... إذا لم تكن متأكدا اكتب "غير معروف"). ابحث جيداً في العنوان أو الوصف أو الكلمات المفتاحية.
            5. عنوان مناسب للمقطع (مثل: تلاوة خاشعة بصوت...).
            6. التصنيف الروحي (اختر واحدًا من: طمأنينة، خشوع، سكينة، دعاء).
            
            إذا لم تتمكن من تحديد السورة والآيات، افترض سورة الفاتحة (1) والآيات 1 إلى 5.
            
            يجب أن يكون الرد حصرياً بصيغة JSON بالتنسيق التالي بدون أي نصوص إضافية:
            {
                "surah": 1,
                "startAyah": 1,
                "endAyah": 5,
                "reciterName": "اسم القارئ",
                "title": "عنوان المقطع",
                "category": "خشوع"
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("response_mime_type", "application/json")
                put("temperature", 0.2)
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${geminiModel.trim()}:generateContent?key=${apiKey.trim()}"
        val request = Request.Builder()
            .url(url)
            .header("x-goog-api-key", apiKey.trim())
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseStr = response.body?.string() ?: ""
                val rootJson = JSONObject(responseStr)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.getJSONObject("content")
                    val parts = contentObj.getJSONArray("parts")
                    if (parts.length() > 0) {
                        var rawText = parts.getJSONObject(0).getString("text").trim()
                        SystemDiagnosticTracker.addLog("GEMINI", "Raw Gemini Response: $rawText")
                        
                        if (rawText.startsWith("```json")) {
                            rawText = rawText.substringAfter("```json").substringBeforeLast("```").trim()
                        } else if (rawText.startsWith("```")) {
                            rawText = rawText.substringAfter("```").substringBeforeLast("```").trim()
                        }
                        
                        try {
                            val jsonOutput = JSONObject(rawText)
                            return@withContext ClipAnalysisResult(
                                relevance = 1.0f,
                                analysis = "OK",
                                surah = jsonOutput.optInt("surah", 1),
                                startAyah = jsonOutput.optInt("startAyah", 1),
                                endAyah = jsonOutput.optInt("endAyah", 5),
                                reciterName = jsonOutput.optString("reciterName", "غير معروف"),
                                title = jsonOutput.optString("title", "تلاوة خاشعة"),
                                category = jsonOutput.optString("category", "سكينة")
                            )
                        } catch (e: Exception) {
                            SystemDiagnosticTracker.addLog("GEMINI", "❌ خطأ في تحليل استجابة Gemini: ${e.message}")
                            return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "فشل تحليل معلومات المقطع من الذكاء الاصطناعي")
                        }
                    }
                }
            } else {
                val errorBody = response.body?.string() ?: ""
                SystemDiagnosticTracker.addLog("GEMINI", "HTTP Error ${response.code}: $errorBody")
                return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "حدث خطأ في الاستجابة: ${response.code} - ${if (response.code == 503) "النموذج يواجه ضغطاً كبيراً، يرجى المحاولة لاحقاً أو تغيير النموذج" else "غير معروف"}")
            }
        } catch (e: Exception) {
            SystemDiagnosticTracker.addLog("GEMINI", "Error calling Gemini: ${e.message}")
            e.printStackTrace()
            return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "خطأ في الاتصال بالذكاء الاصطناعي: ${e.message}")
        }
        
        return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "لم يتم الحصول على أي معلومات")
    }
}
