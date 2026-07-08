import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Fix text translations and arabic.
# Earlier I accidentally failed to match because the text might be different. Let's just grep the actual content

