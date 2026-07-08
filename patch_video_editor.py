import re

with open('app/src/main/java/com/example/ui/VideoEditorScreen.kt', 'r') as f:
    content = f.read()

# 1. Update EditorState
editor_state_old = """    data class EditorState(
        val arabicX: Float,
        val arabicY: Float,
        val transX: Float,
        val transY: Float,
        val surahX: Float,
        val surahY: Float,
        val fontSize: Float,
        val transFontSize: Float,
        val textColor: String
    )"""

editor_state_new = """    data class EditorState(
        val arabicX: Float,
        val arabicY: Float,
        val transX: Float,
        val transY: Float,
        val surahX: Float,
        val surahY: Float,
        val iconX: Float,
        val iconY: Float,
        val iconSize: Float,
        val iconOpacity: Float,
        val fontSize: Float,
        val transFontSize: Float,
        val textColor: String
    )"""
content = content.replace(editor_state_old, editor_state_new)

# 2. Add icon state vars
vars_old = """    var surahNameX by remember { mutableFloatStateOf(0f) }
    var surahNameY by remember { mutableFloatStateOf(0f) }

    var fontSize by remember { mutableFloatStateOf(50f) }"""

vars_new = """    var surahNameX by remember { mutableFloatStateOf(0f) }
    var surahNameY by remember { mutableFloatStateOf(0f) }
    var iconX by remember { mutableFloatStateOf(0f) }
    var iconY by remember { mutableFloatStateOf(0f) }
    var iconSize by remember { mutableFloatStateOf(20f) }
    var iconOpacity by remember { mutableFloatStateOf(0f) }

    var fontSize by remember { mutableFloatStateOf(50f) }"""
content = content.replace(vars_old, vars_new)

# 3. Read initial settings
init_old = """        surahNameX = settingsManager.surahNameX.first().toFloat()
        surahNameY = settingsManager.surahNameY.first().toFloat()

        fontSize = settingsManager.fontSize.first().toFloat()"""

init_new = """        surahNameX = settingsManager.surahNameX.first().toFloat()
        surahNameY = settingsManager.surahNameY.first().toFloat()
        iconX = settingsManager.iconX.first().toFloat()
        iconY = settingsManager.iconY.first().toFloat()
        iconSize = settingsManager.iconSize.first().toFloat()
        iconOpacity = settingsManager.iconOpacity.first()

        fontSize = settingsManager.fontSize.first().toFloat()"""
content = content.replace(init_old, init_new)

# 4. captureState
cap_old = "return EditorState(arabicTextX, arabicTextY, translationTextX, translationTextY, surahNameX, surahNameY, fontSize, translationFontSize, textColor)"
cap_new = "return EditorState(arabicTextX, arabicTextY, translationTextX, translationTextY, surahNameX, surahNameY, iconX, iconY, iconSize, iconOpacity, fontSize, translationFontSize, textColor)"
content = content.replace(cap_old, cap_new)

# 5. restoreState
rest_old = """        surahNameX = state.surahX
        surahNameY = state.surahY
        fontSize = state.fontSize"""

rest_new = """        surahNameX = state.surahX
        surahNameY = state.surahY
        iconX = state.iconX
        iconY = state.iconY
        iconSize = state.iconSize
        iconOpacity = state.iconOpacity
        fontSize = state.fontSize"""
content = content.replace(rest_old, rest_new)

rest_save_old = """        savePosition("surah", surahNameX, surahNameY)
        coroutineScope.launch {
            settingsManager.setFontSize(fontSize.roundToInt())"""

rest_save_new = """        savePosition("surah", surahNameX, surahNameY)
        savePosition("icon", iconX, iconY)
        coroutineScope.launch {
            settingsManager.setIconSize(iconSize.roundToInt())
            settingsManager.setIconOpacity(iconOpacity)
            settingsManager.setFontSize(fontSize.roundToInt())"""
content = content.replace(rest_save_old, rest_save_new)

