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
            return@withContext null
        }

        val prompt = """
            اود منك استخراج بيانات متعلقة بالقرآن الكريم من الرابط التالي (إذا لم تستطع استخراجها حقيقياً، قم بتخمين منطقي بناءً على اسم الرابط أو اختلق قيم متوقعة كقالب).
            يجب أن تضع كل معلومة داخل الرموز المحددة بالضبط (بين الرمز والرمز المغلق) كما هو موضح أدناه لكي أتمكن من استخراجها برمجياً:
            
            [SURAH]هنا ضع رقم السورة فقط كـرقم (مثل 1 للفاتحة، 2 للبقرة)[/SURAH]
            [RECITER]هنا اسم القارئ او الشيخ[/RECITER]
            [START]هنا رقم آية البداية (رقم فقط)[/START]
            [END]هنا رقم الاية النهائية (رقم فقط)[/END]
            [TITLE]هنا العنوان المناسب للمقطع (بدون كلمات مثل سورة، فقط عنوان مرئي جذاب)[/TITLE]
            [CATEGORY]هنا التصنيف (اختر فقط حرفياً من: طمأنينة، خشوع، سكينة، دعاء)[/CATEGORY]

            الرابط: $url
            
            تأكد من عدم إضافة مسافات إضافية داخل الأقواس. أعد الرد باستخدام هذه الرموز فقط.
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
                put("temperature", 0.4)
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
        val maxAttempts = 3
        while (attempt < maxAttempts) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val rootJson = JSONObject(responseStr)
                    if (rootJson.has("error")) {
                        throw Exception(rootJson.getJSONObject("error").getString("message"))
                    }
                    val candidates = rootJson.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val contentObj = candidate.getJSONObject("content")
                        val parts = contentObj.getJSONArray("parts")
                        if (parts.length() > 0) {
                            val rawText = parts.getJSONObject(0).getString("text").trim()
                            
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
                                throw Exception("فشل في استخراج البيانات من الرد: ${e.message}")
                            }
                            
                            if (reciter.isBlank()) reciter = "Unknown"

                            return@withContext ClipAnalysisResult(
                                surah = surahNum,
                                startAyah = startA,
                                endAyah = endA,
                                reciterName = reciter,
                                title = title,
                                videoQuery = "",
                                category = category
                            )
                        }
                    }
                } else if (response.code == 429) {
                    if (attempt < maxAttempts - 1) {
                        attempt++
                        kotlinx.coroutines.delay(2000L * attempt)
                        continue
                    } else {
                        throw Exception("استنفذت الحد المسموح (خطأ 429). أضف مفتاح API الخاص بك في الإعدادات.")
                    }
                } else {
                    throw Exception("فشل الاتصال بـ Gemini API: HTTP ${response.code}\n${response.body?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e.message?.contains("429") == true) {
                    throw Exception("لقد استنفذت الحد المسموح (خطأ 429). يرجى إضافة مفتاح API الخاص بك في الإعدادات لتخطي هذا الحد.")
                }
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
        val maxAttempts = 3
        while (attempt < maxAttempts) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val rootJson = JSONObject(responseStr)
                    val candidates = rootJson.getJSONArray("candidates")
                    if (candidates.length() > 0) {
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
                        kotlinx.coroutines.delay(2000L * attempt)
                        continue
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            break
        }
        return@withContext null
    }
}
