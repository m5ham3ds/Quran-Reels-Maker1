import os

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    original_content = content

    content = content.replace('com.example.utils.AppLogger', 'AppLogger')
    
    if content != original_content:
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Fixed {filepath}")

for root, _, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt') and file != 'AppLogger.kt':
            process_file(os.path.join(root, file))
