import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

textsize_old = "this.textSize = textFontSize.toFloat() * scale"
textsize_new = "this.textSize = textFontSize.toFloat() * 1.5f * scale"

transsize_old = "this.textSize = translationFontSize.toFloat() * scale"
transsize_new = "this.textSize = translationFontSize.toFloat() * 1.5f * scale"

surahsize_old = "this.textSize = surahNameFontSize.toFloat() * scale"
surahsize_new = "this.textSize = surahNameFontSize.toFloat() * 1.5f * scale"

# Wait, is `* 1.5f` necessary? The Live Preview has `scalePx` but also it's an SP value. Let's see what happens if I don't touch text size but just offsets.

