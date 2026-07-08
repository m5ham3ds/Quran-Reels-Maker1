            if (chunkIndexToReplace != -1 && downloadedVideoFiles.isNotEmpty() && chunkIndexToReplace < downloadedVideoFiles.size) {
                SystemDiagnosticTracker.addLog("BACKGROUND", "استبدال الخلفية للمقطع رقم $chunkIndexToReplace")
                onProgress(if (isArabic) "جاري البحث عن خلفية جديدة للمقطع..." else "Searching for a new background...", 0.3f)
                try {
                    val allChunks = verses.flatMap { if (it.chunks.isEmpty()) listOf(SmartChunk("", null, 0, (it.durationUs/1000).toLong())) else it.chunks }
                    val chunk = allChunks[chunkIndexToReplace]
                    val neededDurSec = ((chunk.endTimeMs - chunk.startTimeMs) / 1000L).toInt() + 2
                    
                    val pexelsQueries = if (backgroundKeywords.isNotEmpty()) backgroundKeywords.toList() else listOf(
                        "islamic+aesthetics+kaaba+mecca", "dark+cinematic+aesthetic+landscape", "stormy+aesthetic+rainy+window",
                        "moonlight+trees+dark+night", "epic+sunset+clouds+aesthetic", "snowy+mountains+cinematic",
                        "rain+flowers+nature+aesthetic", "sunset+bike+nature+dark"
                    )
                    val chosenQuery = if (!videoQuery.isNullOrBlank()) videoQuery else pexelsQueries.random().replace(" ", "+")
                    
                    var selectedVideoUrl: String? = null
                    if (pexelsApiKey.isNotBlank()) {
                        val requestUrl = "https://api.pexels.com/videos/search?query=$chosenQuery&orientation=portrait&per_page=30"
                        val request = Request.Builder().url(requestUrl).addHeader("Authorization", pexelsApiKey).build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val json = JSONObject(response.body?.string() ?: "")
                            val videos = json.getJSONArray("videos")
                            val validVideos = mutableListOf<JSONObject>()
                            for (i in 0 until videos.length()) validVideos.add(videos.getJSONObject(i))
                            validVideos.shuffle()
                            val selected = validVideos.firstOrNull { it.optInt("duration", 0) >= neededDurSec } ?: validVideos.firstOrNull()
                            
                            if (selected != null) {
                                val videoFiles = selected.getJSONArray("video_files")
                                val mp4Files = mutableListOf<JSONObject>()
                                for(v in 0 until videoFiles.length()) {
                                    val f = videoFiles.getJSONObject(v)
                                    if (f.getString("link").contains("mp4", ignoreCase = true)) mp4Files.add(f)
                                }
                                val portraitFiles = mp4Files.filter { it.optInt("width", 0) < it.optInt("height", 0) }
                                val targetList = if(portraitFiles.isNotEmpty()) portraitFiles else mp4Files
                                if (targetList.isNotEmpty()) {
                                    val sortedFiles = targetList.sortedBy { it.optInt("width", 0) * it.optInt("height", 0) }
                                    selectedVideoUrl = sortedFiles.last().getString("link")
                                }
                            }
                        }
                    }
                    
                    if (selectedVideoUrl != null) {
                        onProgress(if (isArabic) "جاري تحميل الخلفية الجديدة..." else "Downloading new background...", 0.35f)
                        val targetFile = File(context.cacheDir, "bg_video_$chunkIndexToReplace.mp4")
                        targetFile.delete()
                        downloadAudio(selectedVideoUrl, targetFile)
                        downloadedVideoFiles[chunkIndexToReplace] = targetFile
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
