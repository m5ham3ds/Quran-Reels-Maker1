import re
with open("app/src/main/java/com/example/ui/PopularClipsScreen.kt", "r") as f:
    content = f.read()

target = """                                                                    if (cachedFile.exists()) cachedFile.delete()
                                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        Toast.makeText(context, if (isArabic) "جاري جلب العينة من المنصة..." else "Fetching sample from platform...", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                    val whisperClient = com.example.generator.WhisperXClient()
                                                                    val result = whisperClient.processAudio(null, clip.audioUrl, "") { _ -> }
                                                                    if (result.audioUrl.isNotBlank()) {
                                                                        val request = okhttp3.Request.Builder().url(result.audioUrl).build()
                                                                        val client = okhttp3.OkHttpClient()
                                                                        val response = client.newCall(request).execute()
                                                                        if (response.isSuccessful && response.body != null) {
                                                                            val inputStream = response.body!!.byteStream()
                                                                            val outputStream = java.io.FileOutputStream(cachedFile)
                                                                            inputStream.copyTo(outputStream)
                                                                            outputStream.close()
                                                                            inputStream.close()
                                                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                                previewPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(cachedFile)))
                                                                                previewPlayer.prepare()
                                                                                previewPlayer.playWhenReady = true
                                                                            }
                                                                        } else {
                                                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                                Toast.makeText(context, if (isArabic) "فشل تنزيل العينة" else "Failed to download sample", Toast.LENGTH_SHORT).show()
                                                                                playingClipId = null
                                                                                isPreviewLoading = false
                                                                            }
                                                                        }
                                                                    } else {
                                                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                            Toast.makeText(context, if (isArabic) "فشل استخراج العينة" else "Failed to extract sample", Toast.LENGTH_SHORT).show()
                                                                            playingClipId = null
                                                                            isPreviewLoading = false
                                                                        }
                                                                    }"""

replacement = """                                                                    if (cachedFile.exists()) cachedFile.delete()
                                                                    com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE", "بدء جلب عينة المقطع من الرابط: ${clip.audioUrl}")
                                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        Toast.makeText(context, if (isArabic) "جاري جلب العينة من المنصة..." else "Fetching sample from platform...", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                    val whisperClient = com.example.generator.WhisperXClient()
                                                                    val result = whisperClient.processAudio(null, clip.audioUrl, "") { progress -> 
                                                                        com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE_WHISPER", progress)
                                                                    }
                                                                    if (result.audioUrl.isNotBlank()) {
                                                                        com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE", "تم الحصول على رابط الصوت: ${result.audioUrl}. جاري التنزيل...")
                                                                        val request = okhttp3.Request.Builder().url(result.audioUrl).build()
                                                                        val client = okhttp3.OkHttpClient()
                                                                        val response = client.newCall(request).execute()
                                                                        if (response.isSuccessful && response.body != null) {
                                                                            val totalBytes = response.body!!.contentLength()
                                                                            val inputStream = response.body!!.byteStream()
                                                                            val outputStream = java.io.FileOutputStream(cachedFile)
                                                                            
                                                                            val buffer = ByteArray(8 * 1024)
                                                                            var bytesRead: Int
                                                                            var downloadedBytes = 0L
                                                                            var lastProgress = 0
                                                                            
                                                                            while (inputStream.read(buffer).also { bytesRead = it } >= 0) {
                                                                                outputStream.write(buffer, 0, bytesRead)
                                                                                downloadedBytes += bytesRead
                                                                                if (totalBytes > 0) {
                                                                                    val progress = ((downloadedBytes.toFloat() / totalBytes) * 100).toInt()
                                                                                    if (progress - lastProgress >= 10 || progress == 100) {
                                                                                        com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE_DOWNLOAD", "تنزيل العينة: $progress%")
                                                                                        lastProgress = progress
                                                                                    }
                                                                                }
                                                                            }
                                                                            outputStream.close()
                                                                            inputStream.close()
                                                                            
                                                                            com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE", "تم تنزيل العينة بنجاح. يتم حفظها لمدة ساعة ثم ستُحذف تلقائياً.")
                                                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                                previewPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(cachedFile)))
                                                                                previewPlayer.prepare()
                                                                                previewPlayer.playWhenReady = true
                                                                            }
                                                                        } else {
                                                                            com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE", "فشل تنزيل العينة. رمز الخطأ: ${response.code}")
                                                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                                Toast.makeText(context, if (isArabic) "فشل تنزيل العينة" else "Failed to download sample", Toast.LENGTH_SHORT).show()
                                                                                playingClipId = null
                                                                                isPreviewLoading = false
                                                                            }
                                                                        }
                                                                    } else {
                                                                        com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE", "فشل استخراج العينة، رابط الصوت فارغ.")
                                                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                            Toast.makeText(context, if (isArabic) "فشل استخراج العينة" else "Failed to extract sample", Toast.LENGTH_SHORT).show()
                                                                            playingClipId = null
                                                                            isPreviewLoading = false
                                                                        }
                                                                    }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/PopularClipsScreen.kt", "w") as f:
    f.write(content)

