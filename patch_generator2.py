import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Fix icon position, which was manually hardcoded to videoHeight - 170f
hearty_old = "val heartY = videoHeight - 170f + (iconY.toFloat() * scale) - iconPaint.descent()"
hearty_new = "val heartY = videoHeight - 170f + (iconY.toFloat() * 2f * scale) - iconPaint.descent()"
content = content.replace(hearty_old, hearty_new)

heartx_old = """canvas.drawText("♡", videoWidth / 2f + ((iconX.toFloat() - 150f) * scale), heartY, iconPaint)"""
heartx_new = """canvas.drawText("♡", videoWidth / 2f + ((iconX.toFloat() * 2f - 150f) * scale), heartY, iconPaint)"""
content = content.replace(heartx_old, heartx_new)

# Let's fix text positions that use translationTextY
# Search for translationTextY inside createVerseBitmap
with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(content)

