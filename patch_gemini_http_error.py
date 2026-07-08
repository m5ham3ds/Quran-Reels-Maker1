import re
with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "r") as f:
    content = f.read()

target = """            } else {
                SystemDiagnosticTracker.addLog("GEMINI", "HTTP Error ${response.code}: ${response.body?.string()}")
            }
        } catch (e: Exception) {
            SystemDiagnosticTracker.addLog("GEMINI", "Error calling Gemini: ${e.message}")
            e.printStackTrace()
        }
        
        return@withContext null"""

replacement = """            } else {
                val errorBody = response.body?.string() ?: ""
                SystemDiagnosticTracker.addLog("GEMINI", "HTTP Error ${response.code}: $errorBody")
                return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "حدث خطأ في الاستجابة: ${response.code} - ${if (response.code == 503) "النموذج يواجه ضغطاً كبيراً، يرجى المحاولة لاحقاً أو تغيير النموذج" else "غير معروف"}")
            }
        } catch (e: Exception) {
            SystemDiagnosticTracker.addLog("GEMINI", "Error calling Gemini: ${e.message}")
            e.printStackTrace()
            return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "خطأ في الاتصال بالذكاء الاصطناعي: ${e.message}")
        }
        
        return@withContext ClipAnalysisResult(relevance = 0f, analysis = "", error = "لم يتم الحصول على أي معلومات")"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/generator/GeminiMetaGenerator.kt", "w") as f:
    f.write(content)
