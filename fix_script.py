import re

with open('./app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    content = f.read()

# Let's remove all the bad insertions of chunkIndexToReplace
content = re.sub(r'if \(chunkIndexToReplace != -1.*?(?=\n\s*// Fallback to high-quality|\n\s*if \(!videoLoaded\) \{)', '', content, flags=re.DOTALL)
content = re.sub(r'if \(chunkIndexToReplace != -1.*', '', content) # wait, I don't want to delete everything

with open('./app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(content)

