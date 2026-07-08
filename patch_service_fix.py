import re
with open("app/src/main/java/com/example/service/VideoGenerationService.kt", "r") as f:
    content = f.read()

content = content.replace("val includeBasmalah", "val currentJob = activeJob\n                val includeBasmalah")
content = content.replace("activeJob == kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]", "activeJob == currentJob")

with open("app/src/main/java/com/example/service/VideoGenerationService.kt", "w") as f:
    f.write(content)
