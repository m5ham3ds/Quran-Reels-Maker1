import re
with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "r") as f:
    content = f.read()

# Make downloadAudio suspend
content = content.replace("private fun downloadAudio", "private suspend fun downloadAudio")

# Make transcodeAudioChunk suspend
content = content.replace("private fun transcodeAudioChunk", "private suspend fun transcodeAudioChunk")

# Make analyzeAudioEnergies suspend
content = content.replace("private fun analyzeAudioEnergies", "private suspend fun analyzeAudioEnergies")

# Make monitorWhisperXJob suspend
content = content.replace("private fun monitorWhisperXJob", "private suspend fun monitorWhisperXJob")

# Make fetchWithRetry suspend
content = content.replace("private fun fetchWithRetry", "private suspend fun fetchWithRetry")

with open("app/src/main/java/com/example/generator/VideoGenerator.kt", "w") as f:
    f.write(content)

