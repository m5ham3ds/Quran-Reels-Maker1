import re

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "r") as f:
    content = f.read()

target = """SystemDiagnosticTracker.addLog("GEMINI", "Calling WhisperX for audio transcription of URL: $videoUrl")"""
replacement = """SystemDiagnosticTracker.addLog("SYSTEM", "جاري تحويل الرابط إلى Space لمعالجة الصوت واستخراج النصوص: $videoUrl")"""
content = content.replace(target, replacement)

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "w") as f:
    f.write(content)
