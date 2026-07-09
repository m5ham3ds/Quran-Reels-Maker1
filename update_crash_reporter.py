import re

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "r") as f:
    content = f.read()

new_saveCrashLog = """    private fun saveCrashLog(report: String) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "crash_$timeStamp.txt"

        var success = writeViaPublicMoviesDir(fileName, report)
        if (!success) {
            success = writeViaMediaStore(fileName, report)
        }
        if (!success) {
            success = writeViaAppScoped(fileName, report)
        }
        if (!success) {
            writeViaInternal(fileName, report)
        }
    }

    private fun writeViaPublicMoviesDir(fileName: String, report: String): Boolean {
        return try {
            val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val quranReelsDir = File(moviesDir, "Quran Reels/ERROR")
            if (!quranReelsDir.exists()) quranReelsDir.mkdirs()
            val file = File(quranReelsDir, fileName)
            PrintWriter(FileWriter(file)).use { it.print(report) }
            AppLogger.i(TAG, "Crash log saved via public directory: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Public directory save failed", e)
            false
        }
    }"""

content = re.sub(r'    private fun saveCrashLog.*?    private fun writeViaMediaStore', new_saveCrashLog + '\n\n    private fun writeViaMediaStore', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "w") as f:
    f.write(content)
