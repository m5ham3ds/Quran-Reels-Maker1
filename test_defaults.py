import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Let's verify arabicTextY and translationTextY offsets one more time.
if "arabicTextY.toFloat() - 90f" in content:
    print("arabic offset OK")
if "translationTextY.toFloat() - 110f" in content:
    print("translation offset OK")
if "iconX.toFloat() - 150f" in content:
    print("iconX offset OK")

