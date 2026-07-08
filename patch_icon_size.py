import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Fix icon size calculation (it should just use the raw size like text sizes, but with scale applied)
icon_draw_old = """            if (iconOpacity > 0f) {
                val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alphaVal = (iconOpacity * 255).toInt().coerceIn(0, 255)
                    color = Color.argb(alphaVal, 255, 255, 255)
                    textSize = iconSize.toFloat() * scale
                    this.textAlign = Paint.Align.CENTER
                    setShadowLayer(6f, 0f, 3f, Color.argb(150, 0, 0, 0))
                }"""

icon_draw_new = """            if (iconOpacity > 0f) {
                val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alphaVal = (iconOpacity * 255).toInt().coerceIn(0, 255)
                    color = Color.argb(alphaVal, 255, 255, 255)
                    textSize = iconSize.toFloat() * 1.5f * scale
                    this.textAlign = Paint.Align.CENTER
                    setShadowLayer(6f, 0f, 3f, Color.argb(150, 0, 0, 0))
                }"""
content = content.replace(icon_draw_old, icon_draw_new)

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(content)

