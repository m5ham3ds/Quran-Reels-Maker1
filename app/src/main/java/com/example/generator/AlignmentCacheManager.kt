package com.example.generator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

object AlignmentCacheManager {
    private const val CACHE_EXPIRY_MS = 60 * 60 * 1000L // 1 hour
    private const val CACHE_DIR_NAME = "alignment_cache"

    data class CachedAlignment(
        val wordSegments: List<WordSegment>,
        val smartChunks: List<SmartChunk>,
        val audioPath: String?
    )

    private fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun generateKey(mediaUrl: String?, text: String): String {
        val rawKey = (mediaUrl ?: "") + "_" + text
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawKey.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getCachedAlignment(context: Context, mediaUrl: String?, text: String): CachedAlignment? {
        val key = generateKey(mediaUrl, text)
        val cacheFile = File(getCacheDir(context), "$key.json")
        if (!cacheFile.exists()) return null

        if (System.currentTimeMillis() - cacheFile.lastModified() > CACHE_EXPIRY_MS) {
            cacheFile.delete()
            return null
        }

        try {
            val jsonStr = cacheFile.readText()
            val jsonObj = JSONObject(jsonStr)
            
            val wordSegments = mutableListOf<WordSegment>()
            val wordsArray = jsonObj.optJSONArray("wordSegments") ?: JSONArray()
            for (i in 0 until wordsArray.length()) {
                val obj = wordsArray.getJSONObject(i)
                wordSegments.add(
                    WordSegment(
                        wordIndex = obj.getInt("wordIndex"),
                        word = obj.getString("word"),
                        startTimeMs = obj.getLong("startTimeMs"),
                        endTimeMs = obj.getLong("endTimeMs")
                    )
                )
            }

            val smartChunks = mutableListOf<SmartChunk>()
            val chunksArray = jsonObj.optJSONArray("smartChunks") ?: JSONArray()
            for (i in 0 until chunksArray.length()) {
                val obj = chunksArray.getJSONObject(i)
                smartChunks.add(
                    SmartChunk(
                        arabic = obj.getString("arabic"),
                        english = if (obj.has("english")) obj.getString("english") else null,
                        startTimeMs = obj.getLong("startTimeMs"),
                        endTimeMs = obj.getLong("endTimeMs")
                    )
                )
            }

            var audioPath = if (jsonObj.has("audioPath")) jsonObj.getString("audioPath") else null
            if (audioPath != null && !File(audioPath).exists()) {
                audioPath = null
            }

            return CachedAlignment(wordSegments, smartChunks, audioPath)
        } catch (e: Exception) {
            cacheFile.delete()
            return null
        }
    }

    fun putCachedAlignment(context: Context, mediaUrl: String?, text: String, wordSegments: List<WordSegment>, smartChunks: List<SmartChunk>, audioFile: File?) {
        val key = generateKey(mediaUrl, text)
        val cacheFile = File(getCacheDir(context), "$key.json")
        
        try {
            val wordsArray = JSONArray()
            wordSegments.forEach {
                val obj = JSONObject()
                obj.put("wordIndex", it.wordIndex)
                obj.put("word", it.word)
                obj.put("startTimeMs", it.startTimeMs)
                obj.put("endTimeMs", it.endTimeMs)
                wordsArray.put(obj)
            }

            val chunksArray = JSONArray()
            smartChunks.forEach {
                val obj = JSONObject()
                obj.put("arabic", it.arabic)
                if (it.english != null) obj.put("english", it.english)
                obj.put("startTimeMs", it.startTimeMs)
                obj.put("endTimeMs", it.endTimeMs)
                chunksArray.put(obj)
            }

            val jsonObj = JSONObject()
            jsonObj.put("wordSegments", wordsArray)
            jsonObj.put("smartChunks", chunksArray)
            if (audioFile != null && audioFile.exists()) {
                jsonObj.put("audioPath", audioFile.absolutePath)
            }

            cacheFile.writeText(jsonObj.toString())
        } catch (e: Exception) {
            com.example.utils.AppLogger.e("ExceptionCatch", "Exception caught: ${ e.message }", e)
        }
    }

    fun clearCache(context: Context) {
        try {
            val dir = getCacheDir(context)
            dir.listFiles()?.forEach {
                if (it.name.endsWith(".json")) it.delete()
            }
        } catch (e: Exception) {
            com.example.utils.AppLogger.e("ExceptionCatch", "Exception caught: ${ e.message }", e)
        }
    }
}
