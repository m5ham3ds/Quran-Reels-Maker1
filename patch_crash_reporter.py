import re

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "r") as f:
    content = f.read()

content = content.replace(
    "if (writeViaMediaStore(fileName, report)) return\n        if (writeViaAppScoped(fileName, report)) return\n        writeViaInternal(fileName, report)",
    "writeViaMediaStore(fileName, report)\n        writeViaAppScoped(fileName, report)\n        writeViaInternal(fileName, report)"
)

with open("app/src/main/java/com/example/utils/CrashReporter.kt", "w") as f:
    f.write(content)
