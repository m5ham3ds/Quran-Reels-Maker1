import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Make sure translation, text size and surah size are correctly scaled
# They are correct, the canvas in the live preview has 720 width while the video width could be 1080 (so 1.5x)
# `val scale = videoWidth / 360f` -> 1080 / 360 = 3
# In Live Preview `val scalePx = viewWidthPx / 720f`
# We use `textFontSize.toFloat() * scale` which is 20 * 3 = 60 pixels.
# The `scale` variable accounts for the difference between the 360dp standard and the real video size, which is completely fine!

