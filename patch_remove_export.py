import os

# 1. Update MainActivity.kt
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# remove from menuItems
content = content.replace(',\n                    Triple("video_settings", if (isArabic) "إعدادات تصدير الفيديو" else "Video Export Settings", Icons.Default.HighQuality)', '')
content = content.replace('                    Triple("video_settings", if (isArabic) "إعدادات تصدير الفيديو" else "Video Export Settings", Icons.Default.HighQuality)\n', '')

# remove from pageTitle when
content = content.replace('                    "video_settings" -> if (isArabic) "إعدادات التصدير" else "Export Settings"\n', '')

# remove from Navigation graph
target_nav = """                    "settings" -> com.example.ui.settings.SettingsScreen(onNavigateBack = { selectedTab = "home" })
                    "video_settings" -> com.example.ui.settings.VideoExportSettingsScreen(settingsManager = settingsManager, isArabic = isArabic, onNavigateBack = { selectedTab = "home" })
                }"""
replace_nav = """                    "settings" -> com.example.ui.settings.SettingsScreen(onNavigateBack = { selectedTab = "home" })
                }"""
content = content.replace(target_nav, replace_nav)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

# 2. Update SettingsScreen.kt
with open("app/src/main/java/com/example/ui/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

# add videoFps
content = content.replace('val videoQuality by settingsManager.videoQuality.collectAsState(initial = "Ultra")', 'val videoQuality by settingsManager.videoQuality.collectAsState(initial = "Ultra")\n    val videoFps by settingsManager.videoFps.collectAsState(initial = 30)')

# add UI for FPS
fps_ui_code = """
                    HorizontalDivider(color = Color(0x15FFFFFF))
                    // Video FPS Dropdown
                    Column {
                        Text(
                            text = if (isArabic) "معدل الإطارات (FPS)" else "Video Framerate (FPS)",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        var fpsExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                onClick = { fpsExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x14FFFFFF),
                                border = BorderStroke(1.dp, Color(0x2BFFFFFF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${videoFps} إطار بالثانية",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.HighQuality,
                                        contentDescription = null,
                                        tint = Color(0xFFCFD8DC)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = fpsExpanded,
                                onDismissRequest = { fpsExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(CardBg)
                            ) {
                                val options = listOf(24, 30, 60, 90, 120)
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                             Text(
                                                if (isArabic) "$option إطار بالثانية" else "$option FPS",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                         },
                                        onClick = {
                                            scope.launch { settingsManager.setVideoFps(option) }
                                            fpsExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }"""

# Insert right after the end of quality drop down.
target_quality_end = """                                            qualityExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }"""
                    
content = content.replace(target_quality_end, target_quality_end + fps_ui_code)

with open("app/src/main/java/com/example/ui/settings/SettingsScreen.kt", "w") as f:
    f.write(content)

# 3. Delete VideoExportSettingsScreen.kt
if os.path.exists("app/src/main/java/com/example/ui/settings/VideoExportSettingsScreen.kt"):
    os.remove("app/src/main/java/com/example/ui/settings/VideoExportSettingsScreen.kt")

