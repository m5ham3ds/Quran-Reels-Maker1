package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.generator.VideoGenerator
import com.example.service.VideoGenerationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import org.json.JSONObject

sealed class ReelState {
    object Idle : ReelState()
    data class Loading(val message: String, val progress: Float) : ReelState()
    data class Success(
        val uri: Uri,
        val generatedMeta: com.example.generator.GeneratedMetaResult? = null,
        val publishedPlatforms: Map<String, Boolean> = emptyMap()
    ) : ReelState()
    data class Error(val message: String) : ReelState()
}

class ReelViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .build()
            chain.proceed(request)
        }
        .build()
    
    private val _uiState = MutableStateFlow<ReelState>(ReelState.Idle)
    val uiState: StateFlow<ReelState> = _uiState
    
    private val _reciters = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val reciters: StateFlow<List<Pair<String, String>>> = _reciters

    private val _ayahsAvailability = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val ayahsAvailability: StateFlow<Map<Int, Boolean>> = _ayahsAvailability

    private val _isCheckingAvailability = MutableStateFlow(false)
    val isCheckingAvailability: StateFlow<Boolean> = _isCheckingAvailability

    private var checkJob: Job? = null

    fun checkCurrentAyahsAvailability(surah: Int, startAyah: Int, endAyah: Int, reciterId: String) {
        checkJob?.cancel()
        if (reciterId.isBlank() || startAyah <= 0 || endAyah < startAyah) {
            _ayahsAvailability.value = emptyMap()
            _isCheckingAvailability.value = false
            return
        }
        
        checkJob = viewModelScope.launch(Dispatchers.IO) {
            _isCheckingAvailability.value = true
            try {
                val results = mutableMapOf<Int, Boolean>()
                val client = OkHttpClient.Builder()
                    .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .header("Accept", "*/*")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
                
                val maxCount = com.example.SURAH_COUNTS[surah] ?: 1
                val cleanStart = startAyah.coerceIn(1, maxCount)
                val cleanEnd = endAyah.coerceIn(cleanStart, maxCount)
                
                val checkLimit = 30
                val endCheck = if (cleanEnd - cleanStart + 1 > checkLimit) {
                    cleanStart + checkLimit - 1
                } else {
                    cleanEnd
                }
                
                val deferreds = (cleanStart..endCheck).map { ayah ->
                    async {
                        var global = 0
                        for (s in 1 until surah) {
                            global += com.example.SURAH_COUNTS[s] ?: 0
                        }
                        val globalAyah = global + ayah
                        
                        val url = "https://cdn.islamic.network/quran/audio/64/$reciterId/$globalAyah.mp3"
                        val request = Request.Builder().url(url).head().build() 
                        var exists = false
                        try {
                            client.newCall(request).execute().use { response ->
                                exists = response.isSuccessful
                            }
                        } catch (e: Exception) {
                            exists = true // fallback to avoid false alarms due to transient network drops
                        }
                        ayah to exists
                    }
                }
                deferreds.forEach {
                    val pair = it.await()
                    results[pair.first] = pair.second
                }
                _ayahsAvailability.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCheckingAvailability.value = false
            }
        }
    }

    // Active Generation Metadata Tracker
    private var currentSurah: Int = 1
    private var currentStartAyah: Int = 1
    private var currentEndAyah: Int = 5
    private var currentReciterId: String = "ar.alafasy"
    private var currentVideoQuery: String? = null

    private val _activeReciterId = MutableStateFlow(
        runBlocking { com.example.settings.SettingsManager(getApplication()).activeGenerationReciterId.first() }
    )
    val activeReciterId: StateFlow<String> = _activeReciterId

    fun resumeGeneration(context: Context) {
        generate(
            context = context,
            surah = currentSurah,
            startAyah = currentStartAyah,
            endAyah = currentEndAyah,
            reciterId = currentReciterId,
            isRetry = true,
            videoQuery = currentVideoQuery
        )
    }
    
    init {
        fetchReciters()
        
        // Sync activeReciterId from persistence
        viewModelScope.launch {
            val settingsManager = com.example.settings.SettingsManager(getApplication())
            settingsManager.activeGenerationReciterId.collect { id ->
                _activeReciterId.value = id
            }
        }

        viewModelScope.launch {
            VideoGenerationService.serviceState.collect { state ->
                when (state) {
                    is ReelState.Success -> {
                        val currentState = _uiState.value
                        if (currentState is ReelState.Success) {
                            return@collect
                        }

                        val app = getApplication<Application>()
                        val settingsManager = com.example.settings.SettingsManager(app)
                        val isArabic = settingsManager.language.first() == "ar"

                        _uiState.value = ReelState.Loading(
                            if (isArabic) "جاري توليد وحفظ معلومات النشر الاحترافية..." 
                            else "Generating and saving professional publish details...",
                            0.92f
                        )

                        val surahName = com.example.SURAH_NAMES.getOrNull(currentSurah - 1) ?: "سورة"
                        val reciterName = _reciters.value.find { it.first == currentReciterId }?.second ?: "العفاسي"

                        // Run metadata generator always for all 4 channels to create the comprehensive details .txt file!
                        val generator = com.example.generator.GeminiMetaGenerator()
                        var meta = generator.generateSocialMeta(
                            context = app,
                            surahName = surahName,
                            startAyah = currentStartAyah,
                            endAyah = currentEndAyah,
                            reciterName = reciterName,
                            isTiktok = true,
                            isInstagram = true,
                            isFacebook = true,
                            isYoutube = true
                        )

                        // Elegant template generator if Gemini is not configured / failed
                        if (meta == null) {
                            val title = if (isArabic)
                                "تلاوة خاشعة تريح قلبك المنهك بالهموم ☕️🌿 | سورة $surahName آيات $currentStartAyah-$currentEndAyah"
                            else "Breathtaking peaceful Quran recitation - Surah $surahName Verses $currentStartAyah-$currentEndAyah"

                            val descriptionYt = if (isArabic)
                                "أنصت بقلبك إلى تلاوة خاشعة هادئة تذيب الهموم والأحزان للقارئ الشيخ $reciterName.\nسورة $surahName (الآيات من $currentStartAyah إلى $currentEndAyah).\nاشترك في القناة للمزيد من المقاطع والتدبر الإيماني."
                            else "Ponder upon this moving and peaceful recitation of Surah $surahName, Ayahs $currentStartAyah-$currentEndAyah by Sheikh $reciterName. Please subscribe for more daily spiritual reminders."

                            val descriptionTt = if (isArabic)
                                "أرح قلبك المنهك بآيات من سورة $surahName تلاوة تفوق الوصف للقارئ $reciterName 🥀✨\nاستمع وتدبر الآيات الكريمة شارك الأجر بالنشر والتعليق."
                            else "Take a deep breath and soothe your anxious heart. Holy Quran recitation by $reciterName 🌌🌸\nSupport the page by sharing the reward."

                            val descriptionFb = if (isArabic)
                                "تلاوة تفوق الوصف تلامس الروح وتذهب هموم الدنيا 🤲🌿\nسورة $surahName بصوت الشيخ $reciterName.\nأسأل الله العظيم أن يجعل القرآن ربيع قلوبنا وجلاء همومنا ونور صدورنا."
                            else "A recitation that touches the soul and cleanses the heart of worldly worries. Surah $surahName, recited beautifully by $reciterName. May Allah reward everyone who shares this video."

                            val descriptionInst = if (isArabic)
                                "هدوء وسكينة حقيقية لروحك المتعبة 💎✨\nالقرآن الكريم، بصوت القارئ الشيخ $reciterName (سورة $surahName آيات $currentStartAyah-$currentEndAyah).\n#اكسبلور_قرآن #تدبر #راحة_نفسية"
                            else "Deep tranquil peace for your soul. Holy Quran, Surah $surahName Ayat $currentStartAyah-$currentEndAyah recited by $reciterName. 🌱💖\n#explore #quran"

                            meta = com.example.generator.GeneratedMetaResult(
                                tiktok = com.example.generator.PlatformMeta(title, descriptionTt, if (isArabic) "#قران_كريم #تلاوة_خاشعة #راحة_نفسية #أرح_قلبك #foryou #قرآن" else "#quran #peace #islam"),
                                instagram = com.example.generator.PlatformMeta(title, descriptionInst, if (isArabic) "#reels #quran #راحة #اسلاميات #explore #تدبر" else "#reels #explore #quran"),
                                facebook = com.example.generator.PlatformMeta(title, descriptionFb, if (isArabic) "#دروس_دينية #تلاوت_خاشعة #فيس_بوك_إسلامي #أرح_سمعك" else "#quran #recitation #facebookislam"),
                                youtube = com.example.generator.PlatformMeta(title, descriptionYt, if (isArabic) "#Shorts #قرآن #تلاوة_خاشعة #راحة_نفسية" else "#Shorts #quran #recitation")
                            )
                        }

                        // Write publish details file to /storage/emulated/0/Movies/Quran Reels/Details/
                        try {
                            val detailsContent = """
                                معلومات النشر على اليوتيوب : 
                                
                                الاسم : ${meta.youtube?.title ?: ""}
                                
                                الوصف : ${meta.youtube?.description ?: ""}
                                
                                الكلمات الدلالية و الهاشتاقات : ${meta.youtube?.hashtags ?: ""}
                                
                                
                                معلومات النشر على التيك توك : 
                                
                                الاسم : ${meta.tiktok?.title ?: ""}
                                
                                الوصف : ${meta.tiktok?.description ?: ""}
                                
                                الكلمات الدلالية و الهاشتاقات : ${meta.tiktok?.hashtags ?: ""}
                                
                                
                                معلومات النشر على الفيسبوك : 
                                
                                الاسم : ${meta.facebook?.title ?: ""}
                                
                                الوصف : ${meta.facebook?.description ?: ""}
                                
                                الكلمات الدلالية و الهاشتاقات : ${meta.facebook?.hashtags ?: ""}
                                
                                
                                معلومات النشر على الانستغرام : 
                                
                                الاسم : ${meta.instagram?.title ?: ""}
                                
                                الوصف : ${meta.instagram?.description ?: ""}
                                
                                الكلمات الدلالية و الهاشتاقات : ${meta.instagram?.hashtags ?: ""}
                            """.trimIndent()
                            
                            var rawSuccess = false
                            try {
                                val detailsDir = java.io.File("/storage/emulated/0/Movies/Quran Reels/Details")
                                if (!detailsDir.exists()) {
                                    detailsDir.mkdirs()
                                }
                                if (detailsDir.exists()) {
                                    val detailsFile = java.io.File(detailsDir, "Quran_Reel_Publish_Details_${System.currentTimeMillis()}.txt")
                                    detailsFile.writeText(detailsContent, Charsets.UTF_8)
                                    
                                    // Let the system MediaScanner scan this file as well
                                    android.media.MediaScannerConnection.scanFile(
                                        app,
                                        arrayOf(detailsFile.absolutePath),
                                        arrayOf("text/plain"),
                                        null
                                    )
                                    rawSuccess = true
                                    android.util.Log.d("DetailsWriter", "Saved details raw file successfully to: ${detailsFile.absolutePath}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("DetailsWriter", "Raw path write failed: ${e.message}. Using MediaStore insertion...")
                            }

                            // If raw file creation fails (due to Scoped Storage on Android 10+), write using MediaStore ContentResolver!
                            if (!rawSuccess) {
                                val values = android.content.ContentValues().apply {
                                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "Quran_Reel_Publish_Details_${System.currentTimeMillis()}.txt")
                                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Movies/Quran Reels/Details")
                                    }
                                }
                                
                                val externalUri = android.provider.MediaStore.Files.getContentUri("external")
                                val textUri = app.contentResolver.insert(externalUri, values)
                                if (textUri != null) {
                                    app.contentResolver.openOutputStream(textUri)?.use { out ->
                                        out.write(detailsContent.toByteArray(Charsets.UTF_8))
                                    }
                                    android.util.Log.d("DetailsWriter", "Saved details to MediaStore database successfully: $textUri")
                                } else {
                                    android.util.Log.e("DetailsWriter", "Failed to retrieve MediaStore reference.")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.util.Log.e("DetailsWriter", "All details file saving attempts failed: ${e.message}")
                        }

                        val isTiktok = settingsManager.tiktokLinked.first() && settingsManager.tiktokAutopost.first()
                        val isInstagram = settingsManager.instagramLinked.first() && settingsManager.instagramAutopost.first()
                        val isFacebook = settingsManager.facebookLinked.first() && settingsManager.facebookAutopost.first()
                        val isYoutube = settingsManager.youtubeLinked.first() && settingsManager.youtubeAutopost.first()
                        val isGoogle = settingsManager.googleDriveSheetsLinked.first() && settingsManager.googleAutoSaveEnabled.first()

                        val tiktokToken = settingsManager.tiktokAccessToken.first()
                        val instagramToken = settingsManager.instagramAccessToken.first()
                        val facebookToken = settingsManager.facebookAccessToken.first()
                        val youtubeToken = settingsManager.youtubeAccessToken.first()
                        val webhookUrl = settingsManager.webhookPublishUrl.first()

                        // Direct Google Publisher integration
                        var googleDriveLink: String? = null
                        if (isGoogle) {
                            try {
                                val tempFile = java.io.File(app.cacheDir, "temp_google_upload_${System.currentTimeMillis()}.mp4")
                                app.contentResolver.openInputStream(state.uri)?.use { input ->
                                    tempFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                
                                val descriptionText = meta.tiktok?.description 
                                    ?: meta.instagram?.description 
                                    ?: "Quran Reel - Surah $surahName Ayat $currentStartAyah-$currentEndAyah"

                                val googlePublisher = com.example.generator.GoogleDriveSheetsPublisher(app)
                                val res = googlePublisher.publishReel(
                                    videoFile = tempFile,
                                    surahName = surahName,
                                    ayahRange = "$currentStartAyah-$currentEndAyah",
                                    reciterName = reciterName,
                                    description = descriptionText
                                )
                                if (res != null) {
                                    googleDriveLink = res.first
                                    android.util.Log.d("GooglePublisher", "Direct Google Publisher succeeded! Link: $googleDriveLink")
                                }
                                try { tempFile.delete() } catch (ex: Exception) {}
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.util.Log.e("GooglePublisher", "Direct Google Publisher failed: ${e.message}")
                            }
                        }

                        // Webhook dispatch
                        if (webhookUrl.isNotBlank()) {
                            dispatchWebhook(
                                webhookUrl = webhookUrl,
                                videoUri = state.uri,
                                surah = currentSurah,
                                startAyah = currentStartAyah,
                                endAyah = currentEndAyah,
                                reciter = currentReciterId,
                                tiktokToken = tiktokToken,
                                instagramToken = instagramToken,
                                facebookToken = facebookToken,
                                youtubeToken = youtubeToken,
                                metaResult = meta
                            )
                        }

                        _uiState.value = ReelState.Success(
                            uri = state.uri,
                            generatedMeta = meta,
                            publishedPlatforms = mapOf(
                                "tiktok" to isTiktok,
                                "instagram" to isInstagram,
                                "facebook" to isFacebook,
                                "youtube" to isYoutube,
                                "google_drive" to (googleDriveLink != null)
                            )
                        )
                    }
                    is ReelState.Loading -> {
                        _uiState.value = state
                    }
                    is ReelState.Error -> {
                        _uiState.value = state
                    }
                    is ReelState.Idle -> {
                        _uiState.value = state
                    }
                }
            }
        }
    }
    
    private fun fetchReciters() {
        val list = listOf(
            "ar.abdullahbasfar" to "عبد الله بصفر (Abdullah Basfar)",
            "ar.abdurrahmaansudais" to "عبدالرحمن السديس (Abdurrahmaan As-Sudais)",
            "ar.abdulsamad" to "عبدالباسط عبدالصمد (Abdul Samad)",
            "ar.shaatree" to "أبو بكر الشاطري (Abu Bakr Ash-Shaatree)",
            "ar.ahmedajamy" to "أحمد بن علي العجمي (Ahmed ibn Ali al-Ajamy)",
            "ar.alafasy" to "مشاري العفاسي (Alafasy)",
            "ar.hanirifai" to "هاني الرفاعي (Hani Rifai)",
            "ar.husary" to "محمود خليل الحصري (Husary)",
            "ar.husarymujawwad" to "محمود خليل الحصري - المجود (Husary Mujawwad)",
            "ar.hudhaify" to "علي بن عبدالرحمن الحذيفي (Hudhaify)",
            "ar.ibrahimakhbar" to "إبراهيم الأخضر (Ibrahim Akhdar)",
            "ar.mahermuaiqly" to "ماهر المعيقلي (Maher Al Muaiqly)",
            "ar.muhammadayyoub" to "محمد أيوب (Muhammad Ayyoub)",
            "ar.muhammadjibreel" to "محمد جبريل (Muhammad Jibreel)",
            "ar.saoodshuraym" to "سعود الشريم (Saood bin Ibraaheem Ash-Shuraym)",
            "ar.parhizgar" to "شهریار پرهیزگار (Parhizgar)",
            "ar.aymanswoaid" to "أيمن سويد (Ayman Sowaid)"
        )
        _reciters.value = list
    }
    
    fun generate(
        context: Context,
        surah: Int,
        startAyah: Int,
        endAyah: Int,
        reciterId: String,
        isRetry: Boolean = false,
        videoQuery: String? = null
    ) {
        currentSurah = surah
        currentStartAyah = startAyah
        currentEndAyah = endAyah
        currentReciterId = reciterId
        _activeReciterId.value = reciterId
        viewModelScope.launch {
            val settingsManager = com.example.settings.SettingsManager(getApplication())
            settingsManager.setActiveGenerationReciterId(reciterId)
        }
        currentVideoQuery = videoQuery

        _uiState.value = ReelState.Loading(if (isRetry) "جاري استئناف المعالجة وإعادة المحاولة..." else "جاري البدء...", 0f)
        viewModelScope.launch {
            val settingsManager = com.example.settings.SettingsManager(context)
            val showTranslation = settingsManager.showTranslation.first()
            val pexelsApiKey = settingsManager.pexelsApiKey.first()
            val videoQuality = settingsManager.videoQuality.first()
            
            val intent = Intent(context, VideoGenerationService::class.java).apply {
                putExtra("surah", surah)
                putExtra("startAyah", startAyah)
                putExtra("endAyah", endAyah)
                putExtra("reciterId", reciterId)
                putExtra("showTranslation", showTranslation)
                putExtra("pexelsApiKey", pexelsApiKey)
                putExtra("videoQuality", videoQuality)
                putExtra("isRetry", isRetry)
                if (videoQuery != null) {
                    putExtra("videoQuery", videoQuery)
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    fun togglePauseGeneration() {
        VideoGenerationService.togglePauseResumed()
        val current = _uiState.value
        if (current is ReelState.Loading) {
            _uiState.value = ReelState.Loading(current.message, current.progress)
        }
    }

    fun cancelGeneration() {
        VideoGenerationService.cancelGeneration()
        _uiState.value = ReelState.Idle
    }

    val isGenerationPausedFlow = VideoGenerationService.isPausedState

    fun isGenerationPaused(): Boolean {
        return VideoGenerationService.isPaused
    }
    
    fun reset() {
        VideoGenerationService.clearState()
        _uiState.value = ReelState.Idle
    }

    private fun dispatchWebhook(
        webhookUrl: String,
        videoUri: Uri,
        surah: Int,
        startAyah: Int,
        endAyah: Int,
        reciter: String,
        tiktokToken: String,
        instagramToken: String,
        facebookToken: String,
        youtubeToken: String,
        metaResult: com.example.generator.GeneratedMetaResult?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                var fileBytes: ByteArray? = null
                try {
                    app.contentResolver.openInputStream(videoUri)?.use { input ->
                        fileBytes = input.readBytes()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.util.Log.e("Webhook", "Could not read video file bytes: ${e.message}")
                }

                val json = JSONObject().apply {
                    put("event", "video_generated")
                    put("surah", surah)
                    put("startAyah", startAyah)
                    put("endAyah", endAyah)
                    put("reciter", reciter)
                    put("localVideoUri", videoUri.toString())
                    
                    val metaJson = JSONObject().apply {
                        if (metaResult != null) {
                            metaResult.tiktok?.let {
                                put("tiktok", JSONObject().apply {
                                    put("title", it.title)
                                    put("description", it.description)
                                    put("hashtags", it.hashtags)
                                })
                            }
                            metaResult.instagram?.let {
                                put("instagram", JSONObject().apply {
                                    put("title", it.title)
                                    put("description", it.description)
                                    put("hashtags", it.hashtags)
                                })
                            }
                            metaResult.facebook?.let {
                                put("facebook", JSONObject().apply {
                                    put("title", it.title)
                                    put("description", it.description)
                                    put("hashtags", it.hashtags)
                                })
                            }
                            metaResult.youtube?.let {
                                put("youtube", JSONObject().apply {
                                    put("title", it.title)
                                    put("description", it.description)
                                    put("hashtags", it.hashtags)
                                })
                            }
                        }
                    }
                    put("geminiMetadata", metaJson)

                    val tokensJson = JSONObject().apply {
                        put("tiktokToken", tiktokToken)
                        put("instagramToken", instagramToken)
                        put("facebookToken", facebookToken)
                        put("youtubeToken", youtubeToken)
                    }
                    put("apiTokens", tokensJson)
                }

                val requestBody = if (fileBytes != null) {
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("event", "video_generated")
                        .addFormDataPart("surah", surah.toString())
                        .addFormDataPart("startAyah", startAyah.toString())
                        .addFormDataPart("endAyah", endAyah.toString())
                        .addFormDataPart("reciter", reciter)
                        .addFormDataPart("payload", json.toString())
                        .addFormDataPart(
                            "video",
                            "quran_reel_${System.currentTimeMillis()}.mp4",
                            fileBytes!!.toRequestBody("video/mp4".toMediaType())
                        )
                        .build()
                } else {
                    json.toString().toRequestBody("application/json".toMediaType())
                }

                val request = Request.Builder()
                    .url(webhookUrl)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    android.util.Log.d("Webhook", "Webhook execution completed with code: ${response.code}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("Webhook", "Webhook execution failed: ${e.message}")
            }
        }
    }
}
