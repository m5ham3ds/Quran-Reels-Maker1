import re

with open('/tmp/heap.bin', 'rb') as f:
    data = f.read()

# The file likely contains imports like "import androidx.compose..."
# and "package com.example.generator"
# We can look for a large string starting with "package com.example.generator" and ending with the end of the file.

# A regex to find the file contents
# Looking for "package com.example.generator\n" followed by imports and then class VideoGenerator
pattern = b'package com\.example\.generator\n+import [^\x00]+class VideoGenerator'
matches = re.finditer(pattern, data)

best_match = None
max_len = 0

for match in matches:
    start = match.start()
    # Find the end of the string (null byte)
    end = data.find(b'\x00', start)
    if end == -1:
        end = len(data)
    
    content = data[start:end]
    if len(content) > max_len:
        max_len = len(content)
        best_match = content

if best_match:
    with open('/tmp/recovered.kt', 'wb') as f:
        f.write(best_match)
    print(f"Found and saved {len(best_match)} bytes!")
else:
    print("Not found.")
