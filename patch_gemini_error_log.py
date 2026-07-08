import re

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "r") as f:
    content = f.read()

target = """                whisperError = e.message ?: "Unknown"
                SystemDiagnosticTracker.addLog("WHISPER", "Failed to extract text: ${e.message}")"""
replacement = """                whisperError = e.message ?: "Unknown"
                SystemDiagnosticTracker.addLog("WHISPER", "فشل استخراج النصوص: ${e.message}")"""
content = content.replace(target, replacement)

target2 = """                    SystemDiagnosticTracker.addLog("WHISPER", "Transcription successful: ${transcription.take(50)}...")"""
replacement2 = """                    SystemDiagnosticTracker.addLog("WHISPER", "نجح استخراج النص: ${transcription.take(50)}...")"""
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "w") as f:
    f.write(content)

