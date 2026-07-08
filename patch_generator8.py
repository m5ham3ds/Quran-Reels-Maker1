import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Let's verify surah name drawing in the UI.
# In VideoEditorScreen.kt:
