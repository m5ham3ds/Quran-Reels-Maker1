import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

iconpaint_old = """                val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alphaVal = (iconOpacity * 255).toInt().coerceIn(0, 255)
                    color = Color.argb(alphaVal, 255, 255, 255)"""
iconpaint_new = """                val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alphaVal = (iconOpacity * 255).toInt().coerceIn(0, 255)
                    color = Color.argb(alphaVal, 255, 255, 255)
                    typeface = Typeface.DEFAULT"""
content = content.replace(iconpaint_old, iconpaint_new)


with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(content)

