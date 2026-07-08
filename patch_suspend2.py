import re
with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "r") as f:
    content = f.read()

# Make transcodeMp3ToAac suspend
content = content.replace("private fun transcodeMp3ToAac", "private suspend fun transcodeMp3ToAac")
content = content.replace("private fun getSmartChunks", "private suspend fun getSmartChunks")

# Remove synchronized from downloadAudio
content = re.sub(r"synchronized\(destFile\.absolutePath\.intern\(\)\) \{", "if(true) {", content)

with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "w") as f:
    f.write(content)
