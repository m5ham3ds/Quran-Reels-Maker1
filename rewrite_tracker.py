import re

content = """package com.example.generator

import com.example.utils.AppLogger
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiagnosticLog(
    val timestamp: Long,
    val severity: String,
    val tag: String,
    val message: String
)

object SystemDiagnosticTracker {
    private val _logs = MutableStateFlow<List<DiagnosticLog>>(emptyList())
    val logs: StateFlow<List<DiagnosticLog>> = _logs.asStateFlow()
    
    private var logFile: File? = null
    
    fun init(context: Context) {
        try {
            val dir = File(context.getExternalFilesDir(null), "DiagnosticLogs")
            if (!dir.exists()) dir.mkdirs()
            logFile = File(dir, "live_log_${System.currentTimeMillis()}.txt")
            logFile?.writeText("=== Live Diagnostic Log Started ===\\n")
        } catch (e: Exception) {
            com.example.utils.AppLogger.e("ExceptionCatch", "Exception caught: ${ e.message }", e)
        }
    }

    fun addLog(tag: String, message: String, severity: String = "INFO") {
        val currentLogs = _logs.value.toMutableList()
        val log = DiagnosticLog(System.currentTimeMillis(), severity, tag, message)
        currentLogs.add(log)
        _logs.value = currentLogs
        
        try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            val line = "[${sdf.format(Date(log.timestamp))}] [${log.severity}] [${log.tag}] ${log.message}\\n"
            logFile?.appendText(line)
        } catch (e: Exception) {}
    }

    fun getLogs(): List<String> {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        return _logs.value.map { "[${sdf.format(Date(it.timestamp))}] [${it.severity}] [${it.tag}] ${it.message}" }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    suspend fun runFullSystemAudit(context: Context, force: Boolean = false): String {
        val sb = java.lang.StringBuilder()
        sb.appendLine("=== تقرير الفحص الشامل لعملية إنشاء الفيديو ===")
        val allLogs = getLogs()
        if (allLogs.isEmpty()) {
            sb.appendLine("لا توجد سجلات حالية للعملية.")
        } else {
            allLogs.forEach { sb.appendLine(it) }
        }
        return sb.toString()
    }

    fun saveReportToFilesAndGetPath(context: Context, extraData: String = ""): String {
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
        val fileName = "diagnostic_report_$timeStamp.txt"
        var finalPath = ""

        val reportContent = buildString {
            append("=== Quran Reels Diagnostic Report ===\\n")
            append("Time: ${java.util.Date()}\\n")
            append(extraData)
            append("\\n\\n")
            append("--- Application Log (AppLogger) ---\\n")
            append(com.example.utils.AppLogger.getLogs())
            append("\\n\\n--- System Logcat Live Dump ---\\n")
            try {
                val process = Runtime.getRuntime().exec("logcat -d")
                val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    append(line).append("\\n")
                }
            } catch(e: Exception) {
                append("Failed to dump logcat: ${e.message}\\n")
            }
            append("\\n\\n--- Process Logs ---\\n")
            for (log in getLogs()) {
                append(log).append("\\n")
            }
        }

        // 1. Try Public Movies Directory directly
        try {
            val moviesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES)
            val quranReelsDir = java.io.File(moviesDir, "Quran Reels/ERROR")
            if (!quranReelsDir.exists()) quranReelsDir.mkdirs()
            val file = java.io.File(quranReelsDir, fileName)
            java.io.PrintWriter(java.io.FileWriter(file)).use { it.print(reportContent) }
            finalPath = file.absolutePath
            com.example.utils.AppLogger.i("SystemDiagnosticTracker", "Report saved via public directory: $finalPath")
            return finalPath
        } catch (e: Exception) {
            com.example.utils.AppLogger.e("SystemDiagnosticTracker", "Failed to save via public directory", e)
        }

        // 2. Try MediaStore fallback
        try {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Movies/Quran Reels/ERROR")
                }
            }
            val uri = context.contentResolver.insert(android.provider.MediaStore.Files.getContentUri("external"), values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    java.io.OutputStreamWriter(out).use { writer ->
                        writer.write(reportContent)
                    }
                }
                finalPath = uri.toString()
                return finalPath
            }
        } catch (e: Exception) {
            com.example.utils.AppLogger.e("SystemDiagnosticTracker", "Failed to save via MediaStore", e)
        }

        // 3. Fallback to App Scoped Directories
        val directoriesToTry = listOfNotNull(
            context.getExternalFilesDir(null)?.let { java.io.File(it, "DiagnosticLogs") },
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)?.let { java.io.File(it, "ERROR") },
            java.io.File(context.filesDir, "ERROR")
        )

        for (dir in directoriesToTry) {
            try {
                if (!dir.exists()) dir.mkdirs()
                val file = java.io.File(dir, fileName)
                java.io.PrintWriter(java.io.FileWriter(file)).use { it.print(reportContent) }
                if (finalPath.isEmpty()) {
                    finalPath = file.absolutePath
                }
                return finalPath
            } catch (e: Exception) {
                com.example.utils.AppLogger.e("SystemDiagnosticTracker", "Failed to save via app scoped dir: ${dir.absolutePath}", e)
            }
        }
        
        return finalPath
    }

    private fun getAppLogcat(): String {
        return try {
            val pid = android.os.Process.myPid()
            val process = Runtime.getRuntime().exec("logcat -d -v threadtime -t 2000")
            val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            val log = java.lang.StringBuilder()
            var line: String?
            val pidStr = pid.toString()
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains(pidStr)) {
                    log.append(line).append("\\n")
                }
            }
            log.toString()
        } catch (e: Exception) {
            "Failed to get logcat: ${e.message}"
        }
    }
}
"""

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "w") as f:
    f.write(content)
