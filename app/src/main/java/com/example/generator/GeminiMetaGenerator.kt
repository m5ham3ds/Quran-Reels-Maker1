package com.example.generator

import android.content.Context
import com.example.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class PlatformMeta(
    val title: String,
    val description: String,
    val hashtags: String
)

data class GeneratedMetaResult(
    val tiktok: PlatformMeta?,
    val instagram: PlatformMeta?,
    val facebook: PlatformMeta?,
    val youtube: PlatformMeta?
)

data class ClipAnalysisResult(
    val surah: Int,
    val startAyah: Int,
    val endAyah: Int,
    val reciterName: String,
    val title: String = "",
    val videoQuery: String = "",
    val category: String = ""
)

class GeminiMetaGenerator {
    private val client = OkHttpClient.Builder()
        // API Timeout to prevent cut-off
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .build()
            chain.proceed(request)
        }
        .build()

    suspend fun analyzeClipUrl(context: Context, url: String): ClipAnalysisResult? = withContext(Dispatchers.IO) {
        val settingsManager = SettingsManager(context)
        var apiKey = settingsManager.geminiApiKey.first()
        val geminiModel = settingsManager.geminiModel.first()
        
        if (apiKey.isBlank()) {
            apiKey = com.example.BuildConfig.GEMINI_API_KEY
        }
        
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            SystemDiagnosticTracker.addLog("GEMINI", "Error: Gemini API Key is missing.")
            return@withContext null
        }

        SystemDiagnosticTracker.addLog("GEMINI", "بدء تحليل الرابط: $url")
        SystemDiagnosticTracker.addLog("GEMINI", "جاري الاتصال بـ WhisperX Frontend لاستخراج النص...")

        // 1. Call WhisperX to get transcription text to assist Gemini
        var whisperText = ""
        try {
            val alignPayload = JSONObject().apply {
                put("data", JSONArray().apply {
                    put(JSONObject.NULL) // file
                    put(url)             // url
                    put("")              // text
                })
            }
            
            val jsonMediaType = "application/json".toMediaType()
            val alignRequest = Request.Builder()
                .url("https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process")
                .post(alignPayload.toString().toRequestBody(jsonMediaType))
                .build()

            val alignResponse = client.newCall(alignRequest).execute()
            if (alignResponse.isSuccessful) {
                val alignResponseBody = alignResponse.body?.string() ?: ""
                val eventIdJson = JSONObject(alignResponseBody)
                val eventId = eventIdJson.optString("event_id")
                
                if (eventId.isNotEmpty()) {
                    SystemDiagnosticTracker.addLog("GEMINI", "تم الاتصال بنجاح. معرف العملية (Event ID): $eventId")
                    val eventRequest = Request.Builder()
                        .url("https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process/$eventId")
                        .get()
                        .build()

                    var completedData: String? = null
                    var attempt = 0
                    SystemDiagnosticTracker.addLog("GEMINI", "جاري الاستماع للنتائج من WhisperX...")
                    while (attempt < 15 && completedData == null) {
                        try {
                            val eventResponse = client.newCall(eventRequest).execute()
                            if (eventResponse.isSuccessful) {
                                val reader = eventResponse.body?.charStream()?.buffered()
                                var line: String?
                                if (reader != null) {
                                    while (reader.readLine().also { line = it } != null) {
                                        val currentLine = line ?: ""
                                        if (currentLine.startsWith("event: complete")) {
                                            val nextLine = reader.readLine() ?: ""
                                            if (nextLine.startsWith("data: ")) {
                                                completedData = nextLine.substring("data: ".length)
                                            }
                                        } else if (currentLine.startsWith("event: error")) {
                                            break
                                        }
                                    }
                                }
                            }
                            eventResponse.close()
                            if (completedData == null) {
                                attempt++
                                kotlinx.coroutines.delay(2000) // retry if stream was closed prematurely
                            }
                        } catch (e: Exception) {
                            attempt++
                            kotlinx.coroutines.delay(2000)
                        }
                    }
                    
                    if (completedData != null) {
                        val jsonArr = JSONArray(completedData)
                        var videoInfoText = ""
                        var textInfoText = ""
                        for (i in 0 until jsonArr.length()) {
                            val str = jsonArr.optString(i, "")
                            if (str.contains("المدة:") || str.contains("القناة:") || str.contains("الوصف:")) {
                                videoInfoText = str
                            } else if (str.contains("📄 النص الكامل:\n") || str.length > 50) {
                                textInfoText = str
                            }
                        }
                        
                        if (videoInfoText.isNotBlank() || textInfoText.isNotBlank()) {
                            val fullText = if (textInfoText.contains("📄 النص الكامل:\n")) {
                                textInfoText.substringAfter("📄 النص الكامل:\n").trim()
                            } else textInfoText
                            
                            whisperText = "معلومات الفيديو:\n$videoInfoText\n\nالنص المستخرج من الفيديو:\n$fullText"
                            SystemDiagnosticTracker.addLog("GEMINI", "تم استخراج النص والمعلومات من WhisperX بنجاح.")
                        } else {
                            SystemDiagnosticTracker.addLog("GEMINI", "لم يتم العثور على نص كافٍ في بيانات WhisperX المسترجعة.")
                        }
                    } else {
                         SystemDiagnosticTracker.addLog("GEMINI", "انتهت المهلة ولم يتم استلام بيانات مكتملة من WhisperX.")
                    }
                } else {
                    SystemDiagnosticTracker.addLog("GEMINI", "فشل في الحصول على Event ID من WhisperX.")
                }
            } else {
                SystemDiagnosticTracker.addLog("GEMINI", "فشل الاتصال بـ WhisperX: ${alignResponse.code}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SystemDiagnosticTracker.addLog("GEMINI", "خطأ أثناء الاتصال بـ WhisperX: ${e.message}")
        }

        val savedPrompt = settingsManager.geminiPrompt.first()
        val prompt = savedPrompt.replace("[URL]", url).replace("[WHISPER_TEXT]", whisperText)

        SystemDiagnosticTracker.addLog("GEMINI", "تجهيز الطلب وإرساله إلى نموذج الذكاء الاصطناعي: $geminiModel")
        
        val jsonRequest = JSONObject().apply {
            val countArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", countArray)
            
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/${geminiModel.trim()}:generateContent?key=${apiKey.trim()}"
        
        val request = Request.Builder()
            .url(apiUrl)
            .header("x-goog-api-key", apiKey.trim())
            .post(requestBody)
            .build()
            
        var attempt = 0
        val maxAttempts = 5
        while (attempt < maxAttempts) {
            try {
                SystemDiagnosticTracker.addLog("GEMINI", "محاولة الاتصال بـ Gemini رقم ${attempt + 1}")
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val rootJson = JSONObject(responseStr)
                    if (rootJson.has("error")) {
                        val errMsg = rootJson.getJSONObject("error").getString("message")
                        if (errMsg.contains("429") || errMsg.contains("quota") || errMsg.contains("rate limit")) {
                            if (attempt < maxAttempts - 1) {
                                attempt++
                                SystemDiagnosticTracker.addLog("GEMINI", "تحذير: استنفاد حصة (429). سيتم إعادة المحاولة...")
                                kotlinx.coroutines.delay(4000L * attempt)
                                continue
                            }
                        }
                        SystemDiagnosticTracker.addLog("GEMINI", "خطأ من سيرفر Gemini: $errMsg")
                        throw Exception(errMsg)
                    }
                    val candidates = rootJson.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val contentObj = candidate.getJSONObject("content")
                        val parts = contentObj.getJSONArray("parts")
                        if (parts.length() > 0) {
                            val rawText = parts.getJSONObject(0).getString("text").trim()
                            
                            SystemDiagnosticTracker.addLog("GEMINI", "تم استلام الرد من Gemini بنجاح. جاري استخراج الحقول...")
                            
                            var surahNum = 1
                            var startA = 1
                            var endA = 1
                            var reciter = "Unknown"
                            var title = ""
                            var category = "طمأنينة"

                            try {
                                val surahRegex = Regex("\\[SURAH\\](.*?)\\[/SURAH\\]", RegexOption.DOT_MATCHES_ALL)
                                val reciterRegex = Regex("\\[RECITER\\](.*?)\\[/RECITER\\]", RegexOption.DOT_MATCHES_ALL)
                                val startRegex = Regex("\\[START\\](.*?)\\[/START\\]", RegexOption.DOT_MATCHES_ALL)
                                val endRegex = Regex("\\[END\\](.*?)\\[/END\\]", RegexOption.DOT_MATCHES_ALL)
                                val titleRegex = Regex("\\[TITLE\\](.*?)\\[/TITLE\\]", RegexOption.DOT_MATCHES_ALL)
                                val categoryRegex = Regex("\\[CATEGORY\\](.*?)\\[/CATEGORY\\]", RegexOption.DOT_MATCHES_ALL)
                                
                                val surahMatch = surahRegex.find(rawText)?.groupValues?.get(1)?.trim()
                                if (surahMatch != null) {
                                    surahNum = surahMatch.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                                }
                                reciterRegex.find(rawText)?.groupValues?.get(1)?.trim()?.let { reciter = it }
                                startRegex.find(rawText)?.groupValues?.get(1)?.trim()?.replace(Regex("[^0-9]"), "")?.toIntOrNull()?.let { startA = it }
                                endRegex.find(rawText)?.groupValues?.get(1)?.trim()?.replace(Regex("[^0-9]"), "")?.toIntOrNull()?.let { endA = it }
                                titleRegex.find(rawText)?.groupValues?.get(1)?.trim()?.let { title = it }
                                categoryRegex.find(rawText)?.groupValues?.get(1)?.trim()?.let { category = it }

                            } catch (e: Exception) {
                                SystemDiagnosticTracker.addLog("GEMINI", "فشل في مطابقة نصوص الحقول من الرد.")
                                throw Exception("فشل في استخراج البيانات من الرد: ${e.message}")
                            }
                            
                            if (reciter.isBlank()) reciter = "Unknown"

                            SystemDiagnosticTracker.addLog("GEMINI", "نجاح! تم استخراج: سورة=$surahNum، آيات=$startA-$endA، قارئ=$reciter")
                            
                            return@withContext ClipAnalysisResult(
                                surah = surahNum,
                                startAyah = startA,
                                endAyah = endA,
                                reciterName = reciter,
                                title = title,
                                videoQuery = "",
                                category = category
                            )
                        } else {
                            SystemDiagnosticTracker.addLog("GEMINI", "لم يتم العثور على أجزاء نصية في الرد (parts فارغ).")
                        }
                    } else {
                        SystemDiagnosticTracker.addLog("GEMINI", "لم يتم العثور على نتائج في الرد (candidates فارغ).")
                    }
                } else if (response.code == 429) {
                    if (attempt < maxAttempts - 1) {
                        SystemDiagnosticTracker.addLog("GEMINI", "استنفاد الحد (429). محاولة ${attempt + 1}...")
                        attempt++
                        kotlinx.coroutines.delay(4000L * attempt)
                        continue
                    } else {
                        SystemDiagnosticTracker.addLog("GEMINI", "خطأ 429 دائم. توقف المحاولات.")
                        throw Exception("استنفذت الحد المسموح (خطأ 429). أضف مفتاح API الخاص بك في الإعدادات.")
                    }
                } else {
                    if (attempt < maxAttempts - 1 && response.code >= 500) {
                        SystemDiagnosticTracker.addLog("GEMINI", "خطأ سيرفر ${response.code}. محاولة ${attempt + 1}...")
                        attempt++
                        kotlinx.coroutines.delay(2000L * attempt)
                        continue
                    }
                    SystemDiagnosticTracker.addLog("GEMINI", "فشل الاتصال: رمز الاستجابة ${response.code}")
                    throw Exception("فشل الاتصال بـ Gemini API: HTTP ${response.code}\n${response.body?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e.message?.contains("429") == true || e.message?.contains("Too Many Requests") == true) {
                    if (attempt < maxAttempts - 1) {
                        SystemDiagnosticTracker.addLog("GEMINI", "استثناء 429. محاولة ${attempt + 1}...")
                        attempt++
                        kotlinx.coroutines.delay(4000L * attempt)
                        continue
                    }
                    SystemDiagnosticTracker.addLog("GEMINI", "خطأ 429. توقف المحاولات.")
                    throw Exception("لقد استنفذت الحد المسموح (خطأ 429). يرجى إضافة مفتاح API الخاص بك في الإعدادات لتخطي هذا الحد.")
                }
                
                if (attempt < maxAttempts - 1 && e is java.io.IOException) {
                    SystemDiagnosticTracker.addLog("GEMINI", "مشكلة اتصال (IOException). محاولة ${attempt + 1}...")
                    attempt++
                    kotlinx.coroutines.delay(2000L * attempt)
                    continue
                }
                SystemDiagnosticTracker.addLog("GEMINI", "خطأ استثناء: ${e.message}")
                throw Exception(e.message ?: "حدث خطأ غير معروف")
            }
            break
        }
        return@withContext null
    }

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
    ): GeneratedMetaResult? = withContext(Dispatchers.IO) {
        val settingsManager = SettingsManager(context)
        var apiKey = settingsManager.geminiApiKey.first()
        val geminiModel = settingsManager.geminiModel.first()
        
        // If empty in user settings, check if there is an injection in BuildConfig
        if (apiKey.isBlank()) {
            apiKey = com.example.BuildConfig.GEMINI_API_KEY
        }
        
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val prompt = """
            You are an expert AI social media content strategist and creative manager for elite, highly-viral Islamic religious video assets (e.g. Quran Reels, TikTok, YouTube Shorts, and Facebook Watch).
            Generate tailored engaging, highly spiritual and emotionally-moving titles, descriptions, and hashtags/tags in Arabic for sharing an Islamic video on social media.
            Video Context Details:
            - Quran Surah: $surahName
            - Ayah Range: $startAyah to $endAyah
            - Reciter Voice: $reciterName
            
            We are publishing this video reel to the following target platforms:
            TikTok: $isTiktok
            Instagram: $isInstagram
            Facebook: $isFacebook
            YouTube Shorts: $isYoutube
            
            CRITICAL RULES (FOLLOW STRICTLY):
            1. NEVER use the raw Surah Name or plain 'Surah $surahName' as the video title or start of the title! That is too generic and boring.
            2. Instead, craft highly emotional, spiritually-moving titles (in Arabic) that touch the human soul and drive deep curiosity, contemplation, and viewing retention. Mirror elite Islamic accounts on TikTok & Instagram Reels.
               Examples of high-performing, heart-melting spiritual hooks:
               - "تلاوة خاشعة تريح القلوب المتعبة ☕️🌿"
               - "أرح سمعك وقلبك المنهك بالهموم 🍃"
               - "سيهدأ روعك وتزول همومك بسماع هذه الآيات ✨"
               - "هدئ قلبك وعالج ضيق صدرك 🤲"
               - "تلاوة تأخذك لعالم آخر من السكينة والوقار 🌌"
            3. Build and customize the output for each platform individually to optimize for their respective search SEO filters and viewer behaviors:
               - TikTok: Focus on immediate emotional hooks, dynamic spacing, and highly viral religious hashtags like `#قران_كريم #تلاوة_خاشعة #راحة_نفسية #أرح_قلبك #foryou #قرآن #quran #دعاء`.
               - Instagram: Focus on elegant, clean layout, aesthetic style with high-status emojis (💎, ✨, 🌱), and search-friendly tags (`#reels #quran #راحة #اسلاميات #explore #تدبر`).
               - YouTube Shorts: Use short, punchy, high-click-through titles (under 60 characters) with relevant short tags (`#Shorts #قرآن #اسلام #راحة #يوتيوب`).
               - Facebook: Inspiring, family-friendly, peaceful tone, promoting values of community prayer and blessings (`#فيسبوك_إسلام #تلاوات_خاشعة #فيس_بوك_اسلامي`).
            
            Format your final response strictly as a single JSON object containing keys "tiktok", "instagram", "facebook", and "youtube" (only include key if platform is true).
            Each platform's value should be an object containing:
            1. "title": A catchy spiritually-moving title optimized for that platform based on the instructions.
            2. "description": A highly engaging description matching that platform's character limits & vibe, decorated with fitting emojis, encouraging viewers to listen and ponder.
            3. "hashtags": A space-separated list of highly professional, relevant, trending hashtags like #quran #قرآن #تلاوة_خاشعة #قران_كريم #reels #shorts etc.
            
            Respond with ONLY the raw JSON string, never surround it in markdown notation or code blocks.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            val countArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", countArray)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.75)
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${geminiModel.trim()}:generateContent?key=${apiKey.trim()}"
        
        val request = Request.Builder()
            .url(url)
            .header("x-goog-api-key", apiKey.trim())
            .post(requestBody)
            .build()
            
        var attempt = 0
        val maxAttempts = 5
        while (attempt < maxAttempts) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val rootJson = JSONObject(responseStr)
                    if (rootJson.has("error")) {
                        val errMsg = rootJson.getJSONObject("error").getString("message")
                        if (errMsg.contains("429") || errMsg.contains("quota") || errMsg.contains("rate limit")) {
                            if (attempt < maxAttempts - 1) {
                                attempt++
                                kotlinx.coroutines.delay(4000L * attempt)
                                continue
                            }
                        }
                    }
                    val candidates = rootJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val contentObj = candidate.getJSONObject("content")
                        val parts = contentObj.getJSONArray("parts")
                        if (parts.length() > 0) {
                            val rawText = parts.getJSONObject(0).getString("text").trim()
                            
                            // Parse JSON output from Gemini
                            val cleanText = if (rawText.startsWith("```json")) {
                                rawText.substringAfter("```json").substringBeforeLast("```").trim()
                            } else if (rawText.startsWith("```")) {
                                rawText.substringAfter("```").substringBeforeLast("```").trim()
                            } else {
                                rawText
                            }
                            
                            val metaJson = JSONObject(cleanText)
                            
                            val tiktokMeta = if (isTiktok && metaJson.has("tiktok")) {
                                val obj = metaJson.getJSONObject("tiktok")
                                PlatformMeta(obj.getString("title"), obj.getString("description"), obj.getString("hashtags"))
                            } else null
                            
                            val instagramMeta = if (isInstagram && metaJson.has("instagram")) {
                                val obj = metaJson.getJSONObject("instagram")
                                PlatformMeta(obj.getString("title"), obj.getString("description"), obj.getString("hashtags"))
                            } else null

                            val facebookMeta = if (isFacebook && metaJson.has("facebook")) {
                                val obj = metaJson.getJSONObject("facebook")
                                PlatformMeta(obj.getString("title"), obj.getString("description"), obj.getString("hashtags"))
                            } else null

                            val youtubeMeta = if (isYoutube && metaJson.has("youtube")) {
                                val obj = metaJson.getJSONObject("youtube")
                                PlatformMeta(obj.getString("title"), obj.getString("description"), obj.getString("hashtags"))
                            } else null
                            
                            return@withContext GeneratedMetaResult(tiktokMeta, instagramMeta, facebookMeta, youtubeMeta)
                        }
                    }
                } else if (response.code == 429) {
                    if (attempt < maxAttempts - 1) {
                        attempt++
                        kotlinx.coroutines.delay(4000L * attempt)
                        continue
                    }
                } else if (response.code >= 500) {
                    if (attempt < maxAttempts - 1) {
                        attempt++
                        kotlinx.coroutines.delay(2000L * attempt)
                        continue
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (attempt < maxAttempts - 1 && e is java.io.IOException) {
                    attempt++
                    kotlinx.coroutines.delay(2000L * attempt)
                    continue
                }
            }
            break
        }
        return@withContext null
    }
}
