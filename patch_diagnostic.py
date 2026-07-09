import re

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "r") as f:
    content = f.read()

content = content.replace(
    "writer.write(\"--- App Logcat Activity ---\\n\")\n                        writer.write(getAppLogcat())",
    "writer.write(\"--- Application Log (AppLogger) ---\\n\")\n                        writer.write(com.example.utils.AppLogger.getLogs())\n                        writer.write(\"\\n\\n--- System Logcat Live Dump ---\\n\")\n                        writer.write(getAppLogcat())"
)

content = content.replace(
    "android.util.Log", "com.example.utils.AppLogger"
)

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "w") as f:
    f.write(content)
