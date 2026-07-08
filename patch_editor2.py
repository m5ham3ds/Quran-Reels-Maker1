import re

with open('app/src/main/java/com/example/ui/VideoEditorScreen.kt', 'r') as f:
    content = f.read()

# Make sure icon matches scaled dimension, the UI size should be roughly iconSize.sp
icontext_old = """Text("♡", color = Color.White.copy(alpha = iconOpacity.coerceAtLeast(0.3f)), fontSize = (iconSize / 2).sp)"""
icontext_new = """Text("♡", color = Color.White.copy(alpha = iconOpacity.coerceAtLeast(0.3f)), fontSize = iconSize.sp)"""
content = content.replace(icontext_old, icontext_new)

with open('app/src/main/java/com/example/ui/VideoEditorScreen.kt', 'w') as f:
    f.write(content)

