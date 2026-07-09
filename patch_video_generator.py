import re

with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "r") as f:
    content = f.read()

# Replace the directUrls list with a different one or just let it fall back
# We'll just fix the try-catch to be catch (e: Throwable) and also add runCatching

content = content.replace(
    "downloadAudio(directUrls[vidIdx], targetFile)",
    "runCatching { downloadAudio(directUrls[vidIdx], targetFile) }.onFailure { e -> SystemDiagnosticTracker.addLog(\"DOWNLOAD_ERROR\", \"فشل تحميل فيديو الطوارئ: ${e.message}\") }"
)

with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "w") as f:
    f.write(content)
