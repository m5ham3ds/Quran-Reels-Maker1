import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

iconpaint_old = """                    textSize = iconSize.toFloat() * 1.5f * scale"""
iconpaint_new = """                    textSize = iconSize.toFloat() * scale"""
content = content.replace(iconpaint_old, iconpaint_new)

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(content)

