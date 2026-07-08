import os

new_code = """package com.example.generator

import android.content.Context
import android.graphics.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.media.Image
import com.example.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.max

data class VerseData(val text: String, val translation: String?, val audioPath: String, val durationUs: Long, val originalMp3Path: String)

class VideoGenerator {
    private val client = OkHttpClient()
    @Volatile 
    private var threadError: Throwable? = null
    
    @Volatile
    private var isCancelled = false

    fun cancelNetworkRequests() {
        isCancelled = true
    }

    suspend fun generateReel(
        context: Context,
        surah: Int,
        startAyah: Int,
        endAyah: Int,
        reciterId: String,
        showTranslation: Boolean,
        pexelsApiKey: String,
        videoQuality: String = "Medium",
        isRetry: Boolean = false,
        isPreviewMode: Boolean = false,
        includeBasmalah: Boolean = false,
        videoQuery: String = "",
        chunkIndexToReplace: Int = -1,
        onProgress: (String, Float) -> Unit,
        onComplete: (Uri) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        SystemDiagnosticTracker.clearLogs()
        SystemDiagnosticTracker.addLog("INIT", "بدء عملية إنشاء الفيديو (Surah: $surah, Start: $startAyah, End: $endAyah, Reciter: $reciterId)")
        
        threadError = null
        isCancelled = false
        var videoCodec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var bgDecoder: SequentialFrameDecoder? = null
        
        try {
            val settingsManager = SettingsManager(context)
            val language = settingsManager.language.first()
            val isArabic = language == "ar"
            
            val fontFamily = settingsManager.fontFamily.first()
            val textFontSize = settingsManager.fontSize.first()
            val textColorStr = settingsManager.textColor.first()
            val textOpacity = settingsManager.textOpacity.first()
            
            val showTextBg = settingsManager.showTextBackground.first()
            val textBgColorStr = settingsManager.textBgColor.first()
            val textBgOpacity = settingsManager.textBgOpacity.first()
            val textBgRadius = settingsManager.textBgRadius.first()
            
            val textPosition = settingsManager.textPosition.first()
            val translationFontSize = settingsManager.translationFontSize.first()
            val translationColorStr = settingsManager.translationColor.first()
            val pixabayApiKey = settingsManager.pixabayApiKey.first()

            val isPopularClip = reciterId.startsWith("popular|")
            val socialUrl = if (isPopularClip) reciterId.removePrefix("popular|") else ""

            var combinedAudioPath = ""
            val versesTexts = mutableListOf<String>()
            
            if (!isPopularClip) {
                SystemDiagnosticTracker.addLog("FETCH", "جلب الآيات والملفات الصوتية (الصفحة الرئيسية)")
                val destFiles = mutableListOf<File>()
                for ((i, ayah) in (startAyah..endAyah).withIndex()) {
                    if (isCancelled) return@withContext
                    onProgress(if (isArabic) "جاري تحميل الآية $ayah..." else "Fetching Ayah $ayah...", 0.05f)
                    
                    val (text, globalAyahNumber) = fetchVerseInfo(surah, ayah, "quran-uthmani")
                    versesTexts.add(text)
                    
                    val audioFileName = "${reciterId}_${surah}_${ayah}.mp3"
                    val url = "https://cdn.islamic.network/quran/audio/64/$reciterId/$globalAyahNumber.mp3"
                    val destFile = File(context.cacheDir, audioFileName)
                    SystemDiagnosticTracker.addLog("DOWNLOAD", "تنزيل الآية $ayah من $url")
                    downloadAudio(url, destFile)
                    destFiles.add(destFile)
                }
                
                SystemDiagnosticTracker.addLog("AUDIO", "دمج الملفات الصوتية")
                val combinedMp3 = File(context.cacheDir, "combined_quran_${System.currentTimeMillis()}.mp3")
                combinedMp3.outputStream().use { out ->
                    for (f in destFiles) {
                        f.inputStream().use { it.copyTo(out) }
                    }
                }
                combinedAudioPath = combinedMp3.absolutePath
            } else {
                SystemDiagnosticTracker.addLog("FETCH", "جلب الآيات (الصفحة الرائجة)")
                if (surah > 0 && startAyah > 0) {
                    val actualEnd = if (endAyah >= startAyah) endAyah else startAyah
                    for ((i, ayah) in (startAyah..actualEnd).withIndex()) {
                        val (text, _) = fetchVerseInfo(surah, ayah, "quran-uthmani")
                        versesTexts.add(text)
                    }
                }
            }
            
            val fullArabicText = versesTexts.joinToString(" ")
            
            // WhisperX alignment
            SystemDiagnosticTracker.addLog("WHISPERX", "بدء الموائمة عبر WhisperX Space")
            onProgress(if (isArabic) "جاري الموائمة الذكية للصوت والنص..." else "Smart aligning audio & text...", 0.15f)
            val whisperClient = WhisperXClient()
            
            val result = whisperClient.processAudio(
                file = if (!isPopularClip) File(combinedAudioPath) else null,
                urlInput = socialUrl,
                arabicText = fullArabicText,
                onProgress = { msg ->
                    SystemDiagnosticTracker.addLog("WHISPERX_PROGRESS", msg)
                    onProgress(msg, 0.2f)
                }
            )
            
            SystemDiagnosticTracker.addLog("WHISPERX", "تمت الموائمة بنجاح")
            
            val chunksJson = JSONArray(result.chunksJson)
            val chunks = mutableListOf<SmartChunk>()
            for (i in 0 until chunksJson.length()) {
                val obj = chunksJson.getJSONObject(i)
                val tsArray = obj.optJSONArray("timestamp")
                val start = if (tsArray != null && tsArray.length() > 0) tsArray.optDouble(0, 0.0) else 0.0
                val end = if (tsArray != null && tsArray.length() > 1) tsArray.optDouble(1, start + 2.0) else start + 2.0
                val text = obj.optString("text", "")
                chunks.add(SmartChunk(text, null, (start * 1000).toLong(), (end * 1000).toLong()))
            }
            if (chunks.isEmpty()) {
                throw Exception("لم يتم استخراج أي جمل من أداة WhisperX")
            }
            
            SystemDiagnosticTracker.addLog("AUDIO", "تجهيز الملف الصوتي النهائي للمونتاج")
            var finalAudioPath = ""
            if (isPopularClip && result.audioUrl.isNotBlank()) {
                onProgress(if (isArabic) "جاري تحميل الصوت المعالج..." else "Downloading processed audio...", 0.3f)
                val outAudioFile = File(context.cacheDir, "processed_audio_${System.currentTimeMillis()}.wav")
                downloadAudio(result.audioUrl, outAudioFile)
                
                val aacFile = File(context.cacheDir, "processed_audio_${System.currentTimeMillis()}.m4a")
                transcodeMp3ToAac(outAudioFile.absolutePath, aacFile.absolutePath)
                finalAudioPath = aacFile.absolutePath
            } else {
                val aacFile = File(context.cacheDir, "combined_audio_${System.currentTimeMillis()}.m4a")
                transcodeMp3ToAac(combinedAudioPath, aacFile.absolutePath)
                finalAudioPath = aacFile.absolutePath
            }
            
            val ext = MediaExtractor().apply { setDataSource(finalAudioPath) }
            ext.selectTrack(0)
            val audioFormat = ext.getTrackFormat(0)
            var totalDurationUs = audioFormat.getLong(MediaFormat.KEY_DURATION, -1L)
            if (totalDurationUs <= 0) {
                var maxTs = 0L
                val bb = ByteBuffer.allocate(256)
                while (ext.readSampleData(bb, 0) >= 0) {
                    maxTs = ext.sampleTime
                    ext.advance()
                }
                totalDurationUs = maxTs
            }
            ext.release()
            
            SystemDiagnosticTracker.addLog("VIDEO", "جلب فيديو الخلفية السينمائي")
            onProgress(if (isArabic) "جاري تحضير الخلفية السينمائية..." else "Fetching cinematic background...", 0.4f)
            
            val bgFile = File(context.cacheDir, "bg_video_main.mp4")
            var videoUrl = ""
            val query = if (videoQuery.isNotBlank()) videoQuery else "nature night sky"
            
            if (pixabayApiKey.isNotBlank()) {
                try {
                    val req = Request.Builder().url("https://pixabay.com/api/videos/?key=$pixabayApiKey&q=${Uri.encode(query)}").build()
                    val resp = client.newCall(req).execute()
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "")
                        val hits = json.getJSONArray("hits")
                        if (hits.length() > 0) {
                            val randomHit = hits.getJSONObject((Math.random() * hits.length()).toInt())
                            videoUrl = randomHit.getJSONObject("videos").getJSONObject("medium").getString("url")
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            
            if (videoUrl.isBlank() && pexelsApiKey.isNotBlank()) {
                try {
                    val req = Request.Builder()
                        .url("https://api.pexels.com/videos/search?query=${Uri.encode(query)}&orientation=portrait&per_page=15")
                        .addHeader("Authorization", pexelsApiKey)
                        .build()
                    val resp = client.newCall(req).execute()
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "")
                        val videos = json.getJSONArray("videos")
                        if (videos.length() > 0) {
                            val randomVideo = videos.getJSONObject((Math.random() * videos.length()).toInt())
                            val files = randomVideo.getJSONArray("video_files")
                            if (files.length() > 0) {
                                videoUrl = files.getJSONObject(0).getString("link")
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            
            if (videoUrl.isBlank()) {
                videoUrl = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4"
            }
            
            try {
                val vReq = Request.Builder().url(videoUrl).build()
                val vResp = client.newCall(vReq).execute()
                if (vResp.isSuccessful) {
                    val input = vResp.body?.byteStream()
                    val output = FileOutputStream(bgFile)
                    input?.copyTo(output)
                    output.close()
                }
            } catch (e: Exception) { e.printStackTrace() }
            
            if (bgFile.exists()) {
                bgDecoder = SequentialFrameDecoder(bgFile.absolutePath)
            }
            
            SystemDiagnosticTracker.addLog("RENDER", "بدء عملية تصيير الفيديو")
            
            val outputPath = File(context.cacheDir, "quran_reel_${System.currentTimeMillis()}.mp4").absolutePath
            val finalMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = finalMuxer
            
            var videoTrackIdx = -1
            var audioTrackIdx = -1
            val muxerStarted = java.util.concurrent.atomic.AtomicBoolean(false)
            
            val videoFormat = MediaFormat.createVideoFormat("video/avc", 720, 1280).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, 2000000)
                setInteger(MediaFormat.KEY_FRAME_RATE, 15)
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
                            videoTrackIdx = finalMuxer.addTrack(vf)
                            audioTrackIdx = finalMuxer.addTrack(audioFormat)
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
                                    finalMuxer.writeSampleData(videoTrackIdx, buf, bufferInfo)
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
            
            val audioThread = thread {
                try {
                    val extractor = MediaExtractor().apply { setDataSource(finalAudioPath) }
                    extractor.selectTrack(0)
                    val buf = ByteBuffer.allocate(1024 * 1024)
                    val info = MediaCodec.BufferInfo()
                    while (threadError == null) {
                        val size = extractor.readSampleData(buf, 0)
                        if (size < 0) break
                        val pts = extractor.sampleTime
                        info.offset = 0
                        info.size = size
                        info.flags = if ((extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                            MediaCodec.BUFFER_FLAG_KEY_FRAME
                        } else 0
                        info.presentationTimeUs = pts
                        
                        while (!muxerStarted.get() && drainLatch.count > 0 && threadError == null) {
                            Thread.sleep(10)
                        }
                        if (threadError != null) break
                        if (muxerStarted.get()) {
                            synchronized(finalMuxer) {
                                finalMuxer.writeSampleData(audioTrackIdx, buf, info)
                            }
                        }
                        extractor.advance()
                    }
                    extractor.release()
                } catch (e: Exception) {
                    threadError = e
                    e.printStackTrace()
                }
            }
            
            var videoPtsUs = 0L
            val fps = 15
            val frameDurationUs = 1000000L / fps
            val totalFrames = (totalDurationUs / frameDurationUs).toInt()
            
            for (frameIdx in 0 until totalFrames) {
                if (isCancelled || threadError != null) break
                
                if (frameIdx % 15 == 0) {
                    val prog = 0.5f + (0.5f * (frameIdx.toFloat() / totalFrames.toFloat()))
                    onProgress(if (isArabic) "جاري تصوير وتجميع المقطع..." else "Rendering video frames...", prog)
                }
                
                val currentMs = (videoPtsUs / 1000)
                
                // Find active chunk
                val activeChunk = chunks.find { currentMs in it.startTimeMs..it.endTimeMs }
                
                val textToDraw = activeChunk?.arabic ?: ""
                val transToDraw = activeChunk?.english // null for now
                
                val bgBitmap = bgDecoder?.getNextFrame()
                val bitmap = createVerseBitmap(
                    text = textToDraw,
                    translation = transToDraw,
                    bgBitmap = bgBitmap,
                    context = context,
                    fontFamily = fontFamily,
                    textFontSize = textFontSize,
                    textColorStr = textColorStr,
                    textOpacity = textOpacity,
                    showTextBg = showTextBg,
                    textBgColorStr = textBgColorStr,
                    textBgOpacity = textBgOpacity,
                    textBgRadius = textBgRadius,
                    textPosition = textPosition,
                    translationFontSize = translationFontSize,
                    translationColorStr = translationColorStr
                )
                
                var inIdx = -1
                while (inIdx < 0 && threadError == null) {
                    inIdx = encoder.dequeueInputBuffer(50000)
                }
                if (inIdx >= 0) {
                    val img = encoder.getInputImage(inIdx)!!
                    fillImageFromBitmap(img, bitmap)
                    encoder.queueInputBuffer(inIdx, 0, img.planes[0].buffer.capacity() * 3/2, videoPtsUs, 0)
                    videoPtsUs += frameDurationUs
                }
                bitmap.recycle()
                bgBitmap?.recycle()
            }
            
            var eosIdx = -1
            while (eosIdx < 0) {
                if (threadError != null) {
                    throw Exception("خطأ في قنوات المعالجة الخلفية: ${threadError?.localizedMessage}")
                }
                eosIdx = encoder.dequeueInputBuffer(50000)
            }
            encoder.queueInputBuffer(eosIdx, 0, 0, videoPtsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            
            val drainCompleted = drainLatch.await(5, TimeUnit.MINUTES)
            if (!drainCompleted) {
                throw Exception("توقيت معالجة الفيديو انتهى دون استجابة الترميز")
            }
            audioThread.join(10000)
            
            SystemDiagnosticTracker.addLog("COMPLETE", "اكتملت العملية بنجاح!")
            onComplete(Uri.fromFile(File(outputPath)))
            
        } catch (e: Exception) {
            e.printStackTrace()
            SystemDiagnosticTracker.addLog("ERROR", e.message ?: "حدث خطأ غير معروف", "ERROR")
            val errorMsg = e.message ?: "حدث خطأ غير معروف في صانع المقطع"
            withContext(Dispatchers.Main) { onError(errorMsg) }
        } finally {
            bgDecoder?.release()
            try { videoCodec?.stop(); videoCodec?.release() } catch(e: Exception) {}
            try { muxer?.stop(); muxer?.release() } catch(e: Exception) {}
        }
    }

    private fun fetchVerseInfo(surah: Int, ayah: Int, edition: String): Pair<String, Int> {
        val url = "https://api.alquran.cloud/v1/ayah/$surah:$ayah/$edition"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("فشل تحميل نصوص الآيات من الخادم")
        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        val data = json.getJSONObject("data")
        return Pair(data.getString("text"), data.getInt("number"))
    }

    private fun downloadAudio(url: String, destFile: File) {
        if (destFile.exists() && destFile.length() > 0) return
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("فشل تحميل الملفات الصوتية المحددة")
        response.body?.byteStream()?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun transcodeMp3ToAac(inputPath: String, outputPath: String) {
        val extractor = MediaExtractor().apply { setDataSource(inputPath) }
        if (extractor.trackCount == 0) {
            extractor.release()
            throw Exception("ملف الصوت فارغ أو غير صالح للاستخدام")
        }
        extractor.selectTrack(0)
        val inputFormat = extractor.getTrackFormat(0)
        val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: "audio/mpeg"
        
        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(inputFormat, null, null, 0)
        decoder.start()
        
        val sampleRate = if (inputFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
        val channelCount = if (inputFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1
        val outputFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, 128000)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
        }
        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
        
        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var outTrackIdx = -1
        
        val decoderBufferInfo = MediaCodec.BufferInfo()
        val encoderBufferInfo = MediaCodec.BufferInfo()
        
        var isExtractorEOS = false
        var isDecoderEOS = false
        var isEncoderEOS = false
        
        val timeoutUs = 5000L
        var muxerStarted = false
        
        while (!isEncoderEOS) {
            if (!isExtractorEOS) {
                val inIdx = decoder.dequeueInputBuffer(timeoutUs)
                if (inIdx >= 0) {
                    val buf = decoder.getInputBuffer(inIdx)!!
                    val size = extractor.readSampleData(buf, 0)
                    if (size < 0) {
                        decoder.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isExtractorEOS = true
                    } else {
                        decoder.queueInputBuffer(inIdx, 0, size, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }
            
            if (!isDecoderEOS) {
                val outIdx = decoder.dequeueOutputBuffer(decoderBufferInfo, timeoutUs)
                if (outIdx >= 0) {
                    val buf = decoder.getOutputBuffer(outIdx)!!
                    val size = decoderBufferInfo.size
                    
                    val encInIdx = encoder.dequeueInputBuffer(timeoutUs)
                    if (encInIdx >= 0) {
                        val encBuf = encoder.getInputBuffer(encInIdx)!!
                        encBuf.clear()
                        if (size > 0) {
                            buf.position(decoderBufferInfo.offset)
                            buf.limit(decoderBufferInfo.offset + size)
                            encBuf.put(buf)
                        }
                        
                        val flags = if ((decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            isDecoderEOS = true
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        } else 0
                        encoder.queueInputBuffer(encInIdx, 0, size, decoderBufferInfo.presentationTimeUs, flags)
                    }
                    decoder.releaseOutputBuffer(outIdx, false)
                }
            }
            
            val encOutIdx = encoder.dequeueOutputBuffer(encoderBufferInfo, timeoutUs)
            if (encOutIdx >= 0) {
                val buf = encoder.getOutputBuffer(encOutIdx)!!
                if ((encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    encoderBufferInfo.size = 0
                }
                
                if (encoderBufferInfo.size > 0 && outTrackIdx >= 0) {
                    buf.position(encoderBufferInfo.offset)
                    buf.limit(encoderBufferInfo.offset + encoderBufferInfo.size)
                    muxer.writeSampleData(outTrackIdx, buf, encoderBufferInfo)
                }
                
                if ((encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    isEncoderEOS = true
                }
                encoder.releaseOutputBuffer(encOutIdx, false)
            } else if (encOutIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                outTrackIdx = muxer.addTrack(encoder.outputFormat)
                muxer.start()
                muxerStarted = true
            }
        }
        
        try { decoder.stop(); decoder.release() } catch (e: Exception) {}
        try { encoder.stop(); encoder.release() } catch (e: Exception) {}
        try { if (muxerStarted) muxer.stop(); muxer.release() } catch (e: Exception) {}
        try { extractor.release() } catch (e: Exception) {}
    }

    private fun createVerseBitmap(
        text: String,
        translation: String?,
        bgBitmap: Bitmap?,
        context: Context,
        fontFamily: String,
        textFontSize: Int,
        textColorStr: String,
        textOpacity: Float,
        showTextBg: Boolean,
        textBgColorStr: String,
        textBgOpacity: Float,
        textBgRadius: Int,
        textPosition: String,
        translationFontSize: Int,
        translationColorStr: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        if (bgBitmap != null) {
            val src = android.graphics.Rect(0, 0, bgBitmap.width, bgBitmap.height)
            val dst = android.graphics.Rect(0, 0, 720, 1280)
            canvas.drawBitmap(bgBitmap, src, dst, null)
            canvas.drawColor(Color.argb(140, 0, 0, 0))
        } else {
            canvas.drawColor(Color.parseColor("#0F0F14"))
        }
        
        if (text.isBlank()) return bitmap // Don't draw text if chunk is empty

        val tf = when (fontFamily) {
            "Amiri" -> Typeface.create("serif", Typeface.NORMAL)
            "Cairo" -> Typeface.create("sans-serif", Typeface.NORMAL)
            "Monospace" -> Typeface.MONOSPACE
            else -> Typeface.DEFAULT_BOLD
        }
        
        val tColor = try {
            Color.parseColor(textColorStr)
        } catch (e: Exception) {
            Color.WHITE
        }
        val alpha = (textOpacity * 255).toInt().coerceIn(0, 255)
        val finalTextColor = Color.argb(alpha, Color.red(tColor), Color.green(tColor), Color.blue(tColor))
        
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = finalTextColor
            textAlign = Paint.Align.CENTER
            typeface = tf
            this.textSize = textFontSize.toFloat() * 1.8f
            setShadowLayer(8f, 0f, 4f, Color.argb(200, 0, 0, 0))
        }
        
        val sl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, 620)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.4f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, textPaint, 620, Layout.Alignment.ALIGN_CENTER, 1.4f, 0f, false)
        }
        
        val transColor = try {
            Color.parseColor(translationColorStr)
        } catch (e: Exception) {
            Color.parseColor("#E0E0E0")
        }
        
        val transPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = transColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            this.textSize = translationFontSize.toFloat() * 1.8f
            setShadowLayer(8f, 0f, 4f, Color.argb(200, 0, 0, 0))
        }
        
        val transSl: StaticLayout? = if (translation != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(translation, 0, translation.length, transPaint, 620)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(translation, transPaint, 620, Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)
            }
        } else {
            null
        }

        val totalHeight = sl.height + (transSl?.height?.plus(60f) ?: 0f)
        
        val startY = when (textPosition) {
            "Top" -> 150f
            "Bottom" -> 1280f - totalHeight - 200f
            else -> (1280f - totalHeight) / 2f
        }
        
        if (showTextBg) {
            val bgColor = try { Color.parseColor(textBgColorStr) } catch (e: Exception) { Color.BLACK }
            val bgAlpha = (textBgOpacity * 255).toInt().coerceIn(0, 255)
            val finalBgColor = Color.argb(bgAlpha, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor))
            
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = finalBgColor
                style = Paint.Style.FILL
            }
            
            val boxWidth = 660f
            val boxHeight = totalHeight + 84f
            val boxLeft = 360f - boxWidth / 2f
            val boxTop = startY - 42f
            val boxRight = boxLeft + boxWidth
            val boxBottom = boxTop + boxHeight
            
            val rect = android.graphics.RectF(boxLeft, boxTop, boxRight, boxBottom)
            val radius = textBgRadius.toFloat() * 1.5f
            canvas.drawRoundRect(rect, radius, radius, bgPaint)
        }
        
        canvas.save()
        canvas.translate(360f, startY)
        sl.draw(canvas)
        canvas.restore()
        
        if (transSl != null) {
            canvas.save()
            canvas.translate(360f, startY + sl.height + 60f)
            transSl.draw(canvas)
            canvas.restore()
        }
        
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
}
"""

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(new_code)
