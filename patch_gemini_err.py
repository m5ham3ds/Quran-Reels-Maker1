import re
with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "r") as f:
    content = f.read()

content = content.replace(
    'return@withContext ClipAnalysisResult(0f, "", "حدث خطأ في قراءة المعلومات المخزنة", error = "بيانات مخزنة تالفة")',
    'return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "بيانات مخزنة تالفة")'
)

content = content.replace(
    'return@withContext ClipAnalysisResult(0f, "", "ليست هناك اي معلومات لهذا الرابط. يرجى إيقاف ميزة التخطي وإعادة المحاولة.", error = "NO_CACHE")',
    'return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "ليست هناك اي معلومات لهذا الرابط. يرجى إيقاف ميزة التخطي وإعادة المحاولة.")'
)

content = content.replace(
    'return@withContext ClipAnalysisResult(0f, "", "فشل تحليل معلومات المقطع من الذكاء الاصطناعي", error = "JSON_PARSE_ERROR")',
    'return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "فشل تحليل معلومات المقطع من الذكاء الاصطناعي")'
)

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "w") as f:
    f.write(content)
