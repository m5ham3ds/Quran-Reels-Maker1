import re

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "r") as f:
    content = f.read()

content = content.replace(
    "sb.appendLine(\"---------- Exception Chain (كامل سلسلة الأسباب) ----------\")",
    "sb.appendLine(\"---------- Application Log (AppLogger) ----------\")\n        sb.appendLine(AppLogger.getLogs())\n        sb.appendLine()\n\n        sb.appendLine(\"---------- Exception Chain (كامل سلسلة الأسباب) ----------\")"
)

content = content.replace(
    "android.util.Log", "com.example.utils.AppLogger"
)

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "w") as f:
    f.write(content)
