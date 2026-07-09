import os
import re

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    original_content = content

    content = content.replace('import android.util.Log', 'import com.example.utils.AppLogger')
    
    # Replace calls
    content = re.sub(r'\bLog\.d\(', 'AppLogger.d(', content)
    content = re.sub(r'\bLog\.e\(', 'AppLogger.e(', content)
    content = re.sub(r'\bLog\.i\(', 'AppLogger.i(', content)
    content = re.sub(r'\bLog\.w\(', 'AppLogger.w(', content)
    content = re.sub(r'\bLog\.getStackTraceString\(', 'AppLogger.getStackTraceString(', content)

    # Some files might have `android.util.Log.e`
    content = content.replace('android.util.Log.d', 'com.example.utils.AppLogger.d')
    content = content.replace('android.util.Log.e', 'com.example.utils.AppLogger.e')
    content = content.replace('android.util.Log.i', 'com.example.utils.AppLogger.i')
    content = content.replace('android.util.Log.w', 'com.example.utils.AppLogger.w')
    content = content.replace('android.util.Log.getStackTraceString', 'com.example.utils.AppLogger.getStackTraceString')

    if content != original_content:
        # Check if AppLogger is imported if it's used
        if 'AppLogger' in content and 'import com.example.utils.AppLogger' not in content and 'package com.example.utils' not in content:
            # Add import
            content = re.sub(r'package\s+[\w\.]+\n', r'\g<0>\nimport com.example.utils.AppLogger\n', content, count=1)
        
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, _, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt') and file != 'AppLogger.kt':
            process_file(os.path.join(root, file))

