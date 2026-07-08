import re

with open('app/src/main/java/com/example/ui/PopularClipsScreen.kt', 'r') as f:
    content = f.read()

# Replace caching logic
content = content.replace(
'''val safeId = clip.id.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                                                                val cachedFile = java.io.File(context.cacheDir, "sample_$safeId.mp3")
                                                                val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
                                                                
                                                                if (cachedFile.exists() && cachedFile.lastModified() > oneHourAgo) {
                                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        previewPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(cachedFile)))
                                                                        previewPlayer.prepare()
                                                                        previewPlayer.playWhenReady = true
                                                                    }
                                                                } else {
                                                                    if (cachedFile.exists()) cachedFile.delete()''',
'''val safeId = clip.id.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                                                                val cachedFiles = context.cacheDir.listFiles { _, name -> name.startsWith("sample_$safeId.") }
                                                                val cachedFile = cachedFiles?.firstOrNull()
                                                                val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
                                                                
                                                                if (cachedFile != null && cachedFile.exists() && cachedFile.lastModified() > oneHourAgo && cachedFile.length() > 1024) {
                                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        val uri = android.net.Uri.fromFile(cachedFile)
                                                                        com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE", "تشغيل من الذاكرة المؤقتة: ${cachedFile.absolutePath}")
                                                                        previewPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                                                                        previewPlayer.prepare()
                                                                        previewPlayer.playWhenReady = true
                                                                    }
                                                                } else {
                                                                    cachedFiles?.forEach { it.delete() }'''
)

content = content.replace(
'''val totalBytes = response.body!!.contentLength()
                                                                            val inputStream = response.body!!.byteStream()
                                                                            val outputStream = java.io.FileOutputStream(cachedFile)''',
'''val totalBytes = response.body!!.contentLength()
                                                                            if (totalBytes < 1000 && totalBytes > 0) {
                                                                                com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE", "تحذير: الملف صغير جداً ($totalBytes بايت)، قد يكون صفحة خطأ.")
                                                                            }
                                                                            val extension = result.audioUrl.substringAfterLast('.', "mp3").take(4).replace(Regex("[^a-zA-Z0-9]"), "")
                                                                            val finalCachedFile = java.io.File(context.cacheDir, "sample_$safeId.$extension")
                                                                            val inputStream = response.body!!.byteStream()
                                                                            val outputStream = java.io.FileOutputStream(finalCachedFile)'''
)

content = content.replace(
'''kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                                previewPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(cachedFile)))
                                                                                previewPlayer.prepare()
                                                                                previewPlayer.playWhenReady = true
                                                                            }''',
'''kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                                val uri = android.net.Uri.fromFile(finalCachedFile)
                                                                                com.example.generator.SystemDiagnosticTracker.addLog("SAMPLE", "بدء التشغيل عبر ExoPlayer للملف: ${finalCachedFile.absolutePath}")
                                                                                previewPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                                                                                previewPlayer.prepare()
                                                                                previewPlayer.playWhenReady = true
                                                                            }'''
)

with open('app/src/main/java/com/example/ui/PopularClipsScreen.kt', 'w') as f:
    f.write(content)

