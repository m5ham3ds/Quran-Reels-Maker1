import re

# Revert SettingsScreen
with open("app/src/main/java/com/example/ui/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('"gemini-1.5-flash"', '"gemini-3.5-flash"')
content = content.replace('"gemini-2.0-flash"', '"gemini-3.1-pro-preview"')

with open("app/src/main/java/com/example/ui/settings/SettingsScreen.kt", "w") as f:
    f.write(content)

# Revert SettingsManager
with open("app/src/main/java/com/example/settings/SettingsManager.kt", "r") as f:
    content = f.read()

content = content.replace('"gemini-1.5-flash"', '"gemini-3.5-flash"')

with open("app/src/main/java/com/example/settings/SettingsManager.kt", "w") as f:
    f.write(content)

# Revert GeminiMetaGenerator
with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "r") as f:
    content = f.read()

content = content.replace('"gemini-1.5-flash"', '"gemini-1.5-pro"')

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "w") as f:
    f.write(content)

