import sys

with open('/tmp/Reelss/app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    lines = f.readlines()

bottom_half = "".join(lines[397:]) # From fetchVerseInfo to EOF

top_half = """package com.example.generator

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
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.max

data class VerseData(val text: String, val translation: String?, val audioPath: String, val durationUs: Long)

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
        threadError = null
        isCancelled = false
        var videoCodec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        val decoders = mutableListOf<SequentialFrameDecoder?>()
        
        try {
            val verses = mutableListOf<VerseData>()
            val totalAyahs = endAyah - startAyah + 1
            
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

            val bgFiles = mutableListOf<File>()

            // 1. Fetch text and audio for all verses
            for ((i, ayah) in (startAyah..endAyah).withIndex()) {
                if (isCancelled) return@withContext
                onProgress(if (isArabic) "جاري تحميل الآية $ayah..." else "Fetching Ayah $ayah...", 0.05f + (i * 0.1f / totalAyahs))
                val (text, globalAyahNumber) = fetchVerseInfo(surah, ayah, "quran-uthmani")
                val translation = if (showTranslation) {
                    val (trans, _) = fetchVerseInfo(surah, ayah, "en.asad")
                    trans
                } else null
                
                val audioFileName = "${reciterId}_${surah}_${ayah}.mp3"
                val url = "https://cdn.islamic.network/quran/audio/64/$reciterId/$globalAyahNumber.mp3"
                val destFile = File(context.cacheDir, audioFileName)
                downloadAudio(url, destFile)
                
                val aacFileName = "${reciterId}_${surah}_${ayah}_transcoded.m4a"
                val aacFile = File(context.cacheDir, aacFileName)
                transcodeMp3ToAac(destFile.absolutePath, aacFile.absolutePath)
                
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
                verses.add(VerseData(text, translation, aacFile.absolutePath, durationUs))
            }
            
            if (verses.isEmpty()) throw Exception("لا توجد آيات صالحة لعمل المقطع")

            // 2. Mock SmartChunks and fetch videos for each verse (or chunk)
            val chunks = mutableListOf<SmartChunk>()
            var currentStartTimeMs = 0L
            for (i in 0 until verses.size) {
                val verse = verses[i]
                val durationMs = verse.durationUs / 1000
                chunks.add(SmartChunk(verse.text, verse.translation, currentStartTimeMs, currentStartTimeMs + durationMs))
                currentStartTimeMs += durationMs
                
                // Pexels video fetch for this chunk
                if (isCancelled) return@withContext
                onProgress("Downloading cinematic video for section ${i+1}...", 0.2f + (i * 0.2f / verses.size))
                
                val chunkFile = File(context.cacheDir, "bg_video_$i.mp4")
                if (chunkIndexToReplace == i || !chunkFile.exists()) {
                    // Try Pixabay first, then Pexels, fallback to default
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
                            val output = FileOutputStream(chunkFile)
                            input?.copyTo(output)
                            output.close()
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                bgFiles.add(chunkFile)
                if (chunkFile.exists()) {
                    decoders.add(SequentialFrameDecoder(chunkFile.absolutePath))
                } else {
                    decoders.add(null)
                }
            }

            // 3. MediaCodec setup
            val outputPath = File(context.cacheDir, "quran_reel_${System.currentTimeMillis()}.mp4").absolutePath
            val finalMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = finalMuxer
            
            var videoTrackIdx = -1
            var audioTrackIdx = -1
            val muxerStarted = java.util.concurrent.atomic.AtomicBoolean(false)
            
            val audioFormat = MediaExtractor().apply { setDataSource(verses[0].audioPath) }.apply { selectTrack(0) }.getTrackFormat(0)
            
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
                    var audioPtsUs = 0L
                    for (verse in verses) {
                        if (threadError != null) break
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
                            info.presentationTimeUs = audioPtsUs + pts
                            
                            while (!muxerStarted.get() && drainLatch.count > 0 && threadError == null) {
                                Thread.sleep(10)
                            }
                            if (threadError != null) break
                            if (muxerStarted.get()) {
                                synchronized(finalMuxer) {
                                    finalMuxer.writeSampleData(audioTrackIdx, buf, info)
                                }
                            }
                            ext.advance()
                        }
                        audioPtsUs += verse.durationUs
                        ext.release()
                    }
                } catch (e: Exception) {
                    threadError = e
                    e.printStackTrace()
                }
            }
            
            var videoPtsUs = 0L
            val fps = 15
            val frameDurationUs = 1000000L / fps
            
            for (idx in verses.indices) {
                val verse = verses[idx]
                val chunk = chunks[idx]
                val decoder = decoders.getOrNull(idx)
                
                onProgress(if (isArabic) "جاري تصوير مشهد المقطع ${idx + 1}..." else "Rendering video section ${idx + 1}...", 0.5f + (idx * 0.4f / verses.size))
                
                val framesInVerse = (verse.durationUs / frameDurationUs).toInt()
                
                for (frameIdx in 0 until framesInVerse) {
                    if (isCancelled || threadError != null) break
                    
                    val bgBitmap = decoder?.getNextFrame()
                    val bitmap = createVerseBitmap(
                        text = verse.text,
                        translation = verse.translation,
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
            
            onComplete(Uri.fromFile(File(outputPath)))
            
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = e.message ?: "حدث خطأ غير معروف في صانع المقطع"
            withContext(Dispatchers.Main) { onError(errorMsg) }
        } finally {
            decoders.forEach { it?.release() }
            try { videoCodec?.stop(); videoCodec?.release() } catch(e: Exception) {}
            try { muxer?.stop(); muxer?.release() } catch(e: Exception) {}
        }
    }

"""

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(top_half + bottom_half)