# 6. savePosition
save_old = """                "surah" -> {
                    settingsManager.setSurahNameX(x.roundToInt())
                    settingsManager.setSurahNameY(y.roundToInt())
                }
            }"""

save_new = """                "surah" -> {
                    settingsManager.setSurahNameX(x.roundToInt())
                    settingsManager.setSurahNameY(y.roundToInt())
                }
                "icon" -> {
                    settingsManager.setIconX(x.roundToInt())
                    settingsManager.setIconY(y.roundToInt())
                }
            }"""
content = content.replace(save_old, save_new)

# 7. Offsets in Box handles
surah_offset_old = ".offset { IntOffset((surahNameX * scalePx * 2f).roundToInt(), (surahNameY * scalePx * 2f).roundToInt()) }"
surah_offset_new = ".offset { IntOffset((surahNameX * 2f * scalePx).roundToInt(), (surahNameY * 2f * scalePx).roundToInt()) }"
content = content.replace(surah_offset_old, surah_offset_new)

arabic_offset_old = ".offset { IntOffset((arabicTextX * scalePx * 2f).roundToInt(), ((arabicTextY - 90f) * scalePx * 2f).roundToInt()) }"
arabic_offset_new = ".offset { IntOffset((arabicTextX * 2f * scalePx).roundToInt(), ((arabicTextY * 2f - 90f) * scalePx).roundToInt()) }"
content = content.replace(arabic_offset_old, arabic_offset_new)

trans_offset_old = ".offset { IntOffset((translationTextX * scalePx * 2f).roundToInt(), ((translationTextY - 110f) * scalePx * 2f).roundToInt()) }"
trans_offset_new = ".offset { IntOffset((translationTextX * 2f * scalePx).roundToInt(), ((translationTextY * 2f - 110f) * scalePx).roundToInt()) }"
content = content.replace(trans_offset_old, trans_offset_new)

# 8. Add icon handle
icon_handle = """            // Icon Handle
            if (iconOpacity > 0f || selectedElement == "icon") {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(((iconX * 2f - 150f) * scalePx).roundToInt(), (iconY * 2f * scalePx).roundToInt()) }
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 85.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    undoStack.add(captureState())
                                    redoStack.clear()
                                },
                                onDragEnd = { savePosition("icon", iconX, iconY) },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dx = dragAmount.x
                                    iconX = (iconX + dx / (scalePx * 2f)).coerceIn(-300f, 300f)
                                    iconY = (iconY + dragAmount.y / (scalePx * 2f)).coerceIn(-800f, 800f)
                                }
                            )
                        }
                        .clickable { selectedElement = "icon" }
                        .border(if (selectedElement == "icon") 2.dp else 0.dp, if (selectedElement == "icon") LuxuryGold else Color.Transparent, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text("♡", color = Color.White.copy(alpha = iconOpacity.coerceAtLeast(0.3f)), fontSize = (iconSize / 2).sp)
                }
            }
"""

surah_handle_end = "Text(currentSurah, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)\n            }"

if surah_handle_end in content:
    content = content.replace(surah_handle_end, surah_handle_end + "\n\n" + icon_handle)


# 9. Add icon track to the sidebar
icon_track_old = """                        Box(modifier = Modifier.fillMaxWidth().height(transHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "translation") null else "translation" }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = if (selectedElement == "translation") LuxuryGold else TextMutedColor, modifier = Modifier.size(if (selectedElement == "translation") 24.dp else 16.dp))
                        }"""
icon_track_new = icon_track_old + """
                        Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "icon") null else "icon" }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = if (selectedElement == "icon") LuxuryGold else TextMutedColor, modifier = Modifier.size(if (selectedElement == "icon") 24.dp else 16.dp))
                        }"""
content = content.replace(icon_track_old, icon_track_new)

with open('app/src/main/java/com/example/ui/VideoEditorScreen.kt', 'w') as f:
    f.write(content)

