import re

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "r") as f:
    content = f.read()

# Replace Movies with Documents
content = content.replace('"Movies/Quran Reels/ERROR"', '"Documents/Quran Reels/ERROR"')
content = content.replace('Environment.DIRECTORY_MOVIES', 'Environment.DIRECTORY_DOCUMENTS')

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "r") as f:
    content2 = f.read()

content2 = content2.replace('"Movies/Quran Reels/ERROR"', '"Documents/Quran Reels/ERROR"')
content2 = content2.replace('Environment.DIRECTORY_MOVIES', 'Environment.DIRECTORY_DOCUMENTS')

with open("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt", "w") as f:
    f.write(content2)

print("Paths updated to Documents")
