import re

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "r") as f:
    content = f.read()

# Make sure we use MediaStore.Downloads.getContentUri("external") on Android 10+
new_write_via_mediastore = """    private fun writeViaMediaStore(fileName: String, report: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Quran Reels/ERROR")
            }
            val collection = MediaStore.Files.getContentUri("external")
            val uri = appContext.contentResolver.insert(collection, values) ?: return false
            appContext.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(report.toByteArray())
                out.flush()
            }
            AppLogger.i(TAG, "Crash log saved via MediaStore: $uri")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "MediaStore save failed", e)
            false
        }
    }"""

content = re.sub(r'    private fun writeViaMediaStore.*?return false\n        \}\n    \}', new_write_via_mediastore, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "w") as f:
    f.write(content)
