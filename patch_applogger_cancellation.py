import re

with open("app/src/main/java/com/example/utils/AppLogger.kt", "r") as f:
    content = f.read()

# Make sure we don't log CancellationException as an error
replacement_e_tr = """    fun e(tag: String?, msg: String, tr: Throwable?): Int {
        if (tr is kotlinx.coroutines.CancellationException || tr is java.util.concurrent.CancellationException) {
            appendLog("D", tag, msg, null)
            return Log.d(tag ?: "", msg)
        }
        appendLog("E", tag, msg, tr)
        return Log.e(tag, msg, tr)
    }"""
    
content = re.sub(r'fun e\(tag: String\?, msg: String, tr: Throwable\?\): Int \{.*?return Log\.e\(tag, msg, tr\)\n    \}', replacement_e_tr, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/utils/AppLogger.kt", "w") as f:
    f.write(content)
