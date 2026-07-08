import re
with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "r") as f:
    content = f.read()

content = content.replace('"gemini-1.5-pro"', '"gemini-1.5-flash"')

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "w") as f:
    f.write(content)
