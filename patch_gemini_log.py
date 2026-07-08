import re

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "r") as f:
    content = f.read()

target = """        SystemDiagnosticTracker.addLog("GEMINI", "Sending data to Gemini API for metadata extraction.")"""
replacement = """        SystemDiagnosticTracker.addLog("GEMINI", "تم جلب المعلومات بنجاح وإعدادها. جاري الانتقال إلى إرسال المعلومات والبرومبت الاحترافي إلى نموذج ذكاء اصطناعي Gemini...")"""
content = content.replace(target, replacement)

target2 = """            SystemDiagnosticTracker.addLog("GEMINI", "Failed to extract text: ${e.message}")"""
replacement2 = """            SystemDiagnosticTracker.addLog("WHISPER", "فشل استخراج النصوص: ${e.message}")"""
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "w") as f:
    f.write(content)

