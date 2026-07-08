import re
with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "r") as f:
    content = f.read()

content = content.replace("private fun alignWithWhisperX", "private suspend fun alignWithWhisperX")

with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "w") as f:
    f.write(content)
