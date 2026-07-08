import re
with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "r") as f:
    content = f.read()

# Add a simple cache function
cache_code = """
    private fun getCacheFile(context: Context, url: String): java.io.File {
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
        return java.io.File(context.cacheDir, "whisper_cache_$hash.json")
    }
"""
if "private fun getCacheFile" not in content:
    content = content.replace("class GeminiMetaGenerator {", "class GeminiMetaGenerator {\n" + cache_code)

# Replace the whisperX part
whisper_target = """        if (!skipWhisperX) {
            SystemDiagnosticTracker.addLog("GEMINI", "Calling WhisperX for audio transcription of URL: $videoUrl")
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
                    SystemDiagnosticTracker.addLog("WHISPER", "Transcription successful: ${transcription.take(50)}...")
                }
            } catch (e: Exception) {
                whisperError = e.message ?: "Unknown"
                SystemDiagnosticTracker.addLog("WHISPER", "Failed to extract text: ${e.message}")
            }
        }"""

whisper_replacement = """        val cacheFile = getCacheFile(context, videoUrl)
        if (skipWhisperX) {
            SystemDiagnosticTracker.addLog("GEMINI", "ميزة تخطي WhisperX مفعلة. جاري فحص وجود معلومات مخزنة مسبقاً...")
            if (cacheFile.exists()) {
                try {
                    val cachedJson = JSONObject(cacheFile.readText())
                    transcription = cachedJson.optString("transcription", "")
                    videoInfo = cachedJson.optString("videoInfo", "")
                    SystemDiagnosticTracker.addLog("GEMINI", "تم العثور على معلومات مخزنة! تخطي المعالجة الصوتية.")
                } catch (e: Exception) {
                    return@withContext ClipAnalysisResult(0f, "", "حدث خطأ في قراءة المعلومات المخزنة", error = "بيانات مخزنة تالفة")
                }
            } else {
                SystemDiagnosticTracker.addLog("GEMINI", "❌ لم يتم العثور على أي معلومات مخزنة لهذا الرابط. إيقاف العملية.")
                return@withContext ClipAnalysisResult(0f, "", "ليست هناك اي معلومات لهذا الرابط. يرجى إيقاف ميزة التخطي وإعادة المحاولة.", error = "NO_CACHE")
            }
        } else {
            SystemDiagnosticTracker.addLog("GEMINI", "Calling WhisperX for audio transcription of URL: $videoUrl")
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
                    SystemDiagnosticTracker.addLog("WHISPER", "Transcription successful: ${transcription.take(50)}...")
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
                SystemDiagnosticTracker.addLog("WHISPER", "Failed to extract text: ${e.message}")
            }
        }"""

content = content.replace(whisper_target, whisper_replacement)

# Fix Gemini parsing and log raw response
gemini_parsing_target = """                        var rawText = parts.getJSONObject(0).getString("text").trim()
                        if (rawText.startsWith("```json")) {
                            rawText = rawText.substringAfter("```json").substringBeforeLast("```").trim()
                        } else if (rawText.startsWith("```")) {
                            rawText = rawText.substringAfter("```").substringBeforeLast("```").trim()
                        }
                        
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
                        )"""

gemini_parsing_replacement = """                        var rawText = parts.getJSONObject(0).getString("text").trim()
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
                            return@withContext ClipAnalysisResult(0f, "", "فشل تحليل معلومات المقطع من الذكاء الاصطناعي", error = "JSON_PARSE_ERROR")
                        }"""

content = content.replace(gemini_parsing_target, gemini_parsing_replacement)

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "w") as f:
    f.write(content)

