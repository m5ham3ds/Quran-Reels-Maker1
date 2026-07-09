import re

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "r") as f:
    content = f.read()

replacement = """    fun saveReportToFilesAndGetPath(context: Context, extraData: String = ""): String {
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
        val fileName = "diagnostic_report_$timeStamp.txt"
        var finalPath = ""

        val reportContent = buildString {
            append("=== Quran Reels Diagnostic Report ===\n")
            append("Time: ${java.util.Date()}\n")
            append(extraData)
            append("\n\n")
            append("--- Application Log (AppLogger) ---\n")
            append(com.example.utils.AppLogger.getLogs())
            append("\n\n--- System Logcat Live Dump ---\n")
            try {
                val process = Runtime.getRuntime().exec("logcat -d")
                val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    append(line).append("\n")
                }
            } catch(e: Exception) {
                append("Failed to dump logcat: ${e.message}\n")
            }
            append("\n\n--- Process Logs ---\n")
            for (log in getLogs()) {
                append(log).append("\n")
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
    }"""

content = re.sub(r'    fun saveReportToFilesAndGetPath\(context: Context, extraData: String = ""\): String \{.*?(?=\n    \})', replacement.replace('    fun saveReportToFilesAndGetPath', 'TEMP_REPLACE'), content, flags=re.DOTALL)
content = content.replace('TEMP_REPLACE(context: Context, extraData: String = ""): String {', '')
# Clean up extra brackets
content = re.sub(r'TEMP_REPLACE.*', replacement, content, flags=re.DOTALL) # Need to be careful here

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "w") as f:
    f.write(content)
