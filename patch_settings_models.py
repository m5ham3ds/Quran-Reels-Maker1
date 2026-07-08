import re
with open("app/src/main/java/com/example/ui/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('"gemini-3.5-flash"', '"gemini-1.5-flash"')
content = content.replace('"gemini-3.1-pro-preview"', '"gemini-2.0-flash"')

with open("app/src/main/java/com/example/ui/settings/SettingsScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/settings/SettingsManager.kt", "r") as f:
    content = f.read()

content = content.replace('"gemini-3.5-flash"', '"gemini-1.5-flash"')

with open("app/src/main/java/com/example/settings/SettingsManager.kt", "w") as f:
    f.write(content)

