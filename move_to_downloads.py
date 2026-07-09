import re

def update_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # Change Documents to Download
    content = content.replace('"Documents/Quran Reels/ERROR"', '"Download/Quran Reels/ERROR"')
    content = content.replace('Environment.DIRECTORY_DOCUMENTS', 'Environment.DIRECTORY_DOWNLOADS')
    
    with open(filepath, "w") as f:
        f.write(content)

update_file("app/src/main/java/com/example/utils/CrashReporter.kt")
update_file("app/src/main/java/com/example/generator/SystemDiagnosticTracker.kt")

print("Updated paths to Download")
