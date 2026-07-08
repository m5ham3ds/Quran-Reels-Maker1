import re

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Make sure it actually is drawing with -150 offset.
if "iconX.toFloat() - 150f" in content:
    print("iconX offset is present")
else:
    print("iconX offset NOT PRESENT!")

