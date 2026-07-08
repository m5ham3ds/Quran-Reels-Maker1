import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Fix the translation text draw offsets in generator. Wait, where is translation drawn?
# Let's just grep for "canvas.drawText" first to see.

