import re
with open("app/src/main/java/com/example/ui/PopularClipsScreen.kt", "r") as f:
    content = f.read()

target1 = """                                    if (result != null) {
                                        addSurahStr = result.surah.toString()
                                        addStartStr = result.startAyah.toString()
                                        addEndStr = result.endAyah.toString()"""

replacement1 = """                                    if (result != null) {
                                        if (!result.error.isNullOrEmpty()) {
                                            com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "خطأ من المولد: ${result.error}")
                                            Toast.makeText(context, "فشل: ${result.error}", Toast.LENGTH_LONG).show()
                                        } else {
                                            addSurahStr = result.surah.toString()
                                            addStartStr = result.startAyah.toString()
                                            addEndStr = result.endAyah.toString()"""

# We need to properly brace the 'else' block
target1_full = """                                    if (result != null) {
                                        addSurahStr = result.surah.toString()
                                        addStartStr = result.startAyah.toString()
                                        addEndStr = result.endAyah.toString()
                                        if (result.reciterName.isNotBlank() && result.reciterName != "Unknown") {
                                            addReciter = result.reciterName
                                        }
                                        if (result.title.isNotBlank()) {
                                            addTitle = result.title
                                        }
                                        if (result.category.isNotBlank()) {
                                            addCategory = result.category
                                        }
                                        
                                        if (addTitle.isBlank()) {
                                            addTitle = "تلاوة - ${addReciter.ifBlank { "يوتيوب" }}"
                                        }
                                        com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "تم إضافة المقطع الرائج بنجاح! سيتم حفظ البيانات الآن.")
                                        
                                        val newItem = CuratedClip(
                                                id = "clip_custom_${System.currentTimeMillis()}",
                                                reciter = addReciter.ifBlank { "مقرئ Youtube" },
                                                reciterId = "youtube|$addUrl",
                                                title = addTitle,
                                                surah = addSurahStr.toIntOrNull() ?: 1,
                                                ayahStart = addStartStr.toIntOrNull() ?: 1,
                                                ayahEnd = addEndStr.toIntOrNull() ?: 1,
                                                audioUrl = addUrl,
                                                category = addCategory,
                                                videoQuery = if (backgroundKeywords.isNotEmpty()) backgroundKeywords.random() else "islamic architecture nature"
                                            )
                                        baseClipsList.add(newItem)
                                        saveCustomClipsToSettings(baseClipsList.toList())
                                        showAddDialog = false
                                        com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "تم الحفظ وإغلاق نافذة الإضافة.")
                                        Toast.makeText(context, if (isArabic) "تمت إضافة المقطع بنجاح!" else "Clip added successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "فشل: نتيجة الاستخراج فارغة (null).")
                                        Toast.makeText(context, if (isArabic) "فشل جلب البيانات بالذكاء الاصطناعي، يرجى المحاولة مجدداً أو التأكد من الرابط" else "AI failed to fetch data, please try again or check the link", Toast.LENGTH_LONG).show()
                                    }"""

replacement1_full = """                                    if (result != null) {
                                        if (!result.error.isNullOrEmpty()) {
                                            com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "خطأ من المولد: ${result.error}")
                                            Toast.makeText(context, "فشل: ${result.error}", Toast.LENGTH_LONG).show()
                                        } else {
                                            addSurahStr = result.surah.toString()
                                            addStartStr = result.startAyah.toString()
                                            addEndStr = result.endAyah.toString()
                                            if (result.reciterName.isNotBlank() && result.reciterName != "Unknown") {
                                                addReciter = result.reciterName
                                            }
                                            if (result.title.isNotBlank()) {
                                                addTitle = result.title
                                            }
                                            if (result.category.isNotBlank()) {
                                                addCategory = result.category
                                            }
                                            
                                            if (addTitle.isBlank()) {
                                                addTitle = "تلاوة - ${addReciter.ifBlank { "يوتيوب" }}"
                                            }
                                            com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "تم إضافة المقطع الرائج بنجاح! سيتم حفظ البيانات الآن.")
                                            
                                            val newItem = CuratedClip(
                                                    id = "clip_custom_${System.currentTimeMillis()}",
                                                    reciter = addReciter.ifBlank { "مقرئ Youtube" },
                                                    reciterId = "youtube|$addUrl",
                                                    title = addTitle,
                                                    surah = addSurahStr.toIntOrNull() ?: 1,
                                                    ayahStart = addStartStr.toIntOrNull() ?: 1,
                                                    ayahEnd = addEndStr.toIntOrNull() ?: 1,
                                                    audioUrl = addUrl,
                                                    category = addCategory,
                                                    videoQuery = if (backgroundKeywords.isNotEmpty()) backgroundKeywords.random() else "islamic architecture nature"
                                                )
                                            baseClipsList.add(newItem)
                                            saveCustomClipsToSettings(baseClipsList.toList())
                                            showAddDialog = false
                                            com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "تم الحفظ وإغلاق نافذة الإضافة.")
                                            Toast.makeText(context, if (isArabic) "تمت إضافة المقطع بنجاح!" else "Clip added successfully", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "فشل: نتيجة الاستخراج فارغة (null).")
                                        Toast.makeText(context, if (isArabic) "فشل جلب البيانات بالذكاء الاصطناعي، يرجى المحاولة مجدداً أو التأكد من الرابط" else "AI failed to fetch data, please try again or check the link", Toast.LENGTH_LONG).show()
                                    }"""
content = content.replace(target1_full, replacement1_full)

target2_full = """                                    if (result != null) {
                                        val updatedClip = clipInfo.copy(
                                            reciter = result.reciterName,
                                            title = result.title,
                                            surah = result.surah,
                                            ayahStart = result.startAyah,
                                            ayahEnd = result.endAyah
                                        )
                                        
                                        val index = baseClipsList.indexOfFirst { it.id == clipInfo.id }
                                        if (index != -1) {
                                            baseClipsList[index] = updatedClip
                                            saveCustomClipsToSettings(baseClipsList.toList())
                                        }
                                        Toast.makeText(context, if (isArabic) "تم تحديث البيانات بنجاح!" else "Updated successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, if (isArabic) "تعذر جلب البيانات من النموذج" else "Failed to analyze clip URL", Toast.LENGTH_SHORT).show()
                                    }"""

replacement2_full = """                                    if (result != null) {
                                        if (!result.error.isNullOrEmpty()) {
                                            Toast.makeText(context, "فشل: ${result.error}", Toast.LENGTH_LONG).show()
                                        } else {
                                            val updatedClip = clipInfo.copy(
                                                reciter = result.reciterName,
                                                title = result.title,
                                                surah = result.surah,
                                                ayahStart = result.startAyah,
                                                ayahEnd = result.endAyah
                                            )
                                            
                                            val index = baseClipsList.indexOfFirst { it.id == clipInfo.id }
                                            if (index != -1) {
                                                baseClipsList[index] = updatedClip
                                                saveCustomClipsToSettings(baseClipsList.toList())
                                            }
                                            Toast.makeText(context, if (isArabic) "تم تحديث البيانات بنجاح!" else "Updated successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, if (isArabic) "تعذر جلب البيانات من النموذج" else "Failed to analyze clip URL", Toast.LENGTH_SHORT).show()
                                    }"""

content = content.replace(target2_full, replacement2_full)

with open("app/src/main/java/com/example/ui/PopularClipsScreen.kt", "w") as f:
    f.write(content)

