import re

with open('app/src/main/java/com/example/ui/VideoEditorScreen.kt', 'r') as f:
    content = f.read()

icon_offset_old = ".offset { IntOffset(((iconX * 2f - 150f) * scalePx).roundToInt(), (iconY * 2f * scalePx).roundToInt()) }"
icon_offset_new = ".offset { IntOffset(((iconX - 150f) * 2f * scalePx).roundToInt(), (iconY * 2f * scalePx).roundToInt()) }"

content = content.replace(icon_offset_old, icon_offset_new)

with open('app/src/main/java/com/example/ui/VideoEditorScreen.kt', 'w') as f:
    f.write(content)

