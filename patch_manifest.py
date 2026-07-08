import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

content = content.replace('android.permission.FOREGROUND_SERVICE_MEDIA_PROCESSING', 'android.permission.FOREGROUND_SERVICE_DATA_SYNC')
content = content.replace('android:foregroundServiceType="mediaProcessing"', 'android:foregroundServiceType="dataSync"')

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
