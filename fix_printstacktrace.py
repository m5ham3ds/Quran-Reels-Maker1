import os
import re

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    original_content = content

    # Replace e.printStackTrace() with AppLogger.e("QuranReels", "Exception caught", e)
    # Also handle other variable names like ex.printStackTrace()
    content = re.sub(r'(\w+)\.printStackTrace\(\)', r'com.example.utils.AppLogger.e("ExceptionCatch", "Exception caught: ${ \1.message }", \1)', content)
    
    if content != original_content:
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Fixed {filepath}")

for root, _, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt') and file != 'AppLogger.kt':
            process_file(os.path.join(root, file))
