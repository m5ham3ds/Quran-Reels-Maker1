import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# I used: iconX.toFloat() * 2f - 150f. Let's fix iconX default offset correctly.
