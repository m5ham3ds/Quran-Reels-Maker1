import re

with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "r") as f:
    content = f.read()

content = content.replace(
    "downloadAudio(selectedVideoUrl, targetFile)",
    "runCatching { downloadAudio(selectedVideoUrl, targetFile) }.onFailure { e -> SystemDiagnosticTracker.addLog(\"DOWNLOAD_ERROR\", \"فشل تحميل الفيديو: ${e.message}\") }"
)

with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "w") as f:
    f.write(content)
