import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Fix text translations and arabic.
# It was manually using `- 110f` but let's make sure translation has it.
transy_old = "val transY = baseStartY + sl.height + 32f + (translationTextY.toFloat() * scale)"
transy_new = "val transY = baseStartY + sl.height + 32f + ((translationTextY.toFloat() - 110f) * scale)"

if transy_old in content:
    content = content.replace(transy_old, transy_new)
else:
    print("Not found transy_old")

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(content)

