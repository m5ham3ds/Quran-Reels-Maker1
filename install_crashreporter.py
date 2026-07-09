import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

if "CrashReporter.install" not in content:
    content = content.replace(
        "super.onCreate(savedInstanceState)",
        "super.onCreate(savedInstanceState)\n        com.example.utils.CrashReporter.install(this.applicationContext)"
    )
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(content)
    print("CrashReporter installed in MainActivity")
else:
    print("CrashReporter already installed")
