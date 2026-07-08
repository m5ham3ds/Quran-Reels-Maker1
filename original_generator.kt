package com.example.generator

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class WordSegment(
    val wordIndex: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val word: String = ""
)

data class SmartChunk(
    val arabic: String,
    val english: String?,
    val startTimeMs: Long,
    val endTimeMs: Long
)

data class SurahAudioData(
    val segments: Map<Int, List<WordSegment>>,
    val audioUrls: Map<Int, String>
)

data class VerseData(
    val surahName: String,
    val text: String,
    val translation: String?,
    val audioPath: String,
    val durationUs: Long,
    val energyTimeline: List<Pair<Long, Float>>,
    val wordSegments: List<WordSegment> = emptyList(),
    val chunks: List<SmartChunk> = emptyList()
)

class VideoGenerator {

    private val client = OkHttpClient.Builder()
        // Force IDE text refresh - updated with User-Agent & timeout
        .connectTimeout(1800, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(1800, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(1800, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .method(original.method, original.body)
            chain.proceed(requestBuilder.build())
        }
        .build()
    
    @Volatile 
    private var threadError: Throwable? = null
    
    // Cached typefaces
    private var customArabicTypefaces = mutableMapOf<String, Typeface>()
    private var customEnglishTypefaces = mutableMapOf<String, Typeface>()
    
    private val arabicFontUrls = mapOf(
        "Amiri" to "https://github.com/google/fonts/raw/main/ofl/amiriquran/AmiriQuran-Regular.ttf",
        "Cairo" to "https://github.com/google/fonts/raw/main/ofl/cairo/static/Cairo-Bold.ttf",
        "Scheherazade New" to "https://github.com/google/fonts/raw/main/ofl/scheherazadenew/ScheherazadeNew-Bold.ttf",
        "Lateef" to "https://github.com/google/fonts/raw/main/ofl/lateef/Lateef-Regular.ttf",
        "Reem Kufi" to "https://github.com/google/fonts/raw/main/ofl/reemkufi/static/ReemKufi-Bold.ttf"
    )
    
    private val englishFontUrls = mapOf(
        "Montserrat" to "https://github.com/google/fonts/raw/main/ofl/montserrat/static/Montserrat-Medium.ttf",
        "Roboto" to "https://github.com/google/fonts/raw/main/ofl/roboto/static/Roboto-Medium.ttf",
        "Playfair" to "https://github.com/google/fonts/raw/main/ofl/playfairdisplay/static/PlayfairDisplay-Italic.ttf",
        "Lato" to "https://github.com/google/fonts/raw/main/ofl/lato/Lato-Regular.ttf"
    )

    private fun getArabicTypeface(context: Context, fontName: String): Typeface {
        return customArabicTypefaces[fontName] ?: try {
            if (fontName.startsWith("/")) {
                val file = File(fontName)
                if (file.exists() && file.length() > 0) {
                    val tf = Typeface.createFromFile(file)
                    customArabicTypefaces[fontName] = tf
                    return tf
                }
            }
            val fileName = fontName.replace(" ", "") + ".ttf"
            val file = File(context.cacheDir, fileName)
            if (file.exists() && file.length() > 1000) {
                val tf = Typeface.createFromFile(file)
                customArabicTypefaces[fontName] = tf
                tf
            } else {
                Typeface.create("serif", Typeface.BOLD)
            }
        } catch (e: Exception) {
            Typeface.create("serif", Typeface.BOLD)
        }
    }

    private fun getEnglishTypeface(context: Context, fontName: String): Typeface {
        return customEnglishTypefaces[fontName] ?: try {
            if (fontName.startsWith("/")) {
                val file = File(fontName)
                if (file.exists() && file.length() > 0) {
                    val tf = Typeface.createFromFile(file)
                    customEnglishTypefaces[fontName] = tf
                    return tf
                }
            }
            val fileName = "EN_" + fontName.replace(" ", "") + ".ttf"
            val file = File(context.cacheDir, fileName)
            if (file.exists() && file.length() > 1000) {
                val tf = Typeface.createFromFile(file)
                customEnglishTypefaces[fontName] = tf
                tf
            } else {
                Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }
        } catch (e: Exception) {
            Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }

    fun cancelNetworkRequests() {
        try {
            client.dispatcher.cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatAyahSymbol(ayah: Int): String {
        val arabicStr = ayah.toString()
            .replace("0", "٠").replace("1", "١").replace("2", "٢")
            .replace("3", "٣").replace("4", "٤").replace("5", "٥")
            .replace("6", "٦").replace("7", "٧").replace("8", "٨")
            .replace("9", "٩")
        // U+06DD is the Arabic End of Ayah symbol, and numbers fit inside it in standard fonts.
        // However, not all fonts render it correctly, so we'll use a reliable fallback with standard brackets.
        return " ﴿$arabicStr﴾"
    }

    suspend fun generateReel(
        context: Context,
        surah: Int,
        startAyah: Int,
        endAyah: Int,
        reciterId: String,
        showTranslation: Boolean,
        pexelsApiKey: String,
        videoQuality: String = "Ultra",
        isRetry: Boolean = false,
        isPreviewMode: Boolean = false,
        includeBasmalah: Boolean = true,
        videoQuery: String? = null,
        onProgress: (String, Float) -> Unit,
        onComplete: (Uri) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        SystemDiagnosticTracker.clearLogs()
        SystemDiagnosticTracker.addLog("PROCESS_START", "بدء تصنيع مقطع الريدز الجديد (Start generate reel). السورة: $surah, البداية: $startAyah, النهاية: $endAyah, القارئ: $reciterId, الترجمة: $showTranslation")
        threadError = null

        // FFmpeg and YoutubeDL dependencies removed, processing now uses Cobalt API entirely.
        var videoCodec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var bgBitmap: Bitmap? = null
        var retriever: MediaMetadataRetriever? = null
        
        checkCancellationAndPause()
        
        try {
            val verses = mutableListOf<VerseData>()
            val totalAyahs = endAyah - startAyah + 1
            
            // 1. Fetch text & style configurations
            val settingsManager = SettingsManager(context)
            val language = settingsManager.language.first()
            val isArabic = language == "ar"
            
            val fontFamily = settingsManager.fontFamily.first()
            val fontWeight = settingsManager.fontWeight.first()
            val textFontSize = settingsManager.fontSize.first()
            val textColorStr = settingsManager.textColor.first()
            val textOpacity = settingsManager.textOpacity.first()
            
            val showTextBg = settingsManager.showTextBackground.first()
            val textBgColorStr = settingsManager.textBgColor.first()
            val textBgOpacity = settingsManager.textBgOpacity.first()
            val textBgRadius = settingsManager.textBgRadius.first()
            
            val textPosition = settingsManager.textPosition.first()
            val textAlign = settingsManager.textAlign.first()
            val textAnimationType = settingsManager.textAnimation.first()
            
            val translationFontSize = settingsManager.translationFontSize.first()
            val translationColorStr = settingsManager.translationColor.first()
            val translationFontFamily = settingsManager.translationFontFamily.first()
            val translationFontWeight = settingsManager.translationFontWeight.first()
            val translationTextX = settingsManager.translationTextX.first()
            val translationTextY = settingsManager.translationTextY.first()
            val arabicTextX = settingsManager.arabicTextX.first()
            val arabicTextY = settingsManager.arabicTextY.first()
            val surahNameFontSize = settingsManager.surahNameFontSize.first()
            val surahNameColorStr = settingsManager.surahNameColor.first()
            val surahNameOpacity = settingsManager.surahNameOpacity.first()
            val surahNameX = settingsManager.surahNameX.first()
            val surahNameY = settingsManager.surahNameY.first()
            val iconOpacity = settingsManager.iconOpacity.first()
            val iconX = settingsManager.iconX.first()
            val iconY = settingsManager.iconY.first()
            val pixabayApiKey = settingsManager.pixabayApiKey.first()
            val backgroundKeywords = settingsManager.backgroundKeywords.first()
            
            // Download and prepare custom fonts if needed
            try {
                val arFontFileName = fontFamily.replace(" ", "") + ".ttf"
                val arabicFontCacheFile = File(context.cacheDir, arFontFileName)
                val arUrl = arabicFontUrls[fontFamily]
                
                if (arUrl != null && (!arabicFontCacheFile.exists() || arabicFontCacheFile.length() < 1000)) {
                    onProgress(if (isArabic) "جاري تحميل خط القرآن ($fontFamily)..." else "Downloading primary font...", 0.01f)
                    downloadAudio(arUrl, arabicFontCacheFile)
                }
                
                val enFontFileName = "EN_" + translationFontFamily.replace(" ", "") + ".ttf"
                val englishFontCacheFile = File(context.cacheDir, enFontFileName)
                val enUrl = englishFontUrls[translationFontFamily]
                
                if (enUrl != null && (!englishFontCacheFile.exists() || englishFontCacheFile.length() < 1000)) {
                    onProgress(if (isArabic) "جاري تحميل خط الترجمة الإنجليزية ($translationFontFamily)..." else "Downloading secondary font...", 0.015f)
                    downloadAudio(enUrl, englishFontCacheFile)
                }
            } catch (e: Exception) {
                SystemDiagnosticTracker.addLog("FONT", "فشل تحميل الخطوط المخصصة: ${e.message}")
            }
            
            val isPopularUIState = reciterId.startsWith("popular|")
            var isPopularUrlDownload = false
            var isYoutubeUrlDownload = false
            var actualReciterId = reciterId
            
            if (isPopularUIState) {
                val suffix = reciterId.substringAfter("popular|")
                if (suffix.startsWith("youtube|")) {
                    isPopularUrlDownload = true
                    isYoutubeUrlDownload = true
                    actualReciterId = suffix.substringAfter("youtube|")
                } else if (suffix.startsWith("http") || suffix.startsWith("https")) {
                    isPopularUrlDownload = true
                    actualReciterId = suffix
                } else {
                    actualReciterId = suffix
                }
            }
            
            // Optional: Prepend Basmalah (بسم الله الرحمن الرحيم) as a separate verse/card
            if (includeBasmalah && surah != 1 && surah != 9 && !isPopularUrlDownload) {
                SystemDiagnosticTracker.addLog("BASMALAH", "تهيئة البسملة المباركة (Basmalah required for surah $surah)")
                onProgress(if (isArabic) "جاري تهيئة البسملة المباركة..." else "Initializing blessed Basmalah...", 0.02f)
                try {
                    val basmalahText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                    val basmalahTranslation = if (showTranslation) "In the name of Allah, the Entirely Merciful, the Especially Merciful" else null
                    
                    val audioFileName = "${actualReciterId}_basmalah.mp3"
                    val url = "https://cdn.islamic.network/quran/audio/64/$actualReciterId/1.mp3" // Universal Basmalah is Surah 1 Ayah 1 of the chosen reciter
                    val destFile = File(context.cacheDir, audioFileName)
                    
                    SystemDiagnosticTracker.addLog("DOWNLOAD", "تحميل صوت البسملة من الرابط: $url")
                    downloadAudio(url, destFile)
                    SystemDiagnosticTracker.addLog("DOWNLOAD", "تم تحميل صوت البسملة بنجاح، الحجم: ${destFile.length()} بايت")
                    
                    val aacFileName = "${actualReciterId}_basmalah_transcoded.m4a"
                    val aacFile = File(context.cacheDir, aacFileName)
                    SystemDiagnosticTracker.addLog("TRANSCODE", "تحويل صيغة صوت البسملة إلى AAC/M4A لضمان توافقية الدمج")
                    val timeline = transcodeMp3ToAac(destFile.absolutePath, aacFile.absolutePath)
                    SystemDiagnosticTracker.addLog("TRANSCODE", "اكتمل تحويل صوت البسملة بنجاح")

                    SystemDiagnosticTracker.addLog("ALIGNMENT", "بدء مواءمة البسملة مع WhisperX")
                    val alignedData = alignWithWhisperX(context, aacFile, null, basmalahText)
                    val alignedSegments = alignedData.first
                    val whisperXChunks = alignedData.second
                    SystemDiagnosticTracker.addLog("ALIGNMENT", "تمت مواءمة البسملة بنجاح، عدد الكلمات: ${alignedSegments.size}")
                    
                    val ext = MediaExtractor().apply { setDataSource(aacFile.absolutePath) }
                    ext.selectTrack(0)
                    var durationUs = ext.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION, -1L)
                    if (durationUs <= 0) {
                        var maxTs = 0L
                        val bb = ByteBuffer.allocate(256)
                        while (ext.readSampleData(bb, 0) >= 0) {
                            maxTs = ext.sampleTime
                            ext.advance()
                        }
                        durationUs = maxTs
                    }
                    ext.release()
                    
                    val durationMs = durationUs / 1000
                    val smartChunks = getSmartChunks(context, basmalahText, basmalahTranslation, alignedSegments, whisperXChunks, durationMs)
                    verses.add(VerseData("بِسْمِ اللَّهِ", basmalahText, basmalahTranslation, aacFile.absolutePath, durationUs, timeline, alignedSegments, smartChunks))
                    SystemDiagnosticTracker.addLog("BASMALAH", "تم إعداد كارت بطاقة البسملة بنجاح بمدة ${durationMs}ms")
                } catch (e: Exception) {
                    SystemDiagnosticTracker.addLog("WARN", "فشلت تهيئة البسملة أو تجاوزناها بسبب خطأ: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            if (isPopularUrlDownload) {
                val audioUrl = actualReciterId
                onProgress(if (isArabic) "جاري تحميل مراجع المقطع الرائج..." else "Downloading popular clip audio...", 0.05f)
                
                val destFile = File(context.cacheDir, "popular_clip_${surah}_${startAyah}_${endAyah}.mp3")
                SystemDiagnosticTracker.addLog("DOWNLOAD", "تم تخطي التحميل المحلي للمقطع الرائج ليتم تحميله عبر WhisperX Backend")

                // Fetch combined texts
                val combinedArabic = java.lang.StringBuilder()
                val combinedTranslation = java.lang.StringBuilder()
                
                for (ayah in startAyah..endAyah) {
                    val info = fetchVerseInfo(surah, ayah, "quran-uthmani")
                    var text = info.first
                    if (surah != 1 && surah != 9 && ayah == 1) {
                        val standardBasmalah = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                        text = text.trim()
                        if (text.startsWith(standardBasmalah)) {
                            text = text.substring(standardBasmalah.length).trim()
                        } else {
                            val keywords = listOf("بِسْمِ اللَّهِ", "بِسْمِ اللهِ", "بِسْمِ")
                            for (kw in keywords) {
                                if (text.startsWith(kw)) {
                                    val idx = text.indexOf("الرَّحِيمِ")
                                    if (idx != -1 && idx < 60) {
                                        text = text.substring(idx + "الرَّحِيمِ".length).trim()
                                        break
                                    }
                                    val idx2 = text.indexOf("الرَّحِيْمِ")
                                    if (idx2 != -1 && idx2 < 60) {
                                        text = text.substring(idx2 + "الرَّحِيْمِ".length).trim()
                                        break
                                    }
                                }
                            }
                        }
                    }
                    
                    text += formatAyahSymbol(ayah)
                    combinedArabic.append(text).append(" ")
                    
                    if (showTranslation) {
                    var trans = fetchVerseInfo(surah, ayah, "en.sahih").first
                        if (surah != 1 && surah != 9 && ayah == 1) {
                            val basmalahEnglishList = listOf(
                                "In the name of God, the Most Gracious, the Most Merciful",
                                "In the name of God, Most Gracious, Most Merciful",
                                "In the name of Allah, the Entirely Merciful, the Especially Merciful",
                                "In the name of Allah, the Beneficent, the Merciful",
                                "In the name of Allah, the Compassionate, the Merciful",
                                "In the name of God, the Compassionate, the Merciful"
                            )
                            var cleanTrans = trans.trim()
                            for (b in basmalahEnglishList) {
                                if (cleanTrans.lowercase().startsWith(b.lowercase())) {
                                    cleanTrans = cleanTrans.substring(b.length).trim()
                                    if (cleanTrans.startsWith("-") || cleanTrans.startsWith(".") || cleanTrans.startsWith(":") || cleanTrans.startsWith(",")) {
                                        cleanTrans = cleanTrans.substring(1).trim()
                                    }
                                    break
                                }
                            }
                            trans = cleanTrans
                        }
                        combinedTranslation.append(trans).append(" ")
                    }
                }
                
                val fullArabicText = combinedArabic.toString().trim()
                val fullTranslationText = if (showTranslation) combinedTranslation.toString().trim() else null

                val cacheKeyUrl = audioUrl
                val cached = AlignmentCacheManager.getCachedAlignment(context, cacheKeyUrl, fullArabicText)
                
                var aacFile: File? = null
                var durationUs: Long = 0
                var timeline: List<Pair<Long, Float>> = emptyList()
                var alignedSegments: List<WordSegment> = emptyList()
                var smartChunks: List<SmartChunk> = emptyList()
                
                if (cached != null && cached.audioPath != null && File(cached.audioPath).exists()) {
                    SystemDiagnosticTracker.addLog("CACHE", "تم استرجاع معلومات الموائمة والملف الصوتي للمقطع المشهور من الذاكرة المؤقتة")
                    aacFile = File(cached.audioPath)
                    alignedSegments = cached.wordSegments
                    smartChunks = cached.smartChunks
                    
                    val ext = MediaExtractor().apply { setDataSource(aacFile.absolutePath) }
                    ext.selectTrack(0)
                    durationUs = ext.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION, -1L)
                    if (durationUs <= 0) {
                        var maxTs = 0L
                        val bb = ByteBuffer.allocate(256)
                        while (ext.readSampleData(bb, 0) >= 0) {
                            maxTs = ext.sampleTime
                            ext.advance()
                        }
                        durationUs = maxTs
                    }
                    ext.release()
                    timeline = emptyList()
                } else {
                    SystemDiagnosticTracker.addLog("ALIGNMENT", "بدء مواءمة المجمع للآيات المشهورة بالذكاء الاصطناعي WhisperX")
                    onProgress(if (isArabic) "جاري مواءمة الكلمات بالذكاء الاصطناعي لمجموع الآيات..." else "Aligning popular clip with WhisperX...", 0.12f)
                    val alignedData = alignWithWhisperX(context, destFile, audioUrl, fullArabicText)
                    alignedSegments = alignedData.first
                    val whisperXChunks = alignedData.second
                    SystemDiagnosticTracker.addLog("ALIGNMENT", "تمت مواءمة مقطع السورة بالكامل بنجاح. عدد الكلمات المسترجعة: ${alignedSegments.size}")
                    
                    onProgress(if (isArabic) "جاري ترميز صوت المقطع بدقة سينمائية..." else "Encoding popular clip audio...", 0.18f)
                    val aacFileName = "popular_clip_${surah}_${startAyah}_${endAyah}_transcoded.m4a"
                    aacFile = File(context.cacheDir, aacFileName)
                    timeline = transcodeMp3ToAac(destFile.absolutePath, aacFile.absolutePath)
                    SystemDiagnosticTracker.addLog("TRANSCODE", "تم تحويل ترميز ملف المقطع المشهور وإعداد مسار اللوحات")

                    val ext = MediaExtractor().apply { setDataSource(aacFile.absolutePath) }
                    ext.selectTrack(0)
                    durationUs = ext.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION, -1L)
                    if (durationUs <= 0) {
                        var maxTs = 0L
                        val bb = ByteBuffer.allocate(256)
                        while (ext.readSampleData(bb, 0) >= 0) {
                            maxTs = ext.sampleTime
                            ext.advance()
                        }
                        durationUs = maxTs
                    }
                    ext.release()
                    
                    val durationMs = durationUs / 1000
                    SystemDiagnosticTracker.addLog("CHUNKS", "تقسيم المقطع المشهور إلى Smart Chunks للمزامنة البصرية")
                    smartChunks = getSmartChunks(context, fullArabicText, fullTranslationText, alignedSegments, whisperXChunks, durationMs)
                    
                    AlignmentCacheManager.putCachedAlignment(context, cacheKeyUrl, fullArabicText, alignedSegments, smartChunks, aacFile)
                }
                
                // Fetch Surah name for the title instead of 'الآيات x-y'
                val surahName = try {
                    fetchVerseInfo(surah, startAyah, "quran-uthmani").third
                } catch(e: Exception) {
                    "سورة $surah"
                }
                
                verses.add(VerseData(surahName, fullArabicText, fullTranslationText, aacFile.absolutePath, durationUs, timeline, alignedSegments, smartChunks))
                SystemDiagnosticTracker.addLog("PROCESS", "تم إعداد كارت مقطع الآيات المشهورة بنجاح مدة ${durationUs / 1000}ms")
            } else {
                // 2. Download translation & audio files, then transcode to AAC/M4A for 100% video muxing compatibility
                for (i in 0 until totalAyahs) {
                checkCancellationAndPause()
                val ayah = startAyah + i
                SystemDiagnosticTracker.addLog("AYAH_PROCESS", "=== بدء معالجة الآية رقم $ayah من السورة $surah ===")
                onProgress(if (isArabic) "جاري تحميل الآية $ayah وحفظ مراجع الصوت..." else "Downloading reference audio for Ayah $ayah...", 0.05f + (i * 0.2f / totalAyahs))
                
                SystemDiagnosticTracker.addLog("API_CALL", "طلب نص ورقم الآية $ayah بسند عثماني")
                val verseInfo = fetchVerseInfo(surah, ayah, "quran-uthmani")
                var text = verseInfo.first
                val globalAyahNumber = verseInfo.second
                var translation = if (showTranslation) {
                    SystemDiagnosticTracker.addLog("API_CALL", "طلب ترجمة الآية $ayah بالإنجليزية (Sahih International)")
                    fetchVerseInfo(surah, ayah, "en.sahih").first
                } else null

                // Strip / Clean prepended Basmalah from Ayah 1 text & translation of any surah (except 1 and 9)
                if (surah != 1 && surah != 9 && ayah == 1) {
                    val standardBasmalah = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                    text = text.trim()
                    if (text.startsWith(standardBasmalah)) {
                        text = text.substring(standardBasmalah.length).trim()
                    } else {
                        val keywords = listOf("بِسْمِ اللَّهِ", "بِسْمِ اللهِ", "بِسْمِ")
                        for (kw in keywords) {
                            if (text.startsWith(kw)) {
                                val index = text.indexOf("الرَّحِيمِ")
                                if (index != -1 && index < 60) {
                                    text = text.substring(index + "الرَّحِيمِ".length).trim()
                                    break
                                }
                                val index2 = text.indexOf("الرَّحِيْمِ")
                                if (index2 != -1 && index2 < 60) {
                                    text = text.substring(index2 + "الرَّحِيْمِ".length).trim()
                                    break
                                }
                            }
                        }
                    }

                    text += formatAyahSymbol(ayah)

                    if (translation != null) {
                        val basmalahEnglishList = listOf(
                            "In the name of God, the Most Gracious, the Most Merciful",
                            "In the name of God, Most Gracious, Most Merciful",
                            "In the name of Allah, the Entirely Merciful, the Especially Merciful",
                            "In the name of Allah, the Beneficent, the Merciful",
                            "In the name of Allah, the Compassionate, the Merciful",
                            "In the name of God, the Compassionate, the Merciful"
                        )
                        var cleanTrans = translation.trim()
                        for (b in basmalahEnglishList) {
                            if (cleanTrans.lowercase().startsWith(b.lowercase())) {
                                cleanTrans = cleanTrans.substring(b.length).trim()
                                if (cleanTrans.startsWith("-") || cleanTrans.startsWith(".") || cleanTrans.startsWith(":") || cleanTrans.startsWith(",")) {
                                    cleanTrans = cleanTrans.substring(1).trim()
                                }
                                break
                            }
                        }
                        translation = cleanTrans
                    }
                }

                val audioFileName = "${actualReciterId}_${surah}_${ayah}.mp3"
                val url = "https://cdn.islamic.network/quran/audio/64/$actualReciterId/$globalAyahNumber.mp3"
                val destFile = File(context.cacheDir, audioFileName)
                
                val cacheKeyUrl = url
                val cached = AlignmentCacheManager.getCachedAlignment(context, cacheKeyUrl, text)
                
                var aacFile: File? = null
                var durationUs: Long = 0
                var timeline: List<Pair<Long, Float>> = emptyList()
                var alignedSegments: List<WordSegment> = emptyList()
                var smartChunks: List<SmartChunk> = emptyList()

                if (cached != null && cached.audioPath != null && File(cached.audioPath).exists()) {
                    SystemDiagnosticTracker.addLog("CACHE", "تم استرجاع معلومات الموائمة والملف الصوتي للآية $ayah من الذاكرة المؤقتة")
                    aacFile = File(cached.audioPath)
                    alignedSegments = cached.wordSegments
                    smartChunks = cached.smartChunks
                    
                    val ext = MediaExtractor().apply { setDataSource(aacFile.absolutePath) }
                    ext.selectTrack(0)
                    durationUs = ext.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION, -1L)
                    if (durationUs <= 0) {
                        var maxTs = 0L
                        val bb = ByteBuffer.allocate(256)
                        while (ext.readSampleData(bb, 0) >= 0) {
                            maxTs = ext.sampleTime
                            ext.advance()
                        }
                        durationUs = maxTs
                    }
                    ext.release()
                    timeline = emptyList()
                } else {
                    SystemDiagnosticTracker.addLog("DOWNLOAD", "تحميل تلاوة الآية $ayah من الرابط: $url")
                    downloadAudio(url, destFile)
                    SystemDiagnosticTracker.addLog("DOWNLOAD", "تم تحميل تلاوة الآية $ayah بنجاح، الحجم: ${destFile.length()} بايت")
                    
                    onProgress(if (isArabic) "جاري ترميز ملف الصوت بدقة سينمائية..." else "Encoding audio block dynamically...", 0.12f + (i * 0.2f / totalAyahs))
                    val aacFileName = "${actualReciterId}_${surah}_${ayah}_transcoded.m4a"
                    aacFile = File(context.cacheDir, aacFileName)
                    SystemDiagnosticTracker.addLog("TRANSCODE", "تحويل ترميز الملف الصوتي للآية $ayah إلى AAC سينمائي")
                    timeline = transcodeMp3ToAac(destFile.absolutePath, aacFile.absolutePath)
                    SystemDiagnosticTracker.addLog("TRANSCODE", "تم تحويل ترميز ملف الآية $ayah")

                    SystemDiagnosticTracker.addLog("ALIGNMENT", "بدء المواءمة بالذكاء الاصطناعي WhisperX للآية $ayah")
                    onProgress(if (isArabic) "جاري مواءمة الكلمات بالذكاء الاصطناعي (WhisperX)..." else "Aligning word timings with WhisperX AI...", 0.07f + (i * 0.2f / totalAyahs))
                    val alignedData = alignWithWhisperX(context, aacFile, null, text)
                    alignedSegments = alignedData.first
                    val whisperXChunks = alignedData.second
                    SystemDiagnosticTracker.addLog("ALIGNMENT", "تمت مواءمة الآية $ayah بالكامل بنجاح. عدد الكلمات المسترجعة: ${alignedSegments.size}")
                    
                    val ext = MediaExtractor().apply { setDataSource(aacFile.absolutePath) }
                    ext.selectTrack(0)
                    durationUs = ext.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION, -1L)
                    if (durationUs <= 0) {
                        var maxTs = 0L
                        val bb = ByteBuffer.allocate(256)
                        while (ext.readSampleData(bb, 0) >= 0) {
                            maxTs = ext.sampleTime
                            ext.advance()
                        }
                        durationUs = maxTs
                    }
                    ext.release()
                    
                    val durationMs = durationUs / 1000
                    SystemDiagnosticTracker.addLog("CHUNKS", "تقسيم الآية $ayah إلى Smart Chunks للمزامنة البصرية")
                    smartChunks = getSmartChunks(context, text, translation, alignedSegments, whisperXChunks, durationMs)
                    
                    AlignmentCacheManager.putCachedAlignment(context, cacheKeyUrl, text, alignedSegments, smartChunks, aacFile)
                }
                
                verses.add(VerseData(verseInfo.third, text, translation, aacFile!!.absolutePath, durationUs, timeline, alignedSegments, smartChunks))
                SystemDiagnosticTracker.addLog("AYAH_PROCESS", "اكتملت معالجة الآية $ayah بنجاح.")
            }
            }
            
            checkCancellationAndPause()
            
            // Save timeline for editor preview
            try {
                val timelineArray = org.json.JSONArray()
                var currentOffsetMs = 0L
                for (v in verses) {
                    for (c in v.chunks) {
                        val obj = org.json.JSONObject()
                        obj.put("arabic", c.arabic)
                        obj.put("english", c.english ?: "")
                        obj.put("startTimeMs", c.startTimeMs + currentOffsetMs)
                        obj.put("endTimeMs", c.endTimeMs + currentOffsetMs)
                        obj.put("surahName", v.surahName)
                        timelineArray.put(obj)
                    }
                    currentOffsetMs += (v.durationUs / 1000)
                }
                val timelineFile = File(context.cacheDir, "reel_timeline.json")
                timelineFile.writeText(timelineArray.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 3. Fetch Cinematic Background Portrait Video clip if Pexels or Pixabay API key is provided
            var videoLoaded = false
            val downloadedVideoFiles = mutableListOf<File>()
            
            checkCancellationAndPause()
            
            if (!isRetry) {
                try {
                    val files = context.cacheDir.listFiles()
                    files?.forEach { f ->
                        if (f.name.startsWith("bg_video_") && f.name.endsWith(".mp4")) {
                            f.delete()
                        }
                    }
                } catch (ex: Exception) {}
            }
            
            if (isRetry) {
                try {
                    val files = context.cacheDir.listFiles()
                    val bgFiles = files?.filter { it.name.startsWith("bg_video_") && it.name.endsWith(".mp4") && it.length() > 0 }
                        ?.sortedBy {
                            val numMatch = Regex("\\d+").find(it.name)
                            numMatch?.value?.toIntOrNull() ?: 0
                        }
                    if (bgFiles != null && bgFiles.isNotEmpty()) {
                        downloadedVideoFiles.addAll(bgFiles)
                        videoLoaded = true
                    }
                } catch (ex: Exception) {}
            }
            
            if (!videoLoaded && pexelsApiKey.isNotBlank()) {
                onProgress(if (isArabic) "جاري البحث عن مشاهد سينمائية سريعة (Pexels)..." else "Searching for dynamic fast-paced cinematic scenes (Pexels)...", 0.3f)
                try {
                    val pexelsQueries = if (backgroundKeywords.isNotEmpty()) backgroundKeywords.toList() else listOf(
                        "islamic+aesthetics+kaaba+mecca",
                        "dark+cinematic+aesthetic+landscape",
                        "stormy+aesthetic+rainy+window",
                        "moonlight+trees+dark+night",
                        "epic+sunset+clouds+aesthetic",
                        "snowy+mountains+cinematic",
                        "rain+flowers+nature+aesthetic",
                        "sunset+bike+nature+dark"
                    )
                    val chosenQuery = if (!videoQuery.isNullOrBlank()) videoQuery else pexelsQueries.random().replace(" ", "+")
                    val requestUrl = "https://api.pexels.com/videos/search?query=$chosenQuery&orientation=portrait&per_page=30"
                    val request = Request.Builder()
                        .url(requestUrl)
                        .addHeader("Authorization", pexelsApiKey)
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        val json = JSONObject(body)
                        val videos = json.getJSONArray("videos")
                        if (videos.length() > 0) {
                            val availableVideosList = mutableListOf<JSONObject>()
                            for (vIdx in 0 until videos.length()) {
                                availableVideosList.add(videos.getJSONObject(vIdx))
                            }
                            
                            val numBackgroundVideos = if (isPopularUrlDownload && verses.isNotEmpty()) verses[0].chunks.size else verses.size
                            for (vidIdx in 0 until numBackgroundVideos) {
                                val neededDurSec = if (isPopularUrlDownload) {
                                    val chunk = verses[0].chunks[vidIdx]
                                    ((chunk.endTimeMs - chunk.startTimeMs) / 1000L).toInt() + 2
                                } else {
                                    (verses[vidIdx].durationUs / 1000000L).toInt() + 2
                                }
                                
                                // Try to find a video with duration >= neededDurSec, otherwise find the longest video
                                var selectedVideoJson = availableVideosList.filter {
                                    it.optInt("duration", 0) >= neededDurSec
                                }.minByOrNull { it.optInt("duration", 0) }
                                
                                if (selectedVideoJson == null) {
                                    selectedVideoJson = availableVideosList.maxByOrNull { it.optInt("duration", 0) }
                                }
                                
                                if (selectedVideoJson != null) {
                                    // Remove selected video from lists to maintain variety
                                    if (availableVideosList.size > 1) {
                                        availableVideosList.remove(selectedVideoJson)
                                    }
                                    
                                    val videoFiles = selectedVideoJson.getJSONArray("video_files")
                                    var highestResUrl: String? = null
                                    
                                    val mp4Files = mutableListOf<JSONObject>()
                                    for(v in 0 until videoFiles.length()) {
                                        val f = videoFiles.getJSONObject(v)
                                        if (f.getString("link").contains("mp4", ignoreCase = true)) {
                                            mp4Files.add(f)
                                        }
                                    }
                                    
                                    val portraitFiles = mp4Files.filter { it.optInt("width", 0) < it.optInt("height", 0) }
                                    val targetList = if(portraitFiles.isNotEmpty()) portraitFiles else mp4Files
                                    
                                    if (targetList.isNotEmpty()) {
                                        val sortedFiles = targetList.sortedBy { it.optInt("width", 0) * it.optInt("height", 0) }
                                        highestResUrl = when(videoQuality) {
                                            "Normal" -> {
                                                // Try to pick a medium-low one, bounded so it's not absolutely terrible.
                                                val idx = (sortedFiles.size * 0.25).toInt()
                                                sortedFiles[idx.coerceAtMost(sortedFiles.lastIndex)].getString("link")
                                            }
                                            "High" -> {
                                                // Try to pick a medium-high one.
                                                val idx = (sortedFiles.size * 0.6).toInt()
                                                sortedFiles[idx.coerceAtMost(sortedFiles.lastIndex)].getString("link")
                                            }
                                            else -> {
                                                // "Ultra" - Maximum resolution
                                                sortedFiles.last().getString("link")
                                            }
                                        }
                                    } else if (videoFiles.length() > 0) {
                                        highestResUrl = videoFiles.getJSONObject(0).getString("link")
                                    }
                                    
                                    var selectedVideoUrl: String? = highestResUrl
                                    
                                    if (selectedVideoUrl != null) {
                                        onProgress(
                                            if (isArabic) "جاري تحميل مشهد متناسق للمقطع ${vidIdx + 1} من ${verses.size}..." else "Downloading duration-matched scene ${vidIdx + 1} of ${verses.size}...",
                                            0.35f + (vidIdx * 0.15f / verses.size)
                                        )
                                        val targetFile = File(context.cacheDir, "bg_video_$vidIdx.mp4")
                                        downloadAudio(selectedVideoUrl, targetFile)
                                        downloadedVideoFiles.add(targetFile)
                                    }
                                }
                            }
                            if (downloadedVideoFiles.isNotEmpty()) {
                                videoLoaded = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            if (!videoLoaded && pixabayApiKey.isNotBlank()) {
                onProgress(if (isArabic) "جاري البحث عن مناظر طبيعية هادئة سريعة (Pixabay)..." else "Searching for active nature landscapes (Pixabay)...", 0.3f)
                try {
                    val pixabayQueries = if (backgroundKeywords.isNotEmpty()) backgroundKeywords.toList() else listOf(
                        "islamic+aesthetics",
                        "dark+cinematic+aesthetic+landscape",
                        "stormy+aesthetic+rain",
                        "moonlight+trees+dark+night",
                        "epic+sunset+clouds+aesthetic",
                        "snowy+mountains+cinematic",
                        "rain+nature+aesthetic",
                        "sunset+bike+nature+dark"
                    )
                    val chosenPixabayQuery = if (!videoQuery.isNullOrBlank()) videoQuery else pixabayQueries.random().replace(" ", "+")
                    val request = Request.Builder()
                        .url("https://pixabay.com/api/videos/?key=$pixabayApiKey&q=$chosenPixabayQuery&orientation=vertical&per_page=30")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        val json = JSONObject(body)
                        val hits = json.getJSONArray("hits")
                        if (hits.length() > 0) {
                            val availableHitsList = mutableListOf<JSONObject>()
                            for (hIdx in 0 until hits.length()) {
                                availableHitsList.add(hits.getJSONObject(hIdx))
                            }
                            
                            val numBackgroundVideos = if (isPopularUrlDownload && verses.isNotEmpty()) verses[0].chunks.size else verses.size
                            for (vidIdx in 0 until numBackgroundVideos) {
                                val neededDurSec = if (isPopularUrlDownload) {
                                    val chunk = verses[0].chunks[vidIdx]
                                    ((chunk.endTimeMs - chunk.startTimeMs) / 1000L).toInt() + 2
                                } else {
                                    (verses[vidIdx].durationUs / 1000000L).toInt() + 2
                                }
                                
                                var selectedHit = availableHitsList.filter {
                                    it.optInt("duration", 0) >= neededDurSec
                                }.minByOrNull { it.optInt("duration", 0) }
                                
                                if (selectedHit == null) {
                                    selectedHit = availableHitsList.maxByOrNull { it.optInt("duration", 0) }
                                }
                                
                                if (selectedHit != null) {
                                    if (availableHitsList.size > 1) {
                                        availableHitsList.remove(selectedHit)
                                    }
                                    
                                    val videosObj = selectedHit.getJSONObject("videos")
                                    val sizeKeys = when (videoQuality) {
                                        "Normal" -> listOf("small", "tiny", "medium", "large")
                                        "High" -> listOf("medium", "small", "large", "tiny")
                                        else -> listOf("large", "medium", "small", "tiny")
                                    }
                                    var selectedVideoUrl: String? = null
                                    for (key in sizeKeys) {
                                        if (videosObj.has(key)) {
                                            val vObj = videosObj.getJSONObject(key)
                                            val url = vObj.getString("url")
                                            if (url.isNotBlank()) {
                                                selectedVideoUrl = url
                                                break
                                            }
                                        }
                                    }
                                    if (selectedVideoUrl != null) {
                                        onProgress(
                                            if (isArabic) "جاري تحميل مشهد متناسق للمقطع ${vidIdx + 1} من ${verses.size}..." else "Downloading duration-matched scene ${vidIdx + 1} of ${verses.size}...",
                                            0.35f + (vidIdx * 0.15f / verses.size)
                                        )
                                        val targetFile = File(context.cacheDir, "bg_video_$vidIdx.mp4")
                                        downloadAudio(selectedVideoUrl, targetFile)
                                        downloadedVideoFiles.add(targetFile)
                                    }
                                }
                            }
                            if (downloadedVideoFiles.isNotEmpty()) {
                                videoLoaded = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Fallback to high-quality direct public video CDN loop URLs so we NEVER show a blank background or static image
            if (!videoLoaded) {
                checkCancellationAndPause()
                onProgress(if (isArabic) "جاري تحميل مشاهد طبيعية متحركة عالية الجودة..." else "Downloading premium cinematic video loops...", 0.3f)
                val directUrls = listOf(
                    "https://assets.mixkit.co/videos/preview/mixkit-vertical-shot-of-a-beautiful-waterfall-in-a-forest-43756-large.mp4",
                    "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-vertical-shot-44445-large.mp4",
                    "https://assets.mixkit.co/videos/preview/mixkit-waves-crashing-on-a-sandy-beach-from-above-41793-large.mp4",
                    "https://assets.mixkit.co/videos/preview/mixkit-vertical-shot-of-the-sea-under-a-clear-sky-40767-large.mp4",
                    "https://assets.mixkit.co/videos/preview/mixkit-light-rain-falling-on-green-leaves-vertical-shot-42022-large.mp4"
                )
                val targetCount = if (isPopularUrlDownload && verses.isNotEmpty()) verses[0].chunks.size else verses.size
                val countToLoad = Math.min(targetCount, directUrls.size)
                for (vidIdx in 0 until countToLoad) {
                    try {
                        onProgress(
                            if (isArabic) "جاري تحميل مشهد سينمائي عالي الجودة ${vidIdx + 1} من $countToLoad..." else "Loading cinematic nature loop ${vidIdx + 1} of $countToLoad...",
                            0.35f + (vidIdx * 0.15f / countToLoad)
                        )
                        val targetFile = File(context.cacheDir, "bg_video_$vidIdx.mp4")
                        downloadAudio(directUrls[vidIdx], targetFile)
                        if (targetFile.exists() && targetFile.length() > 0) {
                            downloadedVideoFiles.add(targetFile)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (downloadedVideoFiles.isNotEmpty()) {
                    videoLoaded = true
                }
            }
            
            
            checkCancellationAndPause()
            
            onProgress(if (isArabic) "جاري تهيئة معالجات المقطع..." else "Initializing video filters...", 0.5f)
            
            if (verses.isEmpty()) throw Exception("لا توجد آيات صالحة لعمل المقطع")
            
            val outputPath = File(context.cacheDir, "quran_reel_${System.currentTimeMillis()}.mp4").absolutePath
            val finalMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = finalMuxer
            
            var videoTrackIdx = -1
            var audioTrackIdx = -1
            var lastWrittenVideoPts = -1L
            val muxerStarted = java.util.concurrent.atomic.AtomicBoolean(false)
            
            val audioFormat = try {
                val tempExt = MediaExtractor()
                tempExt.setDataSource(verses[0].audioPath)
                tempExt.selectTrack(0)
                val format = tempExt.getTrackFormat(0)
                tempExt.release()
                format
            } catch (e: Exception) {
                SystemDiagnosticTracker.addLog("EXTRACTOR_ERROR", "فشل قراءة الملف الصوتي: ${verses[0].audioPath}")
                throw Exception("فشل تهيئة قارئ الصوت للملف: ${verses[0].audioPath}. ${e.message}")
            }
            
            val videoResString = settingsManager.videoResolution.first()
            var vidWidth = 720
            var vidHeight = 1280
            var vidBitrate = 2000000
            when (videoResString) {
                "720p" -> { vidWidth = 720; vidHeight = 1280; vidBitrate = 2000000 }
                "1080p" -> { vidWidth = 1080; vidHeight = 1920; vidBitrate = 4000000 }
                "1440p" -> { vidWidth = 1440; vidHeight = 2560; vidBitrate = 8000000 }
                "2160p" -> { vidWidth = 2160; vidHeight = 3840; vidBitrate = 16000000 }
            }
            
            val fpsVal = settingsManager.videoFps.first()
            
            val videoFormat = MediaFormat.createVideoFormat("video/avc", vidWidth, vidHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, vidBitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fpsVal)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            
            val encoder = MediaCodec.createEncoderByType("video/avc")
            videoCodec = encoder
            encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
            
            val drainLatch = CountDownLatch(1)
            
            val drainThread = thread {
                try {
                    val bufferInfo = MediaCodec.BufferInfo()
                    while (threadError == null) {
                        val outIdx = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                        if (outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            val vf = encoder.outputFormat
                            
                            // Build a clean audio format container containing only keys supported by MediaMuxer
                            val cleanAudioFormat = MediaFormat.createAudioFormat(
                                audioFormat.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm",
                                audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                                audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            )
                            if (audioFormat.containsKey("csd-0")) {
                                cleanAudioFormat.setByteBuffer("csd-0", audioFormat.getByteBuffer("csd-0")!!)
                            }
                            if (audioFormat.containsKey("csd-1")) {
                                cleanAudioFormat.setByteBuffer("csd-1", audioFormat.getByteBuffer("csd-1")!!)
                            }

                            videoTrackIdx = finalMuxer.addTrack(vf)
                            audioTrackIdx = finalMuxer.addTrack(cleanAudioFormat)
                            finalMuxer.start()
                            muxerStarted.set(true)
                        } else if (outIdx >= 0) {
                            val buf = encoder.getOutputBuffer(outIdx)!!
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0
                            }
                            if (bufferInfo.size > 0 && muxerStarted.get()) {
                                buf.position(bufferInfo.offset)
                                buf.limit(bufferInfo.offset + bufferInfo.size)
                                synchronized(finalMuxer) {
                                    if (bufferInfo.presentationTimeUs < 0L) bufferInfo.presentationTimeUs = 0L
                                    if (bufferInfo.presentationTimeUs <= lastWrittenVideoPts) {
                                        bufferInfo.presentationTimeUs = lastWrittenVideoPts + 10L
                                    }
                                    lastWrittenVideoPts = bufferInfo.presentationTimeUs
                                    try {
                                        finalMuxer.writeSampleData(videoTrackIdx, buf, bufferInfo)
                                    } catch (e: Exception) {
                                        SystemDiagnosticTracker.addLog("TRANSCODE_WARN", "Video muxer error: ${e.message}")
                                    }
                                }
                            }
                            encoder.releaseOutputBuffer(outIdx, false)
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    threadError = e
                    e.printStackTrace()
                } finally {
                    drainLatch.countDown()
                }
            }
            
            val verseStartTimestampsUs = mutableListOf<Long>()
            var currentStartUs = 0L
            for (verse in verses) {
                verseStartTimestampsUs.add(currentStartUs)
                val frames = Math.round(verse.durationUs.toDouble() / (1000000L / 15).toDouble()).toInt().coerceAtLeast(1)
                currentStartUs += frames * (1000000L / 15)
            }
            
            val audioThread = thread {
                try {
                    var lastWrittenPtsForTrack = -1L
                    for ((idx, verse) in verses.withIndex()) {
                        if (threadError != null) break
                        val audioPtsUs = verseStartTimestampsUs[idx]
                        val ext = MediaExtractor().apply { setDataSource(verse.audioPath) }
                        ext.selectTrack(0)
                        val buf = ByteBuffer.allocate(1024 * 1024)
                        val info = MediaCodec.BufferInfo()
                        
                        while (threadError == null) {
                            val size = ext.readSampleData(buf, 0)
                            if (size < 0) break
                            val pts = ext.sampleTime
                            info.offset = 0
                            info.size = size
                            info.flags = if ((ext.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                                MediaCodec.BUFFER_FLAG_KEY_FRAME
                            } else 0
                            
                            var targetPts = audioPtsUs + pts
                            if (idx > 0 && targetPts <= lastWrittenPtsForTrack) {
                                targetPts = lastWrittenPtsForTrack + 500L
                            }
                            lastWrittenPtsForTrack = targetPts
                            info.presentationTimeUs = targetPts
                            
                            while (!muxerStarted.get() && drainLatch.count > 0 && threadError == null) {
                                if (com.example.service.VideoGenerationService.isCancelled) {
                                    threadError = kotlinx.coroutines.CancellationException("Cancelled")
                                    break
                                }
                                Thread.sleep(10)
                            }
                            if (threadError != null) break
                            if (muxerStarted.get()) {
                                buf.position(0)
                                buf.limit(size)
                                synchronized(finalMuxer) {
                                    if (info.presentationTimeUs < 0) info.presentationTimeUs = 0L
                                    try {
                                        finalMuxer.writeSampleData(audioTrackIdx, buf, info)
                                    } catch (e: Exception) {
                                        SystemDiagnosticTracker.addLog("TRANSCODE_WARN", "Audio muxer error: ${e.message}")
                                    }
                                }
                            }
                            ext.advance()
                        }
                        ext.release()
                    }
                } catch (e: Exception) {
                    threadError = e
                    e.printStackTrace()
                }
            }
            
            val fps = 15
            val frameDurationUs = 1000000L / fps
            
            for ((idx, verse) in verses.withIndex()) {
                onProgress(if (isArabic) "جاري تصوير مشهدي الآية ${startAyah + idx}..." else "Rendering scenes for Ayah ${startAyah + idx}...", 0.5f + (idx * 0.4f / verses.size))
                
                var frameDecoder: SequentialFrameDecoder? = null
                var lastVideoIdx = -1
                
                val framesNeeded = Math.round(verse.durationUs.toDouble() / frameDurationUs.toDouble()).toInt().coerceAtLeast(1)
                val verseStartPts = verseStartTimestampsUs[idx]
                
                for (i in 0 until framesNeeded) {
                    checkCancellationAndPause()
                    if (threadError != null) {
                        throw Exception("خطأ في قنوات المعالجة الخلفية: ${threadError?.localizedMessage}")
                    }
                    
                    if (i % 30 == 0 && i > 0) {
                        val baseProgress = 0.5f + (idx * 0.4f / verses.size)
                        val frameProgress = (i.toFloat() / framesNeeded.toFloat()) * (0.4f / verses.size)
                        onProgress(if (isArabic) "تصوير الآية ${startAyah + idx} (${(i * 100 / framesNeeded)}%)..." else "Rendering Ayah ${startAyah + idx} (${(i * 100 / framesNeeded)}%)...", baseProgress + frameProgress)
                    }
                    
                    val currentFramePts = verseStartPts + i * frameDurationUs
                    val frameIndex = currentFramePts / frameDurationUs
                    val currentTimeMs = (i * frameDurationUs) / 1000
                    val activeChunk = getActiveSmartChunk(verse.chunks, currentTimeMs)
                    val chunkIdx = if (activeChunk != null) verse.chunks.indexOf(activeChunk).coerceAtLeast(0) else 0
                    
                    // If verses.size == 1 (Popular clip), we switch video by chunk. Otherwise by verse index.
                    val videoIdx = if (verses.size <= 1) chunkIdx else idx
                    
                    if (videoIdx != lastVideoIdx) {
                        lastVideoIdx = videoIdx
                        if (videoLoaded && downloadedVideoFiles.isNotEmpty()) {
                            try {
                                frameDecoder?.release()
                                val videoFile = downloadedVideoFiles[videoIdx % downloadedVideoFiles.size]
                                if (videoFile.exists()) {
                                    frameDecoder = SequentialFrameDecoder(videoFile.absolutePath)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    
                    var bgFrameBitmap: Bitmap? = null
                    if (frameDecoder != null) {
                        try {
                            bgFrameBitmap = frameDecoder?.getNextFrame()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    val chunkedText = if (verse.chunks.isNotEmpty() && activeChunk == null) "" else activeChunk?.arabic ?: verse.text
                    val chunkedTranslation = if (verse.chunks.isNotEmpty() && activeChunk == null) "" else if (activeChunk != null) {
                        if (activeChunk.english.isNullOrBlank()) verse.translation else activeChunk.english
                    } else {
                        verse.translation
                    }
                    
                    val chunkTimeMs = currentTimeMs - (activeChunk?.startTimeMs?.toLong() ?: 0L)
                    
                    val bitmap = createVerseBitmap(
                        surahName = verse.surahName,
                        text = chunkedText,
                        translation = chunkedTranslation,
                        bgBitmap = bgFrameBitmap,
                        context = context,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        textFontSize = textFontSize,
                        textColorStr = textColorStr,
                        textOpacity = textOpacity,
                        showTextBg = showTextBg,
                        textBgColorStr = textBgColorStr,
                        textBgOpacity = textBgOpacity,
                        textBgRadius = textBgRadius,
                        textPosition = textPosition,
                        textAlign = textAlign,
                        textAnimationType = textAnimationType,
                        translationFontSize = translationFontSize,
                        translationColorStr = translationColorStr,
                        translationFontFamily = translationFontFamily,
                        translationFontWeight = translationFontWeight,
                        translationTextX = translationTextX,
                        translationTextY = translationTextY,
                        arabicTextX = arabicTextX,
                        arabicTextY = arabicTextY,
                        surahNameFontSize = surahNameFontSize,
                        surahNameColorStr = surahNameColorStr,
                        surahNameOpacity = surahNameOpacity,
                        surahNameX = surahNameX,
                        surahNameY = surahNameY,
                        iconOpacity = iconOpacity,
                        iconX = iconX,
                        iconY = iconY,
                        frameIndex = frameIndex,
                        chunkTimeMs = chunkTimeMs,
                        isPreviewMode = isPreviewMode,
                        videoWidth = vidWidth,
                        videoHeight = vidHeight
                    )
                    
                    var inIdx = -1
                    while (inIdx < 0) {
                        if (threadError != null) {
                            throw Exception("خطأ في قنوات المعالجة الخلفية: ${threadError?.localizedMessage}")
                        }
                        inIdx = encoder.dequeueInputBuffer(50000)
                    }
                    
                    val img = encoder.getInputImage(inIdx)!!
                    fillImageFromBitmap(img, bitmap)
                    encoder.queueInputBuffer(inIdx, 0, img.planes[0].buffer.capacity() * 3/2, currentFramePts, 0)
                    
                    bitmap.recycle()
                    bgFrameBitmap?.recycle()
                }
                
                try { frameDecoder?.release() } catch (ex: Exception) {}
            }
            
            var eosIdx = -1
            while (eosIdx < 0) {
                if (threadError != null) {
                    throw Exception("خطأ في قنوات المعالجة الخلفية: ${threadError?.localizedMessage}")
                }
                eosIdx = encoder.dequeueInputBuffer(50000)
            }
            val totalReelDurationUs = verseStartTimestampsUs.last() + Math.round(verses.last().durationUs.toDouble() / frameDurationUs.toDouble()).toInt().coerceAtLeast(1) * frameDurationUs
            encoder.queueInputBuffer(eosIdx, 0, 0, totalReelDurationUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            
            val drainCompleted = drainLatch.await(5, TimeUnit.MINUTES)
            if (!drainCompleted) {
                throw Exception("توقيت معالجة الفيديو انتهى دون استجابة الترميز")
            }
            audioThread.join(10000)
            
            if (threadError != null) {
                throw Exception("فشلت معالجة مقطع الفيديو: ${threadError?.localizedMessage}")
            }
            
            finalMuxer.stop()
            finalMuxer.release()
            muxer = null
            
            encoder.stop()
            encoder.release()
            videoCodec = null
            
            bgBitmap?.recycle()
            bgBitmap = null
            
            onProgress(if (isArabic) "جاري تصدير المقطع وحفظه بالاستوديو..." else "Exporting video and registering in Gallery...", 0.95f)
            
            // ALWAYS create an internal playable file that ExoPlayer can always read without permission
            try {
                val playableFile = File(context.cacheDir, "playable_reel.mp4")
                File(outputPath).copyTo(playableFile, overwrite = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            var uri: Uri? = null
            
            if (isPreviewMode) {
                uri = Uri.fromFile(File(context.cacheDir, "playable_reel.mp4"))
            } else {
                // 1. Direct custom directory save attempt as requested: /storage/emulated/0/Quran Reels
                try {
                    val customDir = File("/storage/emulated/0/Quran Reels")
                if (!customDir.exists()) {
                    customDir.mkdirs()
                }
                if (customDir.exists()) {
                    val targetFile = File(customDir, "Quran_Reel_${System.currentTimeMillis()}.mp4")
                    File(outputPath).copyTo(targetFile, overwrite = true)
                    
                    // Crucial: Scan file so it is indexed in the system media database & instantly visible in standard players/gallery!
                    android.media.MediaScannerConnection.scanFile(
                        context,
                        arrayOf(targetFile.absolutePath),
                        arrayOf("video/mp4"),
                        null
                    )
                    
                    uri = Uri.fromFile(targetFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 2. Fallback to standard MediaStore registration if Scoped Storage blocks raw file creation (this is 100% reliable on Android 10+ and places it in Movies directory)
            if (uri == null) {
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, "Quran_Reel_${System.currentTimeMillis()}.mp4")
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Quran Reels")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Video.Media.IS_PENDING, 1)
                        }
                    }
                    val mUri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                    if (mUri != null) {
                        context.contentResolver.openOutputStream(mUri)?.use { out ->
                            File(outputPath).inputStream().use { input ->
                                input.copyTo(out)
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            values.clear()
                            values.put(MediaStore.Video.Media.IS_PENDING, 0)
                            context.contentResolver.update(mUri, values, null, null)
                        }
                        uri = mUri
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            } // Close the else branch for isPreviewMode
            
            // 3. Absolute Fallback to Internal Cache to prevent failures (guarantees we always have a valid URI)
            if (uri == null) {
                uri = Uri.fromFile(File(outputPath))
            } else if (!isPreviewMode) {
                // Delete temporary internal file after successful copy
                try { File(outputPath).delete() } catch (ex: Exception) {}
            }
            
            val finalUri = uri
            if (finalUri != null) {
                SystemDiagnosticTracker.addLog("PROCESS_SUCCESS", "تم إنتاج وحفظ مقطع الفيديو بنجاح! الرابط: $finalUri")
                withContext(Dispatchers.Main) { onComplete(finalUri) }
            } else {
                val err = "لم نتمكن من حفظ المقطع في المعرض."
                SystemDiagnosticTracker.addLog("ERROR", err)
                throw Exception(err)
            }
            
        } catch (e: kotlinx.coroutines.CancellationException) {
            SystemDiagnosticTracker.addLog("PROCESS_CANCEL", "تم إلغاء عملية المونتاج من قبل المستخدم")
            try {
                videoCodec?.stop()
                videoCodec?.release()
            } catch (ex: Exception) {}
            try {
                muxer?.stop()
                muxer?.release()
            } catch (ex: Exception) {}
            bgBitmap?.recycle()
            try { retriever?.release() } catch (ex: Exception) {}
            throw e
        } catch (e: Exception) {
            if (com.example.service.VideoGenerationService.isCancelled) {
                SystemDiagnosticTracker.addLog("PROCESS_CANCEL", "تم إلغاء عملية المونتاج من قبل المستخدم أثناء خطأ")
                throw kotlinx.coroutines.CancellationException("تم إلغاء عملية إنتاج الفيديو")
            }
            SystemDiagnosticTracker.addLog("PROCESS_CRASH", "فشل فادح في معالجة الفيديو: ${e.message}")
            e.printStackTrace()
            try {
                videoCodec?.stop()
                videoCodec?.release()
            } catch (ex: Exception) {}
            try {
                muxer?.stop()
                muxer?.release()
            } catch (ex: Exception) {}
            bgBitmap?.recycle()
            try { retriever?.release() } catch (ex: Exception) {}
            
            val errorMsg = e.message ?: "حدث خطأ غير معروف في صانع المقطع"
            withContext(Dispatchers.Main) { onError(errorMsg) }
        }
    }

    private fun fetchFullSurahText(surah: Int): String {
        val url = "https://api.alquran.cloud/v1/surah/$surah/quran-uthmani"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val json = org.json.JSONObject(response.body?.string() ?: "")
            val ayahs = json.getJSONObject("data").getJSONArray("ayahs")
            val sb = java.lang.StringBuilder()
            for (i in 0 until ayahs.length()) {
                var text = ayahs.getJSONObject(i).getString("text")
                if (surah != 1 && surah != 9 && i == 0) {
                     val standardBasmalah = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                     val keywords = listOf("بِسْمِ اللَّهِ", "بِسْمِ اللهِ", "بِسْمِ")
                     if (text.startsWith(standardBasmalah)) {
                         text = text.substring(standardBasmalah.length).trim()
                     } else {
                         for (kw in keywords) {
                             if (text.startsWith(kw)) {
                                 val index = text.indexOf("الرَّحِيمِ")
                                 if (index != -1 && index < 60) {
                                     text = text.substring(index + "الرَّحِيمِ".length).trim()
                                     break
                                 }
                                 val index2 = text.indexOf("الرَّحِيْمِ")
                                 if (index2 != -1 && index2 < 60) {
                                     text = text.substring(index2 + "الرَّحِيْمِ".length).trim()
                                     break
                                 }
                             }
                         }
                     }
                }
                sb.append(text).append(" ")
            }
            return sb.toString().trim()
        }
        return ""
    }

    private fun fetchVerseInfo(surah: Int, ayah: Int, edition: String): Triple<String, Int, String> {
        val url = "https://api.alquran.cloud/v1/ayah/$surah:$ayah/$edition"
        val request = Request.Builder().url(url).build()
        var body = ""
        var retries = 0
        while (retries < 3) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    retries++
                    Thread.sleep(2000)
                    continue
                }
                body = response.body?.string() ?: ""
                break
            } catch (e: Exception) {
                retries++
                if (retries >= 3) throw e
                Thread.sleep(2000)
            }
        }
        if (body.isEmpty()) throw Exception("فشل تحميل نصوص الآيات من الخادم")
        val json = JSONObject(body)
        val data = json.getJSONObject("data")
        val surahObj = data.getJSONObject("surah")
        val surahName = surahObj.getString("name")
        return Triple(data.getString("text"), data.getInt("number"), surahName)
    }

    private fun downloadAudio(url: String, destFile: File) {
        synchronized(destFile.absolutePath.intern()) {
            if (destFile.exists() && destFile.length() > 0) return
            
            val fixedUrl = url
                .replace(" ", "%20")
                .replace("#", "%23")
                .replace("|", "%7C")
                .replace("^", "%5E")
                .replace(">", "%3E")
                .replace("<", "%3C")
                .replace("\\", "%5C")
                .replace("{", "%7B")
                .replace("}", "%7D")
                .replace("[", "%5B")
                .replace("]", "%5D")
                
            val request = Request.Builder()
                .url(fixedUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Android VideoGenerator")
                .build()
            var retries = 0
            var lastErrorUrlCode = 0
            while (retries < 3) {
                checkCancellationAndPause()
                val tmpFile = File(destFile.absolutePath + ".tmp_${System.currentTimeMillis()}")
                try {
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        retries++
                        lastErrorUrlCode = response.code
                        Thread.sleep(3000)
                        continue
                    }
                    val contentType = response.body?.contentType()?.toString()?.lowercase() ?: ""
                    if (contentType.contains("text/html")) {
                        throw Exception("Server returned HTML instead of media file. Possible captcha or blocking.")
                    }
                    response.body?.byteStream()?.use { input ->
                        tmpFile.outputStream().use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } >= 0) {
                                checkCancellationAndPause()
                                output.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    if (tmpFile.exists() && tmpFile.length() > 0) {
                        tmpFile.renameTo(destFile)
                        return
                    }
                } catch (e: Exception) {
                    retries++
                    if (retries >= 3) throw Exception("فشل تحميل الملفات من الخادم بعد ٣ محاولات لـ $url: ${e.message}")
                    Thread.sleep(3000)
                } finally {
                    if (tmpFile.exists()) tmpFile.delete()
                }
            }
            throw Exception("الرابط غير متاح أو لا يمكن الوصول إليه. رمز الخطأ الأخير: $lastErrorUrlCode لـ $url")
        }
    }

    private fun checkCancellationAndPause() {
        if (com.example.service.VideoGenerationService.isCancelled) {
            throw kotlinx.coroutines.CancellationException("تم إلغاء عملية إنتاج الفيديو")
        }
        if (com.example.service.VideoGenerationService.isPaused) {
            synchronized(com.example.service.VideoGenerationService.pauseLock) {
                while (com.example.service.VideoGenerationService.isPaused && !com.example.service.VideoGenerationService.isCancelled) {
                    try {
                        com.example.service.VideoGenerationService.pauseLock.wait(100)
                    } catch (e: Exception) {}
                }
            }
            if (com.example.service.VideoGenerationService.isCancelled) {
                throw kotlinx.coroutines.CancellationException("تم إلغاء عملية إنتاج الفيديو")
            }
        }
    }

    private fun transcodeMp3ToAac(inputPath: String, outputPath: String, extractStartUs: Long? = null, extractEndUs: Long? = null): List<Pair<Long, Float>> {
        val rawEnergySamples = mutableListOf<Pair<Long, Float>>()
        val extractor = MediaExtractor().apply { setDataSource(inputPath) }
        if (extractor.trackCount == 0) {
            extractor.release()
            throw Exception("ملف الصوت فارغ أو غير صالح للاستخدام")
        }
        var audioTrackIdx = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) {
                audioTrackIdx = i
                break
            }
        }
        if (audioTrackIdx == -1) {
            extractor.release()
            throw Exception("لم يتم العثور على مسار صوتي صالح في الملف")
        }
        
        extractor.selectTrack(audioTrackIdx)
        if (extractStartUs != null) {
            extractor.seekTo(extractStartUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        }
        val inputFormat = extractor.getTrackFormat(audioTrackIdx)
        val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: "audio/mpeg"
        
        val isRawAudio = mime == "audio/raw" || mime == "audio/x-wav"
        
        // 1. Setup Decoder (Only if not raw)
        val decoder = if (!isRawAudio) MediaCodec.createDecoderByType(mime) else null
        if (decoder != null) {
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()
        }
        
        // 2. Setup Encoder (AAC matching source format)
        var sourceSampleRate = if (inputFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
        var sourceChannelCount = if (inputFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1
        
        val targetSampleRate = sourceSampleRate
        val targetChannelCount = sourceChannelCount
        
        val outputFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, targetSampleRate, targetChannelCount).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, 320000)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
        }
        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
        
        // 3. Setup Muxer
        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var outTrackIdx = -1
        
        val decoderBufferInfo = MediaCodec.BufferInfo()
        val encoderBufferInfo = MediaCodec.BufferInfo()
        
        var isExtractorEOS = false
        var isDecoderEOS = false
        var isEncoderEOS = false
        
        val timeoutUs = 5000L
        var muxerStarted = false
        
        var lastWrittenPts = -1L
        var firstPts = -1L

        while (!isEncoderEOS) {
            checkCancellationAndPause()
 
            // A. Read from extractor and feed decoder OR encoder directly
            if (!isExtractorEOS) {
                if (decoder != null) {
                    val inIdx = decoder.dequeueInputBuffer(timeoutUs)
                    if (inIdx >= 0) {
                        val buf = decoder.getInputBuffer(inIdx)!!
                        val size = extractor.readSampleData(buf, 0)
                        if (size < 0 || (extractEndUs != null && extractor.sampleTime > extractEndUs)) {
                            decoder.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isExtractorEOS = true
                        } else {
                            decoder.queueInputBuffer(inIdx, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                } else {
                    // Feed encoder directly from extractor for raw audio
                    val encInIdx = encoder.dequeueInputBuffer(timeoutUs)
                    if (encInIdx >= 0) {
                        val buf = encoder.getInputBuffer(encInIdx)!!
                        val size = extractor.readSampleData(buf, 0)
                        if (size < 0 || (extractEndUs != null && extractor.sampleTime > extractEndUs)) {
                            encoder.queueInputBuffer(encInIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isExtractorEOS = true
                            isDecoderEOS = true
                        } else {
                            val pts = extractor.sampleTime
                            encoder.queueInputBuffer(encInIdx, 0, size, pts, 0)
                            
                            // Capture Energy for raw audio here
                            try {
                                val bufferDuplicate = buf.duplicate()
                                bufferDuplicate.position(0)
                                bufferDuplicate.limit(size)
                                val shortArray = ShortArray(size / 2)
                                bufferDuplicate.asShortBuffer().get(shortArray)
                                var sum = 0.0
                                for (s in shortArray) { sum += s * s }
                                val rms = Math.sqrt(sum / shortArray.size).toFloat()
                                rawEnergySamples.add(Pair(pts, rms))
                            } catch (e: Exception) {}
                            
                            extractor.advance()
                        }
                    }
                }
            }
            
            // D. Helper to drain encoder out
            fun drainEncoder(isEnd: Boolean = false) {
                while (true) {
                    val encOutIdx = encoder.dequeueOutputBuffer(encoderBufferInfo, timeoutUs)
                    if (encOutIdx >= 0) {
                        val encBuf = encoder.getOutputBuffer(encOutIdx)!!
                        if ((encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            encoderBufferInfo.size = 0
                        }
                        if (encoderBufferInfo.size > 0 && outTrackIdx >= 0) {
                            encBuf.position(encoderBufferInfo.offset)
                            encBuf.limit(encoderBufferInfo.offset + encoderBufferInfo.size)
                            if (firstPts == -1L && encoderBufferInfo.presentationTimeUs > 0) {
                                firstPts = encoderBufferInfo.presentationTimeUs
                            }
                            if (firstPts != -1L) {
                                encoderBufferInfo.presentationTimeUs -= firstPts
                            }
                            if (encoderBufferInfo.presentationTimeUs < 0) {
                                encoderBufferInfo.presentationTimeUs = 0L
                            }
                            
                            if (encoderBufferInfo.presentationTimeUs <= lastWrittenPts) {
                                encoderBufferInfo.presentationTimeUs = lastWrittenPts + 10L
                            }
                            lastWrittenPts = encoderBufferInfo.presentationTimeUs
                            
                            try {
                                muxer.writeSampleData(outTrackIdx, encBuf, encoderBufferInfo)
                            } catch (e: Exception) {
                                SystemDiagnosticTracker.addLog("TRANSCODE_WARN", "تصحيح عينة مفقودة: ${e.message}")
                            }
                        }
                        if ((encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            isEncoderEOS = true
                        }
                        encoder.releaseOutputBuffer(encOutIdx, false)
                    } else if (encOutIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        outTrackIdx = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (encOutIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!isEnd) break
                    } else {
                        break
                    }
                }
            }

            // B. Decode output into encoder input (Only if decoder is used)
            if (decoder != null && !isDecoderEOS) {
                val outIdx = decoder.dequeueOutputBuffer(decoderBufferInfo, timeoutUs)
                if (outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val actualFormat = decoder.outputFormat
                    sourceSampleRate = actualFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    sourceChannelCount = actualFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                } else if (outIdx >= 0) {
                    val buf = decoder.getOutputBuffer(outIdx)!!
                    val size = decoderBufferInfo.size
                    
                    if (size > 0) {
                        // Amplitude Energy Analysis for precise timing
                        try {
                            val bufferDuplicate = buf.duplicate()
                            bufferDuplicate.position(decoderBufferInfo.offset)
                            bufferDuplicate.limit(decoderBufferInfo.offset + size)
                            
                            var sumOfAbs = 0L
                            var count = 0
                            while (bufferDuplicate.remaining() >= 2) {
                                val sample = bufferDuplicate.short
                                sumOfAbs += Math.abs(sample.toInt())
                                count++
                            }
                            val avgEnergy = if (count > 0) sumOfAbs.toFloat() / count else 0f
                            rawEnergySamples.add(Pair(decoderBufferInfo.presentationTimeUs, avgEnergy))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
 
                    var encInIdx = -1
                    while (encInIdx < 0 && !isDecoderEOS) {
                        checkCancellationAndPause()
                        encInIdx = encoder.dequeueInputBuffer(timeoutUs)
                        if (encInIdx < 0) {
                            // While waiting for an encoder input buffer, we should also drain the encoder output buffer to prevent a deadlock
                            drainEncoder(false)
                        }
                    }
                    
                    if (encInIdx >= 0) {
                        val encBuf = encoder.getInputBuffer(encInIdx)!!
                        encBuf.clear()
                        if (size > 0) {
                            val resampledBuffer = resamplePCM(
                                inputBuf = buf,
                                inputSize = size,
                                inputOffset = decoderBufferInfo.offset,
                                srcSampleRate = sourceSampleRate,
                                srcChannels = sourceChannelCount,
                                dstSampleRate = targetSampleRate,
                                dstChannels = targetChannelCount
                            )
                            encBuf.put(resampledBuffer)
                            
                            val flags = if ((decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                isDecoderEOS = true
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            } else 0
                            encoder.queueInputBuffer(encInIdx, 0, resampledBuffer.limit(), decoderBufferInfo.presentationTimeUs, flags)
                        } else {
                            val flags = if ((decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                isDecoderEOS = true
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            } else 0
                            encoder.queueInputBuffer(encInIdx, 0, 0, decoderBufferInfo.presentationTimeUs, flags)
                        }
                    }
                    decoder.releaseOutputBuffer(outIdx, false)
                }
            }
            
            // C. Encode output and write to muxer
            drainEncoder(false)
        }
        
        // Cleanup all
        try { decoder?.stop(); decoder?.release() } catch (e: Exception) {}
        try { encoder.stop(); encoder.release() } catch (e: Exception) {}
        try { if (muxerStarted) muxer.stop(); muxer.release() } catch (e: Exception) {}
        try { extractor.release() } catch (e: Exception) {}
 
        // Smooth energy with noise-gate threshold - lowered to 0.02f for maximum syllables tracking
        val peak = rawEnergySamples.maxOfOrNull { it.second } ?: 1f
        val gateThreshold = peak * 0.02f
        return rawEnergySamples.map { (time, energy) ->
            val gatedEnergy = if (energy > gateThreshold) energy - gateThreshold else 0f
            Pair(time, gatedEnergy)
        }
    }

    private fun getCumulativeEnergyRatio(
        currentFrame: Int,
        totalFrames: Int,
        durationUs: Long,
        timeline: List<Pair<Long, Float>>
    ): Float {
        if (timeline.isEmpty() || totalFrames <= 1) {
            return currentFrame.toFloat() / totalFrames.toFloat()
        }
        
        // Find peak energy to determine noise gate threshold to identify active speech boundaries
        val peak = timeline.maxOfOrNull { it.second } ?: 1f
        val activeThreshold = peak * 0.04f // 4% threshold for active voice detection
        
        val firstActiveIdx = timeline.indexOfFirst { it.second > activeThreshold }.coerceAtLeast(0)
        val lastActiveIdx = timeline.indexOfLast { it.second > activeThreshold }.coerceAtLeast(0).coerceAtMost(timeline.size - 1)
        
        val startSpeechUs = timeline[firstActiveIdx].first
        val endSpeechUs = timeline[lastActiveIdx].first
        val activeDurationUs = endSpeechUs - startSpeechUs
        
        val currentTimeUs = (currentFrame.toFloat() / totalFrames.toFloat()) * durationUs
        
        // Before active speech has begun
        if (currentTimeUs < startSpeechUs) {
            return 0.0f
        }
        // After active speech has concluded
        if (currentTimeUs > endSpeechUs) {
            return 1.0f
        }
        
        if (activeDurationUs <= 0L) {
            return (currentTimeUs - startSpeechUs).toFloat() / Math.max(1f, durationUs.toFloat())
        }
        
        var totalEnergy = 0f
        var cumulativeEnergy = 0f
        
        for (sample in timeline) {
            if (sample.first >= startSpeechUs && sample.first <= endSpeechUs) {
                totalEnergy += sample.second
                if (sample.first <= currentTimeUs) {
                    cumulativeEnergy += sample.second
                }
            }
        }
        
        return if (totalEnergy > 0f) {
            cumulativeEnergy / totalEnergy
        } else {
            (currentTimeUs - startSpeechUs).toFloat() / activeDurationUs.toFloat()
        }
    }

    private fun getActiveTextChunk(
        text: String, 
        currentFrame: Int, 
        totalFrames: Int,
        durationUs: Long,
        timeline: List<Pair<Long, Float>>,
        wordSegments: List<WordSegment>
    ): String {
        val rawWords = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val words = rawWords.map { 
            it.replace("ۗ", "")
              .replace("ۖ", "")
              .replace("ۚ", "")
              .replace("ۘ", "")
              .replace("ۙ", "")
              .replace("ۛ", "")
              .replace("۞", "")
              .replace("۩", "")
              .trim()
        }.filter { it.isNotBlank() }
        
        if (words.isEmpty()) return text
        
        // Let's group words into dynamic chunks of max 5 words to keep subtitles clean & legible
        val maxWordsInChunk = 5
        val chunks = mutableListOf<List<Int>>() 
        var currentChunk = mutableListOf<Int>()
        for (idx in words.indices) {
            currentChunk.add(idx)
            if (currentChunk.size >= maxWordsInChunk) {
                chunks.add(currentChunk)
                currentChunk = mutableListOf()
            }
        }
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk)
        }
        
        // Use original word segments sorted chronologically without subtracting firstStart offset
        val adjustedWordSegments = wordSegments.sortedBy { it.startTimeMs }
        
        fun getWordStartTime(wordIdx: Int): Long {
            val seg = adjustedWordSegments.find { it.wordIndex == wordIdx + 1 }
            if (seg != null) return seg.startTimeMs
            
            // Interpolation fallback if timing is missing
            if (wordIdx == 0) return 0L
            for (prevIdx in (wordIdx - 1) downTo 0) {
                val prevSeg = adjustedWordSegments.find { it.wordIndex == prevIdx + 1 }
                if (prevSeg != null) {
                    return prevSeg.endTimeMs + (wordIdx - prevIdx - 1) * 350L
                }
            }
            return 350L * wordIdx
        }
        
        val currentTimeMs = ((currentFrame.toFloat() / totalFrames.toFloat()) * durationUs) / 1000
        var activeChunkIdx = 0
        
        if (adjustedWordSegments.isNotEmpty()) {
            for (cIdx in chunks.indices) {
                val firstWordIdxInChunk = chunks[cIdx].first()
                val chunkStartMs = getWordStartTime(firstWordIdxInChunk)
                if (currentTimeMs >= chunkStartMs) {
                    activeChunkIdx = cIdx
                }
            }
        } else {
            // Predictable stable linear timing fallback (no freeze, no stuck)
            val ratio = currentFrame.toFloat() / totalFrames.toFloat()
            activeChunkIdx = (ratio * chunks.size).toInt().coerceIn(0, chunks.size - 1)
        }
        
        val activeChunkWordIndices = chunks[activeChunkIdx]
        val chunkWords = activeChunkWordIndices.map { words[it] }
        return chunkWords.joinToString(" ")
    }

    private fun getActiveTranslationChunk(
        translation: String?, 
        text: String, 
        currentFrame: Int, 
        totalFrames: Int,
        durationUs: Long,
        timeline: List<Pair<Long, Float>>,
        wordSegments: List<WordSegment>
    ): String? {
        if (translation == null) return null
        val rawWords = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val words = rawWords.map { 
            it.replace("ۗ", "")
              .replace("ۖ", "")
              .replace("ۚ", "")
              .replace("ۘ", "")
              .replace("ۙ", "")
              .replace("ۛ", "")
              .replace("۞", "")
              .replace("۩", "")
              .trim()
        }.filter { it.isNotBlank() }
        
        if (words.isEmpty()) return translation
        
        val transWords = translation.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (transWords.size <= 6) return translation
        
        // 1. Setup chunks layout
        val maxWordsInChunk = 5
        val chunksCount = Math.ceil(words.size.toDouble() / maxWordsInChunk.toDouble()).toInt().coerceAtLeast(1)
        
        // Split translation proportionally into chunksCount parts
        val wordsPerTransChunk = Math.ceil(transWords.size.toDouble() / chunksCount.toDouble()).toInt().coerceAtLeast(1)
        val transChunks = mutableListOf<String>()
        var tIdx = 0
        while (tIdx < transWords.size) {
            val end = Math.min(tIdx + wordsPerTransChunk, transWords.size)
            transChunks.add(transWords.subList(tIdx, end).joinToString(" "))
            tIdx += wordsPerTransChunk
        }
        
        // Use original word segments sorted chronologically without subtracting firstStart offset
        val adjustedWordSegments = wordSegments.sortedBy { it.startTimeMs }
        
        fun getWordStartTime(wordIdx: Int): Long {
            val seg = adjustedWordSegments.find { it.wordIndex == wordIdx + 1 }
            if (seg != null) return seg.startTimeMs
            
            // Interpolation
            if (wordIdx == 0) return 0L
            for (prevIdx in (wordIdx - 1) downTo 0) {
                val prevSeg = adjustedWordSegments.find { it.wordIndex == prevIdx + 1 }
                if (prevSeg != null) {
                    return prevSeg.endTimeMs + (wordIdx - prevIdx - 1) * 350L
                }
            }
            return 350L * wordIdx
        }
        
        // 3. Find active chunk index
        val currentTimeMs = ((currentFrame.toFloat() / totalFrames.toFloat()) * durationUs) / 1000
        var activeChunkIdx = 0
        
        if (adjustedWordSegments.isNotEmpty()) {
            for (cIdx in 0 until chunksCount) {
                val firstWordIdxInChunk = cIdx * maxWordsInChunk
                val firstWordIdxInChunkCoerced = firstWordIdxInChunk.coerceAtMost(words.size - 1)
                val chunkStartMs = getWordStartTime(firstWordIdxInChunkCoerced)
                if (currentTimeMs >= chunkStartMs) {
                    activeChunkIdx = cIdx
                }
            }
        } else {
            val ratio = currentFrame.toFloat() / totalFrames.toFloat()
            activeChunkIdx = (ratio * chunksCount).toInt().coerceIn(0, chunksCount - 1)
        }
        
        if (activeChunkIdx < transChunks.size) {
            return transChunks[activeChunkIdx]
        }
        return transChunks.lastOrNull() ?: translation
    }

    private fun createVerseBitmap(
        surahName: String,
        text: String,
        translation: String?,
        bgBitmap: Bitmap?,
        context: Context,
        fontFamily: String,
        fontWeight: String,
        textFontSize: Int,
        textColorStr: String,
        textOpacity: Float,
        showTextBg: Boolean,
        textBgColorStr: String,
        textBgOpacity: Float,
        textBgRadius: Int,
        textPosition: String,
        textAlign: String,
        textAnimationType: String,
        translationFontSize: Int,
        translationColorStr: String,
        translationFontFamily: String,
        translationFontWeight: String,
        translationTextX: Int,
        translationTextY: Int,
        arabicTextX: Int,
        arabicTextY: Int,
        surahNameFontSize: Int,
        surahNameColorStr: String,
        surahNameOpacity: Float,
        surahNameX: Int,
        surahNameY: Int,
        iconOpacity: Float,
        iconX: Int,
        iconY: Int,
        frameIndex: Long,
        chunkTimeMs: Long,
        isPreviewMode: Boolean,
        videoWidth: Int = 720,
        videoHeight: Int = 1280
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 1. Draw Background
        if (bgBitmap != null) {
            val src = android.graphics.Rect(0, 0, bgBitmap.width, bgBitmap.height)
            val dst = android.graphics.Rect(0, 0, videoWidth, videoHeight)
            canvas.drawBitmap(bgBitmap, src, dst, null)
            
            // Apply dual layers of premium dark mysterious gradient filters!
            // Layer A: Vertical dark-gold/violet linear shadow gradient
            val verticalGrad = android.graphics.LinearGradient(
                0f, 0f, 0f, videoHeight.toFloat(),
                intArrayOf(Color.argb(210, 10, 14, 23), Color.argb(120, 20, 16, 26), Color.argb(240, 6, 8, 14)),
                null,
                Shader.TileMode.CLAMP
            )
            val verticalPaint = Paint().apply { shader = verticalGrad }
            canvas.drawRect(0f, 0f, videoWidth.toFloat(), videoHeight.toFloat(), verticalPaint)

            // Layer B: Dramatic dark spotlight radial vignette centering the Quranic Verses
            val vignetteColors = intArrayOf(
                Color.argb(0, 0, 0, 0),        // Clear center
                Color.argb(120, 4, 3, 5),      // Soft shadow
                Color.argb(230, 2, 2, 3)       // Deep cosmic vignette edge
            )
            val vignetteOffsets = floatArrayOf(0.35f, 0.75f, 1.0f)
            val vignetteGrad = android.graphics.RadialGradient(
                videoWidth / 2f, videoHeight / 2f, Math.max(videoWidth, videoHeight).toFloat() * 0.6f,
                vignetteColors,
                vignetteOffsets,
                Shader.TileMode.CLAMP
            )
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = vignetteGrad }
            canvas.drawRect(0f, 0f, videoWidth.toFloat(), videoHeight.toFloat(), vignettePaint)
        } else {
            // Draw a gorgeous dynamic animated gradient background!
            val grad = android.graphics.LinearGradient(
                0f, 0f, videoWidth.toFloat(), videoHeight.toFloat(),
                intArrayOf(Color.parseColor("#07090E"), Color.parseColor("#150F18"), Color.parseColor("#0F0D16")),
                null,
                Shader.TileMode.CLAMP
            )
            val gradPaint = Paint().apply {
                shader = grad
            }
            canvas.drawRect(0f, 0f, videoWidth.toFloat(), videoHeight.toFloat(), gradPaint)
            
            // Draw slow-drifting stellar particles for an elite aesthetic!
            val random = java.util.Random(42)
            val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
            }
            for (s in 0 until 40) {
                val baseX = random.nextFloat() * videoWidth.toFloat()
                val baseY = random.nextFloat() * videoHeight.toFloat()
                val speed = 0.5f + random.nextFloat() * 1.5f
                
                // Drift based on frameIndex (loops visually since we restrict to videoHeight)
                val driftY = (baseY + frameIndex * speed) % videoHeight.toFloat()
                val size = 1f + random.nextFloat() * 3f
                val baseAlpha = 50 + random.nextInt(155)
                val twinkle = (Math.sin((frameIndex * 0.1f + s).toDouble()) * 50).toInt()
                val finalAlpha = (baseAlpha + twinkle).coerceIn(0, 255)
                
                starPaint.alpha = finalAlpha
                canvas.drawCircle(baseX, driftY, size, starPaint)
            }
        }
        
        val scale = videoWidth / 360f

        // Draw Surah Name at top
        val tfArabic = getArabicTypeface(context, fontFamily)
        val tfArabicWeighted = when (fontWeight) {
            "Bold" -> Typeface.create(tfArabic, Typeface.BOLD)
            else -> Typeface.create(tfArabic, Typeface.NORMAL)
        }
        
        val surahPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            try {
                color = Color.parseColor(surahNameColorStr)
            } catch (e: Exception) {
                color = Color.WHITE
            }
            alpha = (surahNameOpacity * 255).toInt().coerceIn(0, 255)
            this.textAlign = Paint.Align.CENTER
            typeface = tfArabicWeighted
            textSize = surahNameFontSize.toFloat() * 1.5f
            setShadowLayer(8f, 0f, 4f, Color.argb(200, 0, 0, 0))
        }
        // Apply Surah Name X/Y offsets, scaled
        val snX = videoWidth / 2f + (surahNameX.toFloat() * scale)
        val snY = 180f + (surahNameY.toFloat() * scale)
        if (!isPreviewMode) {
            canvas.drawText(surahName, snX, snY, surahPaint)
        }
        
        // 2. Typeface config & Animation Setup
        val animDuration = 400L // 400ms entrance
        var animAlpha = 1f
        var animScale = 1f
        var animTranslateY = 0f
        
        if (chunkTimeMs < animDuration && chunkTimeMs >= 0L) {
            val progress = chunkTimeMs.toFloat() / animDuration.toFloat()
            val easeOut = 1f - Math.pow((1f - progress).toDouble(), 3.0).toFloat()
            when (textAnimationType) {
                "Fade" -> {
                    animAlpha = easeOut
                }
                "SlideUp" -> {
                    animAlpha = easeOut
                    animTranslateY = 40f * (1f - easeOut)
                }
                "Scale" -> {
                    animAlpha = easeOut
                    animScale = 0.85f + (0.15f * easeOut)
                }
            }
        }
        
        val tf = tfArabic
        
        val tColor = try {
            Color.parseColor(textColorStr)
        } catch (e: Exception) {
            Color.WHITE
        }
        val alpha = ((textOpacity * 255) * animAlpha).toInt().coerceIn(0, 255)
        val finalTextColor = Color.argb(alpha, Color.red(tColor), Color.green(tColor), Color.blue(tColor))
        
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = finalTextColor
            this.textAlign = Paint.Align.LEFT
            typeface = tfArabicWeighted
            this.textSize = textFontSize.toFloat() * 1.8f
            setShadowLayer(8f, 0f, 4f, Color.argb(200, 0, 0, 0))
        }
        
        val layoutAlign = when (textAlign) {
            "Left" -> Layout.Alignment.ALIGN_NORMAL
            "Right" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }
        
        val textWidth = videoWidth - 100
        val sl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth)
                .setAlignment(layoutAlign)
                .setLineSpacing(0f, 1.4f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, textPaint, textWidth, layoutAlign, 1.4f, 0f, false)
        }
        
        // 3. Translation Paint
        val transColor = try {
            Color.parseColor(translationColorStr)
        } catch (e: Exception) {
            Color.parseColor("#E0E0E0")
        }
        val transFinalAlpha = (255 * animAlpha).toInt().coerceIn(0, 255)
        val finalTransColor = Color.argb(transFinalAlpha, Color.red(transColor), Color.green(transColor), Color.blue(transColor))
        
        val tfEnglish = getEnglishTypeface(context, translationFontFamily)
        val tfEnglishWeighted = when (translationFontWeight) {
            "Bold" -> Typeface.create(tfEnglish, Typeface.BOLD)
            else -> Typeface.create(tfEnglish, Typeface.NORMAL)
        }

        val transPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = finalTransColor
            this.textAlign = Paint.Align.LEFT
            typeface = tfEnglishWeighted
            this.textSize = translationFontSize.toFloat() * 1.8f
            setShadowLayer(8f, 0f, 4f, Color.argb(200, 0, 0, 0))
        }
        
        val transSl: StaticLayout? = if (translation != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(translation, 0, translation.length, transPaint, textWidth)
                    .setAlignment(layoutAlign)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(translation, transPaint, textWidth, layoutAlign, 1f, 0f, false)
            }
        } else {
            null
        }
 
        val totalHeight = sl.height + (transSl?.height?.plus(60f) ?: 0f)
        
        val baseStartY = when (textPosition) {
            "Top" -> 150f
            "Bottom" -> videoHeight.toFloat() - totalHeight - 200f
            else -> (videoHeight.toFloat() - totalHeight) / 2f
        }
        val startY = baseStartY + (arabicTextY.toFloat() * scale)
        
        canvas.save()
        if (animScale != 1f || animTranslateY != 0f) {
            val pivotX = videoWidth / 2f
            val pivotY = baseStartY + (totalHeight / 2f)
            canvas.translate(0f, animTranslateY)
            canvas.scale(animScale, animScale, pivotX, pivotY)
        }
        
        if (!isPreviewMode) {
            // 4. Draw Background Box
            if (showTextBg) {
                val bgColor = try { Color.parseColor(textBgColorStr) } catch (e: Exception) { Color.BLACK }
                val bgAlpha = ((textBgOpacity * 255) * animAlpha).toInt().coerceIn(0, 255)
                val finalBgColor = Color.argb(bgAlpha, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor))
                
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = finalBgColor
                    style = Paint.Style.FILL
                }
                
                val boxWidth = videoWidth - 60f
                val boxHeight = totalHeight + 84f
                val boxLeft = (videoWidth / 2f) - boxWidth / 2f
                val boxTop = baseStartY - 42f
                val boxRight = boxLeft + boxWidth
                val boxBottom = boxTop + boxHeight
                
                val rect = android.graphics.RectF(boxLeft, boxTop, boxRight, boxBottom)
                val radius = textBgRadius.toFloat() * 1.5f
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
            }
            
            // 5. Draw Primary Text
            canvas.save()
            canvas.translate(50f + (arabicTextX.toFloat() * scale), startY)
            sl.draw(canvas)
            canvas.restore()
            
            // 6. Draw translation
            if (transSl != null) {
                canvas.save()
                val transY = baseStartY + sl.height + 40f + (translationTextY.toFloat() * scale)
                canvas.translate(50f + (translationTextX.toFloat() * scale), transY)
                transSl.draw(canvas)
                canvas.restore()
            }
            
            // 7. Draw Qibla/Heart Icon at bottom
            if (iconOpacity > 0f) {
                val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alphaVal = (iconOpacity * 255).toInt().coerceIn(0, 255)
                    color = Color.argb(alphaVal, 255, 255, 255)
                    textSize = 50f
                    this.textAlign = Paint.Align.CENTER
                    setShadowLayer(6f, 0f, 3f, Color.argb(150, 0, 0, 0))
                }
                val heartY = videoHeight - (120f * scale) + (iconY.toFloat() * scale)
                canvas.drawText("♡", videoWidth / 2f + (iconX.toFloat() * scale), heartY, iconPaint)
            }
        }
        
        canvas.restore() // Restore animation transform
        
        return bitmap
    }

    private fun fillImageFromBitmap(image: Image, bitmap: Bitmap) {
        val imgWidth = image.width
        val imgHeight = image.height
        
        val scaledBitmap = if (bitmap.width != imgWidth || bitmap.height != imgHeight) {
            Bitmap.createScaledBitmap(bitmap, imgWidth, imgHeight, true)
        } else {
            bitmap
        }
        
        val argb = IntArray(imgWidth * imgHeight)
        scaledBitmap.getPixels(argb, 0, imgWidth, 0, 0, imgWidth, imgHeight)
        
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        
        val yRowStride = yPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride
        
        yBuffer.clear()
        uBuffer.clear()
        vBuffer.clear()
        
        val yBytes = ByteArray(imgWidth)
        var index = 0
        
        for (r in 0 until imgHeight) {
            for (c in 0 until imgWidth) {
                if (index >= argb.size) break
                val color = argb[index++]
                val rCol = (color and 0xff0000) shr 16
                val gCol = (color and 0xff00) shr 8
                val bCol = (color and 0xff) shr 0
                
                var Y = ((66 * rCol + 129 * gCol + 25 * bCol + 128) shr 8) + 16
                Y = Y.coerceIn(0, 255)
                yBytes[c] = Y.toByte()

                if (r % 2 == 0 && c % 2 == 0) {
                    var U = ((-38 * rCol - 74 * gCol + 112 * bCol + 128) shr 8) + 128
                    var V = ((112 * rCol - 94 * gCol - 18 * bCol + 128) shr 8) + 128
                    U = U.coerceIn(0, 255)
                    V = V.coerceIn(0, 255)
                    
                    val cHalf = c / 2
                    val uPos = (r / 2) * uRowStride + cHalf * uPixelStride
                    val vPos = (r / 2) * vRowStride + cHalf * vPixelStride
                    
                    if (uPos < uBuffer.capacity()) {
                        uBuffer.position(uPos)
                        uBuffer.put(U.toByte())
                    }
                    if (vPos < vBuffer.capacity()) {
                        vBuffer.position(vPos)
                        vBuffer.put(V.toByte())
                    }
                }
            }
            if (r * yRowStride + imgWidth <= yBuffer.capacity()) {
                yBuffer.position(r * yRowStride)
                yBuffer.put(yBytes)
            }
        }
    }

    private fun mapReciterIdToQuranComId(reciterId: String): Int {
        val clean = reciterId.lowercase()
        return when {
            clean.contains("alafasy") -> 7
            clean.contains("sudais") -> 3
            clean.contains("shuraim") -> 10
            clean.contains("husary") -> 6
            clean.contains("minshawi") -> 9
            clean.contains("abdulbasit") -> 2
            clean.contains("shatri") -> 4
            clean.contains("rifai") -> 5
            clean.contains("tablawi") -> 11
            else -> 7 // Fallback to Alafasy (id 7), as it has 100% complete segment data
        }
    }



    private fun resamplePCM(
        inputBuf: ByteBuffer,
        inputSize: Int,
        inputOffset: Int,
        srcSampleRate: Int,
        srcChannels: Int,
        dstSampleRate: Int,
        dstChannels: Int
    ): ByteBuffer {
        val inPosition = inputBuf.position()
        val inLimit = inputBuf.limit()
        
        inputBuf.position(inputOffset)
        inputBuf.limit(inputOffset + inputSize)
        
        val shortBuf = inputBuf.asShortBuffer()
        val totalInputShorts = shortBuf.remaining()
        val inputShorts = ShortArray(totalInputShorts)
        shortBuf.get(inputShorts)
        
        inputBuf.position(inPosition)
        inputBuf.limit(inLimit)
        
        val inputFrames = totalInputShorts / srcChannels
        if (inputFrames <= 0) return ByteBuffer.allocate(0)
        
        val monoSamples = FloatArray(inputFrames)
        for (i in 0 until inputFrames) {
            if (srcChannels == 1) {
                monoSamples[i] = inputShorts[i].toFloat()
            } else {
                val ch0 = inputShorts[i * srcChannels].toFloat()
                val ch1 = inputShorts[i * srcChannels + 1].toFloat()
                monoSamples[i] = (ch0 + ch1) / 2f
            }
        }
        
        val scale = dstSampleRate.toDouble() / srcSampleRate.toDouble()
        val outputFrames = (inputFrames * scale).toInt()
        val resampledMono = FloatArray(outputFrames)
        for (i in 0 until outputFrames) {
            val srcIdx = i / scale
            val index = srcIdx.toInt()
            val frac = srcIdx - index
            if (index >= inputFrames - 1) {
                resampledMono[i] = monoSamples[inputFrames - 1]
            } else {
                val s0 = monoSamples[index]
                val s1 = monoSamples[index + 1]
                resampledMono[i] = s0 + frac.toFloat() * (s1 - s0)
            }
        }
        
        val totalOutputShorts = outputFrames * dstChannels
        val outBytes = ByteBuffer.allocate(totalOutputShorts * 2).order(java.nio.ByteOrder.nativeOrder())
        for (i in 0 until outputFrames) {
            val sampleVal = resampledMono[i].coerceIn(-32768f, 32767f).toInt().toShort()
            for (c in 0 until dstChannels) {
                outBytes.putShort(sampleVal)
            }
        }
        
        outBytes.flip()
        return outBytes
    }

    private fun cleanArabicForWhisper(text: String): String {
        // Remove diacritics and special Uthmani symbols
        val clean1 = text.replace("[\u064B-\u065F\u0670\u06D6-\u06ED\u0610-\u061A\u0640]".toRegex(), "")
        // Map any Alif Wasla or other special alifs to standard Alif
        val clean2 = clean1.replace("[\u0671أإآ]".toRegex(), "ا")
        // Remove non-letter characters except whitespace
        val clean3 = clean2.replace("[^\\p{L}\\s]".toRegex(), "")
        // Normalize whitespace and return
        return clean3.replace("\\s+".toRegex(), " ").trim()
    }

    private fun alignWithWhisperX(context: Context, audioFile: File?, mediaUrl: String?, text: String): Pair<List<WordSegment>, List<SmartChunk>> {
        val cached = AlignmentCacheManager.getCachedAlignment(context, mediaUrl, text)
        if (cached != null) {
            SystemDiagnosticTracker.addLog("WHISPERX_API", "تم العثور على بيانات مواءمة محفوظة مسبقاً، سيتم استخدامها لتوفير الوقت.")
            if (cached.audioPath != null && audioFile != null && audioFile.absolutePath != cached.audioPath) {
                try {
                    val cachedAudio = File(cached.audioPath)
                    if (cachedAudio.exists() && (!audioFile.exists() || audioFile.length() == 0L)) {
                        cachedAudio.copyTo(audioFile, overwrite = true)
                    }
                } catch (e: Exception) {}
            }
            return Pair(cached.wordSegments, cached.smartChunks)
        }

        val wordSegments = mutableListOf<WordSegment>()
        val smartChunks = mutableListOf<SmartChunk>()
        SystemDiagnosticTracker.addLog("WHISPERX_API", "بدء مواءمة النص عبر خوادم WhisperX-Frontend")
        try {
            var fileObject: org.json.JSONObject? = null
            
            // 1. Upload audio file if provided AND mediaUrl is not provided
            if (mediaUrl.isNullOrEmpty() && audioFile != null && audioFile.exists() && audioFile.length() > 0) {
                SystemDiagnosticTracker.addLog("WHISPERX_API", "جاري رفع الملف الصوتي: ${audioFile.name} (الحجم: ${audioFile.length()})")
                val mediaType = "audio/mpeg".toMediaTypeOrNull()
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "files",
                        audioFile.name,
                        audioFile.asRequestBody(mediaType)
                    )
                    .build()

                val uploadRequest = Request.Builder()
                    .url("https://qalam249-whisperx-frontend.hf.space/gradio_api/upload")
                    .post(requestBody)
                    .build()

                var uploadResponseBody = ""
                var uRetries = 0
                while(uRetries < 3) {
                    try {
                        val uploadResponse = client.newCall(uploadRequest).execute()
                        if (!uploadResponse.isSuccessful) {
                            if (uploadResponse.code == 504 || uploadResponse.code == 503) {
                                Thread.sleep(5000)
                                uRetries++
                                continue
                            } else {
                                throw Exception("فشل رفع الملف الصوتي لـ WhisperX. الرمز: ${uploadResponse.code}")
                            }
                        }
                        uploadResponseBody = uploadResponse.body?.string() ?: ""
                        break
                    } catch(e: Exception) {
                        uRetries++
                        if(uRetries >= 3) throw e
                        Thread.sleep(5000)
                    }
                }
                if(uploadResponseBody.isEmpty()) throw Exception("Empty upload response")
                SystemDiagnosticTracker.addLog("WHISPERX_API", "تم الرفع بنجاح. استجابة الرفع: $uploadResponseBody")
                val jArray = org.json.JSONArray(uploadResponseBody)
                val remotePath = jArray.getString(0)

                fileObject = org.json.JSONObject().apply {
                    put("path", remotePath)
                    put("meta", org.json.JSONObject().apply {
                        put("_type", "gradio.FileData")
                    })
                }
            }

            // 2. Call /gradio_api/call/process
            SystemDiagnosticTracker.addLog("WHISPERX_API", "النص المرسل للمطابقة: [$text]")
            val alignPayload = org.json.JSONObject().apply {
                put("data", org.json.JSONArray().apply {
                    if (fileObject != null) put(fileObject) else put(org.json.JSONObject.NULL)
                    put(mediaUrl ?: "")
                    put(text)
                })
            }
            
            val jsonMediaType = "application/json".toMediaTypeOrNull()
            val alignRequest = Request.Builder()
                .url("https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process")
                .post(alignPayload.toString().toRequestBody(jsonMediaType))
                .build()

            var alignResponseBody = ""
            var aRetries = 0
            while(aRetries < 3) {
                try {
                    val alignResponse = client.newCall(alignRequest).execute()
                    if (!alignResponse.isSuccessful) {
                        if (alignResponse.code == 504 || alignResponse.code == 503) {
                            Thread.sleep(5000)
                            aRetries++
                            continue
                        } else {
                            throw Exception("فشل بدء المعالجة في WhisperX. الرمز: ${alignResponse.code}")
                        }
                    }
                    alignResponseBody = alignResponse.body?.string() ?: ""
                    break
                } catch(e: Exception) {
                    aRetries++
                    if(aRetries >= 3) throw e
                    Thread.sleep(5000)
                }
            }
            if(alignResponseBody.isEmpty()) throw Exception("Empty alignment response")
            SystemDiagnosticTracker.addLog("WHISPERX_API", "استجابة تهيئة المواءمة: $alignResponseBody")
            val eventIdJson = org.json.JSONObject(alignResponseBody)
            if (!eventIdJson.has("event_id")) {
                if (eventIdJson.has("error")) {
                    throw Exception("خطأ من WhisperX API: " + eventIdJson.getString("error"))
                }
                throw Exception("فشل الحصول على معرف الحدث من استجابة WhisperX: $alignResponseBody")
            }
            val eventId = eventIdJson.getString("event_id")

            // 3. Poll /gradio_api/call/process/{event_id}
            val eventRequest = Request.Builder()
                .url("https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process/$eventId")
                .get()
                .build()

            SystemDiagnosticTracker.addLog("WHISPERX_API", "بدء فحص المعالجة عبر معرف الحدث: $eventId")
            var attempt = 0
            while (attempt < 20) {
                checkCancellationAndPause()
                var completedData: String? = null
                try {
                    val eventResponse = client.newCall(eventRequest).execute()
                    if (!eventResponse.isSuccessful) {
                        eventResponse.close()
                        throw Exception("فصل غير متوقع، رمز الخطأ: ${eventResponse.code}")
                    }
                    val responseBody = eventResponse.body ?: throw Exception("Empty response body")
                    val reader = responseBody.charStream().buffered()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line ?: ""
                        if (currentLine.startsWith("event: complete")) {
                            val nextLine = reader.readLine() ?: ""
                            if (nextLine.startsWith("data: ")) {
                                completedData = nextLine.substring("data: ".length)
                            }
                        } else if (currentLine.startsWith("event: error")) {
                            val nextLine = reader.readLine() ?: ""
                            if (nextLine.startsWith("data: ")) {
                                val errStr = "حدث خطأ في خادم WhisperX: " + nextLine.substring("data: ".length)
                                throw Exception(errStr)
                            } else {
                                throw Exception("فشل مجرى المواءمة لـ WhisperX بسبب حدث خطأ")
                            }
                        }
                    }
                    eventResponse.close()
                } catch (e: Exception) {
                    SystemDiagnosticTracker.addLog("WHISPERX_API", "انقطع الاتصال أو حدث خطأ أثناء المراقبة: ${e.message} - جاري إعادة المحاولة...")
                }

                if (completedData != null) {
                    SystemDiagnosticTracker.addLog("WHISPERX_API", "اكتملت المعالجة بنجاح. جاري تحليل البيانات المسترجعة...")
                    val dataArray = org.json.JSONArray(completedData)
                    if (dataArray.length() >= 3) {
                        val firstItem = dataArray.get(0)
                        if (firstItem is org.json.JSONObject && firstItem.has("error")) {
                            val errStr = "استجابة خطأ بصيغة JSON من WhisperX: " + firstItem.getString("error")
                            SystemDiagnosticTracker.addLog("ERROR", errStr)
                            throw Exception(errStr)
                        } else if (firstItem is String && firstItem.startsWith("❌ خطأ")) {
                            val errStr = "استجابة خطأ من WhisperX: " + firstItem
                            SystemDiagnosticTracker.addLog("ERROR", errStr)
                            throw Exception(errStr)
                        }

                        var wordsArray: org.json.JSONArray? = null
                        var chunksArray: org.json.JSONArray? = null
                        
                        // Try to find words and chunks arrays in the response
                        for (i in 0 until dataArray.length()) {
                            val jsonString = dataArray.optString(i)
                            if (jsonString != null && jsonString.isNotEmpty()) {
                                val trimmed = jsonString.trim()
                                if (trimmed.startsWith("[")) {
                                    try {
                                        val candidate = org.json.JSONArray(trimmed)
                                        if (candidate.length() > 0) {
                                            val firstObj = candidate.optJSONObject(0)
                                            if (firstObj != null) {
                                                if (firstObj.has("word") && wordsArray == null) {
                                                    wordsArray = candidate
                                                } else if ((firstObj.has("text") && firstObj.has("start")) || (firstObj.has("text") && firstObj.has("timestamp"))) {
                                                    chunksArray = candidate
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {}
                                } else if (trimmed.startsWith("{")) {
                                    try {
                                        val jsonObject = org.json.JSONObject(trimmed)
                                        if (jsonObject.has("words") && wordsArray == null) {
                                            wordsArray = jsonObject.getJSONArray("words")
                                        } else if (jsonObject.has("segments") && wordsArray == null) {
                                            wordsArray = jsonObject.getJSONArray("segments")
                                        }
                                    } catch (e: Exception) {}
                                }
                            }
                        }
                        
                        if (wordsArray == null) {
                            val errStr = "لم يتم العثور على مصفوفة كلمات في استجابة WhisperX"
                            SystemDiagnosticTracker.addLog("ERROR", errStr + ". الاستجابة: $completedData")
                            throw Exception(errStr)
                        }
                        SystemDiagnosticTracker.addLog("WHISPERX_API", "عدد الكلمات المرجعة من WhisperX: ${wordsArray.length()}")
                        
                        if (audioFile != null && (!audioFile.exists() || audioFile.length() == 0L)) {
                            var audioOutputObj: org.json.JSONObject? = null
                            var audioOutputStr = ""
                            
                            // Based on app.py, out_audio is at index 6
                            val audioItemIndex = 6
                            if (dataArray.length() > audioItemIndex) {
                                val itemObj = dataArray.optJSONObject(audioItemIndex)
                                val itemStr = dataArray.optString(audioItemIndex)
                                
                                if (itemObj != null && (itemObj.has("url") || itemObj.has("path"))) {
                                    audioOutputObj = itemObj
                                } else if (itemStr.isNotBlank() && itemStr != "null") {
                                    audioOutputStr = itemStr
                                }
                            }
                            
                            // Fallback logic if structure changed
                            if (audioOutputObj == null && audioOutputStr.isBlank()) {
                                for (i in dataArray.length() - 1 downTo 0) {
                                    val itemObj = dataArray.optJSONObject(i)
                                    val itemStr = dataArray.optString(i)
                                    if (itemObj != null && (itemObj.has("url") || itemObj.has("path"))) {
                                        val urlOrPath = itemObj.optString("url", "") + itemObj.optString("path", "")
                                        if (urlOrPath.contains(".mp3") || urlOrPath.contains(".wav") || urlOrPath.contains(".m4a") || urlOrPath.contains(".ogg") || urlOrPath.contains("audio") || urlOrPath.contains("gradio")) {
                                            audioOutputObj = itemObj
                                            break
                                        }
                                    } else if (itemStr.isNotBlank() && itemStr != "null" && !itemStr.contains("العنوان:") && (itemStr.contains(".mp3") || itemStr.contains(".wav") || itemStr.contains(".m4a") || itemStr.contains(".ogg") || itemStr.contains("/tmp/gradio/"))) {
                                        audioOutputStr = itemStr
                                        break
                                    }
                                }
                            }
                            
                            if (audioOutputObj != null && audioOutputObj.has("url")) {
                                val returnedAudioUrl = audioOutputObj.getString("url")
                                SystemDiagnosticTracker.addLog("WHISPERX_API", "تحميل الملف الصوتي المستخرج من الرابط: $returnedAudioUrl")
                                downloadAudio(returnedAudioUrl, audioFile)
                            } else if (audioOutputObj != null && audioOutputObj.has("path")) {
                                val returnedAudioPath = audioOutputObj.getString("path")
                                val returnedAudioUrl = "https://qalam249-whisperx-frontend.hf.space/file=$returnedAudioPath"
                                SystemDiagnosticTracker.addLog("WHISPERX_API", "تحميل الملف الصوتي المستخرج من المسار: $returnedAudioUrl")
                                downloadAudio(returnedAudioUrl, audioFile)
                            } else if (audioOutputStr.isNotBlank() && audioOutputStr != "null") {
                                val returnedAudioUrl = if (audioOutputStr.startsWith("http")) audioOutputStr else "https://qalam249-whisperx-frontend.hf.space/file=$audioOutputStr"
                                SystemDiagnosticTracker.addLog("WHISPERX_API", "تحميل الملف الصوتي المستخرج من السلسلة النصية: $returnedAudioUrl")
                                downloadAudio(returnedAudioUrl, audioFile)
                            } else {
                                throw java.lang.Exception("لم يتم استرجاع الملف الصوتي من المعالج (تعذر العثور على مسار الصوت)")
                            }
                        }

                        wordSegments.clear()
                        var globalWordIdx = 0
                        var usedChunksForWords = false
                        
                        if (chunksArray != null) {
                            for (cIdx in 0 until chunksArray.length()) {
                                val cObj = chunksArray.getJSONObject(cIdx)
                                val chunkWordsArray = cObj.optJSONArray("words")
                                if (chunkWordsArray != null && chunkWordsArray.length() > 0) {
                                    usedChunksForWords = true
                                    for (cwIdx in 0 until chunkWordsArray.length()) {
                                        val wordObj = chunkWordsArray.getJSONObject(cwIdx)
                                        val startSec = wordObj.optDouble("start", -1.0)
                                        var endSec = wordObj.optDouble("end", -1.0)
                                        val wordText = wordObj.optString("word", "").trim()
                                        if (endSec < 0.0 || endSec.isNaN()) {
                                            endSec = startSec + 1.0
                                        }
                                        if (startSec >= 0.0) {
                                            wordSegments.add(
                                                WordSegment(
                                                    wordIndex = globalWordIdx + 1,
                                                    startTimeMs = (startSec * 1000).toLong(),
                                                    endTimeMs = (endSec * 1000).toLong(),
                                                    word = wordText
                                                )
                                            )
                                            globalWordIdx++
                                        }
                                    }
                                }
                                
                                val chunkText = cObj.optString("text", "").trim()
                                var startSec = cObj.optDouble("start", -1.0)
                                var endSec = cObj.optDouble("end", -1.0)
                                
                                if (startSec < 0.0 || endSec < 0.0) {
                                    val tsArray = cObj.optJSONArray("timestamp")
                                    if (tsArray != null) {
                                        startSec = tsArray.optDouble(0, startSec)
                                        endSec = tsArray.optDouble(1, endSec)
                                    }
                                }
                                
                                if (endSec < 0.0 || endSec.isNaN()) {
                                    endSec = startSec + 3.0 // Fallback to 3 seconds if missing
                                }
                                if (startSec >= 0.0 && chunkText.isNotEmpty()) {
                                    smartChunks.add(
                                        SmartChunk(
                                            arabic = chunkText,
                                            english = null,
                                            startTimeMs = (startSec * 1000).toLong(),
                                            endTimeMs = (endSec * 1000).toLong()
                                        )
                                    )
                                }
                            }
                        }

                        if (!usedChunksForWords && wordsArray != null) {
                            for (wIdx in 0 until wordsArray.length()) {
                                val wordObj = wordsArray.getJSONObject(wIdx)
                                val startSec = wordObj.optDouble("start", -1.0)
                                var endSec = wordObj.optDouble("end", -1.0)
                                val wordText = wordObj.optString("word", "").trim()
                                if (endSec < 0.0 || endSec.isNaN()) {
                                    endSec = startSec + 1.0
                                }
                                if (startSec >= 0.0) {
                                    wordSegments.add(
                                        WordSegment(
                                            wordIndex = wIdx + 1,
                                            startTimeMs = (startSec * 1000).toLong(),
                                            endTimeMs = (endSec * 1000).toLong(),
                                            word = wordText
                                        )
                                    )
                                }
                            }
                        }
                    }
                    break
                }
                attempt++
                SystemDiagnosticTracker.addLog("WHISPERX_API", "المحاولة #$attempt: معالجة قائمة الانتظار لـ WhisperX...")
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            SystemDiagnosticTracker.addLog("ERROR", "خطأ فادح أثناء مزامنة WhisperX للآية: ${e.message}")
            e.printStackTrace()
            throw Exception("فشل الاتصال بخادم WhisperX هل تود معاودة الاتصال ام الغاء العملية بالكامل (رمز الخطأ: ${e.message})")
        }
        
        AlignmentCacheManager.putCachedAlignment(context, mediaUrl, text, wordSegments, smartChunks, audioFile)
        
        return Pair(wordSegments, smartChunks)
    }

    private suspend fun alignTranslationWithGemini(
        context: Context,
        arabicChunks: List<String>,
        fullTranslation: String
    ): List<String>? = withContext(Dispatchers.IO) {
        val settingsManager = SettingsManager(context)
        var apiKey = settingsManager.geminiApiKey.first()
        val geminiModel = settingsManager.geminiModel.first()
        if (apiKey.isBlank()) {
            apiKey = com.example.BuildConfig.GEMINI_API_KEY
        }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val prompt = SystemPromptTemplate.getAlignmentPrompt(arabicChunks, fullTranslation)

        val jsonRequest = JSONObject().apply {
            val partsArray = org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("text", prompt)
                })
            }
            val countArray = org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", partsArray)
                })
            }
            put("contents", countArray)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
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
            var attempt = 0
            val maxAttempts = 3
            while (attempt < maxAttempts) {
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
                            val cleanText = if (rawText.startsWith("```json")) {
                                rawText.substringAfter("```json").substringBeforeLast("```").trim()
                            } else if (rawText.startsWith("```")) {
                                rawText.substringAfter("```").substringBeforeLast("```").trim()
                            } else {
                                rawText
                            }
                            val jsonOutput = JSONObject(cleanText)
                            if (jsonOutput.has("aligned_translations")) {
                                val arr = jsonOutput.getJSONArray("aligned_translations")
                                val chunksList = mutableListOf<String>()
                                for (i in 0 until arr.length()) {
                                    chunksList.add(arr.getString(i))
                                }
                                if (chunksList.size == arabicChunks.size) {
                                    return@withContext chunksList
                                }
                            }
                        }
                    }
                    break
                } else if (response.code == 429) {
                    if (attempt < maxAttempts - 1) {
                        attempt++
                        kotlinx.coroutines.delay(2000L * attempt)
                        continue
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private suspend fun getSmartChunks(
        context: Context,
        arabicText: String,
        englishText: String?,
        wordSegments: List<WordSegment>,
        whisperXChunks: List<SmartChunk>,
        durationMs: Long
    ): List<SmartChunk> {
        val totalArabic = arabicText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val englishWords = englishText?.split("\\s+".toRegex())?.filter { it.isNotBlank() } ?: emptyList()
        val totalEnglish = englishWords.size

        if (whisperXChunks.isNotEmpty()) {
            val mergedChunks = mutableListOf<SmartChunk>()
            var i = 0
            while (i < whisperXChunks.size) {
                var current = whisperXChunks[i]
                
                while (i + 1 < whisperXChunks.size) {
                    val wordCount = current.arabic.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    val nextWordCount = whisperXChunks[i + 1].arabic.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    
                    val isEnd = current.arabic.contains("﴿") || current.arabic.contains("﴾")
                    
                    if (wordCount < 3 && nextWordCount < 3 && !isEnd) {
                        current = current.copy(
                            arabic = current.arabic + " " + whisperXChunks[i + 1].arabic,
                            endTimeMs = whisperXChunks[i + 1].endTimeMs
                        )
                        i++
                    } else {
                        break
                    }
                }
                mergedChunks.add(current)
                i++
            }
            
            var englishChunkTexts: List<String>? = null
            if (!englishText.isNullOrBlank() && mergedChunks.size > 1) {
                val arabicChunkTexts = mergedChunks.map { it.arabic }
                englishChunkTexts = alignTranslationWithGemini(context, arabicChunkTexts, englishText)
                if (englishChunkTexts != null && englishChunkTexts.size != mergedChunks.size) {
                    SystemDiagnosticTracker.addLog("GEMINI", "تم رفض ترجمة Gemini لأن عدد الجمل لا يطابق")
                    englishChunkTexts = null
                }
            }
            
            return mergedChunks.mapIndexed { index, chunk ->
                val fallbackEnglish = if (mergedChunks.size == 1) englishText else ""
                chunk.copy(english = englishChunkTexts?.get(index) ?: fallbackEnglish)
            }
        }

        val arabicWords = arabicText.split("\\s+".toRegex()).filter { it.isNotBlank() }
        
        if (arabicWords.isEmpty()) {
            return listOf(SmartChunk(arabicText, englishText, 0L, durationMs))
        }

        // Strict Requirement: WhisperX segments must not be empty! Otherwise fail immediately to satisfy user's intent.
        if (wordSegments.isEmpty()) {
            throw Exception("فشلت مواءمة الكلمات: لم تقم خدمة WhisperX بإرجاع أي بيانات مزامنة!")
        }
        
        val adjustedWordSegments = wordSegments.sortedBy { it.startTimeMs }
        
        // Helper function to normalize text for perfect comparison
        fun normalizeForMatch(w: String): String {
            val stripped = w.replace("[\u064B-\u065F\u0670\u06D6-\u06ED\u0610-\u061A\u0640]".toRegex(), "")
            val stripNonLetter = stripped.replace("[^\\p{L}]".toRegex(), "")
            return stripNonLetter.replace("[\u0671أإآ]".toRegex(), "ا")
        }

        val cleanSegs = adjustedWordSegments.map { normalizeForMatch(it.word) }
        val cleanArabic = arabicWords.map { normalizeForMatch(it) }

        val wordSegMap = mutableMapOf<Int, WordSegment>()
        var sIdx = 0
        for (aIdx in cleanArabic.indices) {
            val aWord = cleanArabic[aIdx]
            if (aWord.isEmpty()) continue
            
            var matched = false
            for (lookAhead in 0 until 5) {
                val checkIdx = sIdx + lookAhead
                if (checkIdx < cleanSegs.size && cleanSegs[checkIdx].isNotEmpty()) {
                    if (cleanSegs[checkIdx] == aWord || cleanSegs[checkIdx].contains(aWord) || aWord.contains(cleanSegs[checkIdx])) {
                        wordSegMap[aIdx] = adjustedWordSegments[checkIdx]
                        sIdx = checkIdx + 1
                        matched = true
                        break
                    }
                }
            }
            // Strict: Do not update sIdx or greedily mapping if not matched.
            // This prevents the catastrophic cascading misalignment of later words when a word fails to align.
            // Instead, the timing of this word will be correctly interpolated relative to its neighbors.
        }
        
        fun getWordTimingSafe(aIdx: Int): Pair<Long, Long>? {
            val seg = wordSegMap[aIdx]
            if (seg != null) return Pair(seg.startTimeMs, seg.endTimeMs)
            return null
        }
        
        val chunks = mutableListOf<List<Int>>() 
        var currentChunk = mutableListOf<Int>()
        for (idx in arabicWords.indices) {
            currentChunk.add(idx)
            val word = arabicWords[idx]
            val hasPauseMark = word.contains(Regex("[ۗۖۚۘۙۛ۞۩]"))
            
            var hasSilence = false
            if (idx < arabicWords.indices.last) {
                val currEnd = getWordTimingSafe(idx)?.second
                val nextStart = getWordTimingSafe(idx + 1)?.first
                if (currEnd != null && nextStart != null) {
                    if (nextStart - currEnd > 300L) { // Gap of >300ms implies silence
                        hasSilence = true
                    }
                }
            }
            
            if (hasPauseMark || hasSilence || idx == arabicWords.indices.last) {
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk)
                    currentChunk = mutableListOf()
                }
            }
        }
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk)
        }
        
        val refinedChunks = mutableListOf<List<Int>>()
        for (chunk in chunks) {
            if (chunk.size > 25) { 
                val subChunks = chunk.chunked(15)
                refinedChunks.addAll(subChunks)
            } else {
                refinedChunks.add(chunk)
            }
        }
        
        // 1. Construct Arabic chunk texts
        val arabicChunkTexts = refinedChunks.map { arabicIndices ->
            arabicIndices.map { arabicWords[it] }.joinToString(" ")
        }
        
        // 2. We do NOT chunk the English translation. It will be displayed in full.
        val englishChunkTexts: List<String>? = null
        
        fun getWordTiming(aIdx: Int, fallbackRatio: Float): Pair<Long, Long> {
            val safe = getWordTimingSafe(aIdx)
            if (safe != null) return safe
            
            // Linear neighbor interpolation if individual word has no direct timing mapping
            var prevIdx = aIdx - 1
            var prevTiming: Pair<Long, Long>? = null
            while (prevIdx >= 0) {
                val t = getWordTimingSafe(prevIdx)
                if (t != null) {
                    prevTiming = t
                    break
                }
                prevIdx--
            }
            
            var nextIdx = aIdx + 1
            var nextTiming: Pair<Long, Long>? = null
            while (nextIdx < totalArabic) {
                val t = getWordTimingSafe(nextIdx)
                if (t != null) {
                    nextTiming = t
                    break
                }
                nextIdx++
            }
            
            if (prevTiming != null && nextTiming != null) {
                val steps = nextIdx - prevIdx
                val myStep = aIdx - prevIdx
                val start = prevTiming.second + (nextTiming.first - prevTiming.second) * myStep / steps
                val end = start + (nextTiming.first - prevTiming.second) / steps
                return Pair(start, end)
            } else if (prevTiming != null) {
                val start = prevTiming.second + 50L
                val end = start + 200L
                return Pair(start, end)
            } else if (nextTiming != null) {
                val end = (nextTiming.first - 50L).coerceAtLeast(0L)
                val start = (end - 200L).coerceAtLeast(0L)
                return Pair(start, end)
            }
            
            // Full fallback to avoid crash if WhisperX returned empty words or word-mappings completely failed.
            val fallbackStart = (fallbackRatio * durationMs).toLong()
            val fallbackEnd = (fallbackStart + 300L).coerceAtMost(durationMs)
            return Pair(fallbackStart, fallbackEnd)
        }
        
        val result = mutableListOf<SmartChunk>()
        for (cIdx in refinedChunks.indices) {
            val arabicIndices = refinedChunks[cIdx]
            val chunkArabicText = arabicChunkTexts[cIdx]
            val chunkEnglishText = if (englishChunkTexts != null && cIdx < englishChunkTexts.size) {
                englishChunkTexts[cIdx]
            } else {
                null
            }
            
            val startRatio = arabicIndices.first().toFloat() / totalArabic.toFloat()
            val firstTiming = getWordTiming(arabicIndices.first(), startRatio)
            val startMs = firstTiming.first
            var endMs = firstTiming.second
            
            for (i in 1 until arabicIndices.size - 1) {
                val timing = getWordTiming(arabicIndices[i], 0f)
                if (timing.second > endMs) endMs = timing.second
            }
            
            if (arabicIndices.size > 1) {
                val endRatio = (arabicIndices.last() + 1).toFloat() / totalArabic.toFloat()
                val lastTiming = getWordTiming(arabicIndices.last(), endRatio)
                if (lastTiming.second > endMs) endMs = lastTiming.second
            }
            
            result.add(SmartChunk(chunkArabicText, if (chunkEnglishText.isNullOrBlank()) null else chunkEnglishText, startMs, endMs))
        }
        return result
    }

    private fun getActiveSmartChunk(chunks: List<SmartChunk>, currentTimeMs: Long): SmartChunk? {
        if (chunks.isEmpty()) return null
        if (currentTimeMs < chunks.first().startTimeMs) {
            return null
        }
        var activeChunk = chunks.first()
        for (chunk in chunks) {
            if (currentTimeMs >= chunk.startTimeMs) {
                activeChunk = chunk
            } else {
                break
            }
        }
        return activeChunk
    }
}

class SequentialFrameDecoder(private val videoPath: String) {
    private var extractor: MediaExtractor? = null
    private var decoder: MediaCodec? = null
    private var width = 720
    private var height = 1280
    private var trackIndex = -1
    private val bufferInfo = MediaCodec.BufferInfo()
    private var isEOS = false

    init {
        try {
            val ext = MediaExtractor()
            ext.setDataSource(videoPath)
            extractor = ext
            for (i in 0 until ext.trackCount) {
                val format = ext.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/")) {
                    ext.selectTrack(i)
                    trackIndex = i
                    width = format.getInteger(MediaFormat.KEY_WIDTH)
                    height = format.getInteger(MediaFormat.KEY_HEIGHT)
                    
                    val dec = MediaCodec.createDecoderByType(mime)
                    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                    dec.configure(format, null, null, 0)
                    dec.start()
                    decoder = dec
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            release()
        }
    }

    fun getNextFrame(): Bitmap? {
        val dec = decoder ?: return null
        val ext = extractor ?: return null
        if (trackIndex == -1) return null
        
        val timeoutUs = 5000L
        var attempts = 0
        while (attempts < 80) {
            attempts++
            try {
                if (!isEOS) {
                    val inIdx = dec.dequeueInputBuffer(timeoutUs)
                    if (inIdx >= 0) {
                        val buf = dec.getInputBuffer(inIdx)!!
                        val sampleSize = ext.readSampleData(buf, 0)
                        if (sampleSize < 0) {
                            dec.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            dec.queueInputBuffer(inIdx, 0, sampleSize, ext.sampleTime, 0)
                            ext.advance()
                        }
                    }
                }

                val outIdx = dec.dequeueOutputBuffer(bufferInfo, timeoutUs)
                if (outIdx >= 0) {
                    var bitmap: Bitmap? = null
                    try {
                        val image = dec.getOutputImage(outIdx)
                        if (image != null) {
                            bitmap = convertYUVImageToBitmap(image)
                            image.close()
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    dec.releaseOutputBuffer(outIdx, false)
                    
                    if (bitmap != null) {
                        return bitmap
                    }
                } else if (outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val format = dec.outputFormat
                    width = format.getInteger(MediaFormat.KEY_WIDTH)
                    height = format.getInteger(MediaFormat.KEY_HEIGHT)
                } else if (isEOS && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    ext.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                    isEOS = false
                    dec.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }
        }
        return null
    }

    private fun convertYUVImageToBitmap(image: Image): Bitmap {
        val w = image.width
        val h = image.height
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        
        val yRowStride = yPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride
        
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        
        var index = 0
        for (y in 0 until h) {
            val yRowStart = y * yRowStride
            for (x in 0 until w) {
                val yValue = (yBuffer.get(yRowStart + x).toInt() and 0xff)
                
                val uvIndex = (y / 2) * uRowStride + (x / 2) * uPixelStride
                val vIndex = (y / 2) * vRowStride + (x / 2) * vPixelStride
                
                val uValue = if (uvIndex < uBuffer.capacity()) (uBuffer.get(uvIndex).toInt() and 0xff) - 128 else 0
                val vValue = if (vIndex < vBuffer.capacity()) (vBuffer.get(vIndex).toInt() and 0xff) - 128 else 0
                
                var rCol = (yValue + 1.370705f * vValue).toInt()
                var gCol = (yValue - 0.337633f * uValue - 0.698001f * vValue).toInt()
                var bCol = (yValue + 1.732446f * uValue).toInt()
                
                rCol = rCol.coerceIn(0, 255)
                gCol = gCol.coerceIn(0, 255)
                bCol = bCol.coerceIn(0, 255)
                
                pixels[index++] = (0xff shl 24) or (rCol shl 16) or (gCol shl 8) or bCol
            }
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }

    fun release() {
        try {
            decoder?.stop()
            decoder?.release()
        } catch (e: Exception) {}
        decoder = null
        
        try {
            extractor?.release()
        } catch (e: Exception) {}
        extractor = null
    }
}

object SystemPromptTemplate {
    fun getAlignmentPrompt(arabicChunks: List<String>, fullTranslation: String): String {
        return """
            You are an expert Quran translation alignment assistant.
            Your task is to provide an accurate, context-aware English translation for each specific Arabic chunk provided below.
            The user wants the English translation to match the exact meaning of the Arabic words in that specific chunk, rather than randomly splitting the full verse translation.
            
            Input Arabic Chunks:
            ${arabicChunks.mapIndexed { idx, s -> "Chunk #${idx + 1}: $s" }.joinToString("\n")}

            Return a single raw JSON object matching this schema:
            {
               "aligned_translations": [
                  "Accurate English translation for Chunk #1",
                  "Accurate English translation for Chunk #2", ...
               ]
            }
            
            CRITICAL RULES:
            1. The number of translations MUST EXACTLY match the number of input chunks (${arabicChunks.size}).
            2. Do not include any explanation, backticks or markdown formatting. Only return valid raw JSON.
        """.trimIndent()
    }
}

