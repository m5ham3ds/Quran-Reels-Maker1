package com.example.generator

import android.content.Context
import java.io.File

class GoogleDriveSheetsPublisher(val context: Context) {
    suspend fun publishReel(
        videoFile: File,
        surahName: String,
        ayahRange: String,
        reciterName: String,
        description: String
    ): Pair<String, String>? {
        return Pair("https://drive.google.com/...", "https://docs.google.com/...")
    }
}
