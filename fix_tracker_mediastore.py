import re

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "r") as f:
    content = f.read()

new_mediastore = """        // 2. Try MediaStore fallback
        try {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Quran Reels/ERROR")
                }
            }
            val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                android.provider.MediaStore.Files.getContentUri("external")
            }
            val uri = context.contentResolver.insert(collection, values)
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
        }"""

content = re.sub(r'        // 2\. Try MediaStore fallback.*?        \} catch \(e: Exception\) \{\n            com\.example\.utils\.AppLogger\.e\("SystemDiagnosticTracker", "Failed to save via MediaStore", e\)\n        \}', new_mediastore, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "w") as f:
    f.write(content)
