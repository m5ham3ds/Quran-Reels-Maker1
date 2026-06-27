package com.example.generator

import android.content.Context
import android.util.Log
import com.example.settings.SettingsManager
import kotlinx.coroutines.flow.first
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoogleDriveSheetsPublisher(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .build()
            chain.proceed(request)
        }
        .build()
    private val settingsManager = SettingsManager(context)

    companion object {
        private const val TAG = "GooglePublisher"
    }

    /**
     * Publishes a completed video to Google Drive and logs metadata to Google Sheets.
     * Returns a pair of (Drive Video Link, Sheet Row Status) or null if not enabled/failed.
     */
    suspend fun publishReel(
        videoFile: File,
        surahName: String,
        ayahRange: String,
        reciterName: String,
        description: String
    ): Pair<String, String>? {
        // Step 1: Check if Google Drive & Sheets integration is linked and enabled
        val linked = settingsManager.googleDriveSheetsLinked.first()
        val accessToken = settingsManager.googleOauthAccessToken.first().trim()

        if (!linked || accessToken.isBlank()) {
            Log.d(TAG, "Google Drive & Sheets integration is not linked or has empty token.")
            return null
        }

        try {
            Log.d(TAG, "Starting Google Publisher workflow...")

            // Step 2: Get or create Google Drive Folder
            var folderId = settingsManager.googleDriveFolderId.first().trim()
            if (folderId.isBlank()) {
                Log.d(TAG, "Folder ID is empty, attempting to create default 'Quran Reels' folder...")
                val createdFolderId = createDriveFolder(accessToken, "Quran Reels")
                if (createdFolderId != null) {
                    folderId = createdFolderId
                    settingsManager.setGoogleDriveFolderId(folderId)
                    Log.d(TAG, "Successfully created and saved default Google Drive folder: $folderId")
                }
            }

            // Step 3: Upload Video to Google Drive
            val videoName = "Quran_Reel_${surahName.replace(" ", "_")}_$ayahRange.mp4"
            Log.d(TAG, "Uploading video $videoName to Google Drive inside folder '$folderId'...")
            val driveFileId = uploadFileToDrive(accessToken, videoFile, videoName, folderId)
            if (driveFileId == null) {
                throw Exception("Failed to upload video to Google Drive. Check authorization.")
            }

            // Share the drive file publicly so it can be streamed/downloaded directly
            makeFilePubliclyReadable(accessToken, driveFileId)
            val driveVideoLink = "https://drive.google.com/file/d/$driveFileId/view?usp=drivesdk"
            Log.d(TAG, "Video uploaded successfully. Shareable link: $driveVideoLink")

            // Step 4: Get or create Google Spreadsheet ID
            var spreadsheetId = settingsManager.googleSpreadsheetId.first().trim()
            if (spreadsheetId.isBlank()) {
                Log.d(TAG, "Spreadsheet ID is empty, creating default 'Quran Reels Archive' Sheet...")
                val createdSheetId = createGoogleSpreadsheet(accessToken, "Quran Reels Archive")
                if (createdSheetId != null) {
                    spreadsheetId = createdSheetId
                    settingsManager.setGoogleSpreadsheetId(spreadsheetId)
                    Log.d(TAG, "Created default spreadsheet: $spreadsheetId. Setting up header row...")
                    appendRowToSheet(
                        accessToken,
                        spreadsheetId,
                        listOf("Timestamp", "Surah / Topic", "Ayah Range", "Reciter", "Google Drive Video Link", "Gemini AI Post Description")
                    )
                }
            }

            // Step 5: Append Reel details to Sheet row
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val valuesRow = listOf(
                timestamp,
                surahName,
                ayahRange,
                reciterName,
                driveVideoLink,
                description
            )
            Log.d(TAG, "Appending row details to sheet $spreadsheetId...")
            val appendOk = appendRowToSheet(accessToken, spreadsheetId, valuesRow)

            val statusMessage = if (appendOk) {
                "Synced successfully to Google Sheet!"
            } else {
                "Video uploaded to Drive, but row append failed."
            }

            return Pair(driveVideoLink, statusMessage)

        } catch (e: Exception) {
            Log.e(TAG, "Error in Google Drive/Sheets Publisher: ${e.message}", e)
            throw e
        }
    }

    /**
     * Creates a folder in Google Drive and returns the resulting folderId
     */
    private fun createDriveFolder(accessToken: String, folderName: String): String? {
        val url = "https://www.googleapis.com/drive/v3/files"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonPayload = """
            {
              "name": "$folderName",
              "mimeType": "application/vnd.google-apps.folder"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(jsonPayload.toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                return extractJsonValue(responseString, "id")
            } else {
                Log.e(TAG, "createDriveFolder failed: ${response.code} ${response.message} -> $responseString")
            }
        }
        return null
    }

    /**
     * Uploads the video file to Gmail/Google Drive
     */
    private fun uploadFileToDrive(accessToken: String, file: File, fileName: String, parentFolderId: String?): String? {
        val url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
        val mediaTypeJson = "application/json; charset=UTF-8".toMediaType()
        val mediaTypeVideo = "video/mp4".toMediaType()

        val parentSection = if (!parentFolderId.isNullOrBlank()) {
            ", \"parents\": [\"$parentFolderId\"]"
        } else {
            ""
        }

        val metadata = """
            {
              "name": "$fileName",
              "mimeType": "video/mp4"$parentSection
            }
        """.trimIndent()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(
                Headers.Builder().add("Content-Type", "application/json; charset=UTF-8").build(),
                metadata.toRequestBody(mediaTypeJson)
            )
            .addPart(
                Headers.Builder().add("Content-Type", "video/mp4").build(),
                file.asRequestBody(mediaTypeVideo)
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                return extractJsonValue(responseString, "id")
            } else {
                Log.e(TAG, "uploadFileToDrive failed: ${response.code} ${response.message} -> $responseString")
            }
        }
        return null
    }

    /**
     * Changes permission of Drive file so anyone with the link can view it
     */
    private fun makeFilePubliclyReadable(accessToken: String, fileId: String): Boolean {
        val url = "https://www.googleapis.com/drive/v3/files/$fileId/permissions"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonPayload = """
            {
              "role": "reader",
              "type": "anyone"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(jsonPayload.toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return true
            } else {
                val responseString = response.body?.string() ?: ""
                Log.e(TAG, "makeFilePublic failed: ${response.code} ${response.message} -> $responseString")
            }
        }
        return false
    }

    /**
     * Creates a Google Spreadsheet and returns the resulting spreadsheetId
     */
    private fun createGoogleSpreadsheet(accessToken: String, title: String): String? {
        val url = "https://sheets.googleapis.com/v4/spreadsheets"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonPayload = """
            {
              "properties": {
                "title": "$title"
              }
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(jsonPayload.toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                return extractJsonValue(responseString, "spreadsheetId")
            } else {
                Log.e(TAG, "createGoogleSpreadsheet failed: ${response.code} ${response.message} -> $responseString")
            }
        }
        return null
    }

    /**
     * Appends a row of values to a Google Sheet
     */
    private fun appendRowToSheet(accessToken: String, spreadsheetId: String, rowValues: List<String>): Boolean {
        val range = "Sheet1!A:F"
        val url = "https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/$range:append?valueInputOption=USER_ENTERED"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        // Safely escape double quotes and linebreaks in inputs
        val escapedValues = rowValues.map {
            it.replace("\\", "\\\\")
              .replace("\"", "\\\"")
              .replace("\n", "\\n")
              .replace("\r", "")
        }

        val rowArrayString = escapedValues.joinToString(prefix = "\"", postfix = "\"", separator = "\", \"")

        val jsonPayload = """
            {
              "range": "$range",
              "majorDimension": "ROWS",
              "values": [
                [$rowArrayString]
              ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(jsonPayload.toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return true
            } else {
                val responseString = response.body?.string() ?: ""
                Log.e(TAG, "appendRowToSheet failed: ${response.code} ${response.message} -> $responseString")
            }
        }
        return false
    }

    /**
     * Safe Extraction of simple string values from JSON
     */
    private fun extractJsonValue(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }
}
