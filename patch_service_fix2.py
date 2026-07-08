import re
with open("app/src/main/java/com/example/service/VideoGenerationService.kt", "r") as f:
    content = f.read()

content = content.replace("                val currentJob = activeJob\n                val includeBasmalah", "                val includeBasmalah")
content = content.replace("            try {", "            val currentJob = activeJob\n            try {")

with open("app/src/main/java/com/example/service/VideoGenerationService.kt", "w") as f:
    f.write(content)
