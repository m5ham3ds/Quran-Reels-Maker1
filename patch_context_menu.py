import re

with open('app/src/main/java/com/example/ui/VideoEditorScreen.kt', 'r') as f:
    content = f.read()

menu_old = """                    } else if (selectedElement == "translation") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isArabic) "الحجم" else "Size", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = translationFontSize,
                                onValueChange = { translationFontSize = it },
                                onValueChangeFinished = { coroutineScope.launch { settingsManager.setTranslationFontSize(translationFontSize.roundToInt()) } },
                                valueRange = 5f..60f,
                                modifier = Modifier.width(120.dp),
                                colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold)
                            )
                        }
                    } else if (selectedElement?.startsWith("video") == true) {"""

menu_new = """                    } else if (selectedElement == "translation") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isArabic) "الحجم" else "Size", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = translationFontSize,
                                onValueChange = { translationFontSize = it },
                                onValueChangeFinished = { coroutineScope.launch { settingsManager.setTranslationFontSize(translationFontSize.roundToInt()) } },
                                valueRange = 5f..60f,
                                modifier = Modifier.width(120.dp),
                                colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold)
                            )
                        }
                    } else if (selectedElement == "icon") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isArabic) "حجم الرمز" else "Icon Size", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = iconSize,
                                onValueChange = { iconSize = it },
                                onValueChangeFinished = { coroutineScope.launch { settingsManager.setIconSize(iconSize.roundToInt()) } },
                                valueRange = 10f..60f,
                                modifier = Modifier.width(100.dp),
                                colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isArabic) "الشفافية" else "Opacity", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = iconOpacity,
                                onValueChange = { iconOpacity = it },
                                onValueChangeFinished = { coroutineScope.launch { settingsManager.setIconOpacity(iconOpacity) } },
                                valueRange = 0f..1f,
                                modifier = Modifier.width(100.dp),
                                colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold)
                            )
                        }
                    } else if (selectedElement?.startsWith("video") == true) {"""
content = content.replace(menu_old, menu_new)

with open('app/src/main/java/com/example/ui/VideoEditorScreen.kt', 'w') as f:
    f.write(content)

