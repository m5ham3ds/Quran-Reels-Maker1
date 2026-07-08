import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Verify text sizes using scale correctly
