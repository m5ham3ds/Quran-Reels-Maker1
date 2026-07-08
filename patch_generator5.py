import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Let's fix iconX default offset correctly.
iconx_old = """canvas.drawText("♡", videoWidth / 2f + ((iconX.toFloat() * 2f - 150f) * scale), heartY, iconPaint)"""
iconx_new = """canvas.drawText("♡", videoWidth / 2f + ((iconX.toFloat() - 150f) * 2f * scale), heartY, iconPaint)"""
content = content.replace(iconx_old, iconx_new)

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(content)

