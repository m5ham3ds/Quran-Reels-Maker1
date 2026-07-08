import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# The video generator must also respect the default offsets.
draw_text_old = """            val arabicYPx = (arabicTextY * 2 * scale)
            val arabicXPx = (arabicTextX * 2 * scale)"""

draw_text_new = """            val arabicYPx = ((arabicTextY * 2 - 90f) * scale)
            val arabicXPx = (arabicTextX * 2 * scale)"""
content = content.replace(draw_text_old, draw_text_new)

draw_trans_old = """            val transYPx = (translationTextY * 2 * scale)
            val transXPx = (translationTextX * 2 * scale)"""

draw_trans_new = """            val transYPx = ((translationTextY * 2 - 110f) * scale)
            val transXPx = (translationTextX * 2 * scale)"""
content = content.replace(draw_trans_old, draw_trans_new)

draw_icon_old = """                val iconYPx = iconY * 2 * scale
                val iconXPx = iconX * 2 * scale"""

draw_icon_new = """                val iconYPx = iconY * 2 * scale
                val iconXPx = (iconX * 2 - 150f) * scale"""
content = content.replace(draw_icon_old, draw_icon_new)


with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(content)

