import re

with open("app/src/main/java/com/example/service/VideoGenerationService.kt", "r") as f:
    content = f.read()

content = content.replace('FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING', 'FOREGROUND_SERVICE_TYPE_DATA_SYNC')

with open("app/src/main/java/com/example/service/VideoGenerationService.kt", "w") as f:
    f.write(content)
