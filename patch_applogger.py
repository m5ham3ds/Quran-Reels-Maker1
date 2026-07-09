import re

with open("app/src/main/java/com/example/utils/AppLogger.kt", "r") as f:
    content = f.read()

# Add import if needed
if "import com.example.generator.SystemDiagnosticTracker" not in content:
    content = content.replace("import android.util.Log", "import android.util.Log\nimport com.example.generator.SystemDiagnosticTracker")

# Modify appendLog to also send errors to SystemDiagnosticTracker
target = """        synchronized(logs) {
            if (logs.size >= MAX_LINES) {
                logs.poll()
            }
            logs.offer(logLine)
        }"""
        
replacement = """        synchronized(logs) {
            if (logs.size >= MAX_LINES) {
                logs.poll()
            }
            logs.offer(logLine)
        }
        
        if (level == "E" || level == "F") {
            try {
                SystemDiagnosticTracker.addLog("ERROR", "$tag: $msg$exceptionStr", "ERROR")
            } catch(ignored: Exception) {}
        }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/utils/AppLogger.kt", "w") as f:
    f.write(content)
