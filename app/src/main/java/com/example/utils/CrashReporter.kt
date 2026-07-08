package com.example.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashReporter(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            com.example.generator.SystemDiagnosticTracker.addLog("FATAL_CRASH", "Uncaught Exception in ${thread.name}: ${exception.javaClass.name} - ${exception.message}\n${Log.getStackTraceString(exception)}")
            saveCrashLog(thread, exception)
        } catch (e: Exception) {
            Log.e("CrashReporter", "Error saving crash log", e)
        }
        
        defaultHandler?.uncaughtException(thread, exception)
    }

    private fun saveCrashLog(thread: Thread, exception: Throwable) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "crash_$timeStamp.txt"
        
        var isSaved = false
        
        // 1. Try MediaStore (best for Android 10+ user visibility)
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
                    PrintWriter(out).use { writer ->
                        writeCrashLogToWriter(writer, thread, exception)
                    }
                }
                Log.d("CrashReporter", "Successfully wrote crash log to MediaStore: $uri")
                isSaved = true
            }
        } catch (e: Exception) {
            Log.e("CrashReporter", "Failed to write crash log to MediaStore", e)
        }
        
        if (isSaved) return
        
        val directoriesToTry = mutableListOf<File>()
        
        // 1. Android/data/com.../files/DiagnosticLogs (Guaranteed write without permissions, accessible via USB)
        try {
            val extFilesDir = context.getExternalFilesDir(null)
            if (extFilesDir != null) {
                directoriesToTry.add(File(extFilesDir, "DiagnosticLogs"))
            }
        } catch (e: Exception) {}
        // 2. Android/data/com.../files/Documents/ERROR (Guaranteed write without permissions, accessible via USB)
        try {
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (docsDir != null) {
                directoriesToTry.add(File(docsDir, "ERROR"))
            }
        } catch (e: Exception) {}
        // 3. Android/data/com.../files/ERROR (Guaranteed write, accessible via USB)
        try {
            val extFilesDir = context.getExternalFilesDir(null)
            if (extFilesDir != null) {
                directoriesToTry.add(File(extFilesDir, "ERROR"))
            }
        } catch (e: Exception) {}
        
        // 4. Movies/Quran Reels/ERROR (May fail due to scoped storage, but good if it works)
        try {
            val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val appFolder = File(moviesDir, "Quran Reels")
            directoriesToTry.add(File(appFolder, "ERROR"))
        } catch (e: Exception) {}
        
        // 5. Internal app data (Last resort, user might not find it)
        directoriesToTry.add(File(context.filesDir, "ERROR"))
        
        for (dir in directoriesToTry) {
            try {
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val crashFile = File(dir, fileName)
                PrintWriter(FileWriter(crashFile)).use { writer ->
                    writeCrashLogToWriter(writer, thread, exception)
                }
                Log.d("CrashReporter", "Successfully wrote crash log to ${crashFile.absolutePath}")
                break // Stop on first successful file write
            } catch (e: Exception) {
                Log.e("CrashReporter", "Failed to write crash log to ${dir.absolutePath}", e)
            }
        }
    }
    
    private fun writeCrashLogToWriter(writer: PrintWriter, thread: Thread, exception: Throwable) {
        writer.println("=== Quran Reels Crash Report ===")
        writer.println("Time: ${Date()}")
        writer.println("Thread: ${thread.name} (ID: ${thread.id})")
        writer.println("Exception: ${exception.javaClass.name}")
        writer.println("Message: ${exception.message}")
        writer.println()
        writer.println("--- Stack Trace ---")
        exception.printStackTrace(writer)
        writer.println()
        
        var cause = exception.cause
        while (cause != null) {
            writer.println("--- Cause: ${cause.javaClass.name} ---")
            writer.println("Message: ${cause.message}")
            cause.printStackTrace(writer)
            cause = cause.cause
        }
        
        writer.println()
        writer.println("--- System Logcat ---")
        writer.println(getLogcatOutput())
        
        writer.println()
        writer.println("--- Device Info ---")
        writer.println("OS Version: ${System.getProperty("os.version")} (${android.os.Build.VERSION.INCREMENTAL})")
        writer.println("OS API Level: ${android.os.Build.VERSION.SDK_INT}")
        writer.println("Device: ${android.os.Build.DEVICE}")
        writer.println("Model: ${android.os.Build.MODEL}")
        writer.println("Product: ${android.os.Build.PRODUCT}")
        writer.flush()
    }
    
    private fun getLogcatOutput(): String {
        return try {
            val pid = android.os.Process.myPid()
            val command = arrayOf("logcat", "-d", "-v", "threadtime")
            val process = Runtime.getRuntime().exec(command)
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
    
    companion object {
        fun initialize(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (defaultHandler !is CrashReporter) {
                Thread.setDefaultUncaughtExceptionHandler(CrashReporter(context.applicationContext, defaultHandler))
            }
        }
    }
}
