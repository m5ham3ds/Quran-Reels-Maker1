package com.example.generator
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
            logFile?.writeText("=== Live Diagnostic Log Started ===\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addLog(tag: String, message: String, severity: String = "INFO") {
        val currentLogs = _logs.value.toMutableList()
        val log = DiagnosticLog(System.currentTimeMillis(), severity, tag, message)
        currentLogs.add(log)
        _logs.value = currentLogs
        
        try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            val line = "[${sdf.format(Date(log.timestamp))}] [${log.severity}] [${log.tag}] ${log.message}\n"
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "diagnostic_report_$timeStamp.txt"
        var finalPath = ""
        
        // 1. Try MediaStore fallback (best for user visibility on Android 10+)
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
                        writer.write("=== Quran Reels Diagnostic Report ===\n")
                        writer.write("Time: ${Date()}\n")
                        writer.write(extraData)
                        writer.write("\n\n")
                        writer.write("--- Application Log (AppLogger) ---\n")
                        writer.write(AppLogger.getLogs())
                        writer.write("\n\n--- System Logcat Live Dump ---\n")
                        writer.write(getAppLogcat())
                        writer.write("\n\n--- Process Logs ---\n")
                        for (log in getLogs()) {
                            writer.write(log)
                            writer.write("\n")
                        }
                    }
                }
                finalPath = uri.toString()
            }
        } catch (e: Exception) {}

        val directoriesToTry = mutableListOf<File>()

        try {
            val docsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
            if (docsDir != null) directoriesToTry.add(File(docsDir, "ERROR"))
        } catch (e: Exception) {}

        try {
            val extFilesDir = context.getExternalFilesDir(null)
            if (extFilesDir != null) directoriesToTry.add(File(extFilesDir, "ERROR"))
        } catch (e: Exception) {}

        try {
            val moviesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES)
            directoriesToTry.add(File(moviesDir, "Quran Reels/ERROR"))
        } catch (e: Exception) {}

        directoriesToTry.add(File(context.filesDir, "ERROR"))

        for (dir in directoriesToTry) {
            try {
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                java.io.FileWriter(file).use { writer ->
                    writer.write("=== Quran Reels Diagnostic Report ===\n")
                    writer.write("Time: ${Date()}\n")
                    writer.write(extraData)
                    writer.write("\n\n")
                        writer.write("--- Application Log (AppLogger) ---\n")
                        writer.write(AppLogger.getLogs())
                        writer.write("\n\n--- System Logcat Live Dump ---\n")
                        writer.write(getAppLogcat())
                        writer.write("\n\n--- Process Logs ---\n")
                    for (log in getLogs()) {
                        writer.write(log)
                        writer.write("\n")
                    }
                }
                if (finalPath.isEmpty()) {
                    finalPath = file.absolutePath
                }
                AppLogger.d("SystemDiagnostic", "Saved report to ${file.absolutePath}")
            } catch (e: Exception) {
                AppLogger.e("SystemDiagnostic", "Failed to save to ${dir.absolutePath}", e)
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
                    log.append(line).append("\n")
                }
            }
            log.toString()
        } catch (e: Exception) {
            "Failed to get logcat: ${e.message}"
        }
    }
}
