package com.example.ui

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.service.VideoGenerationService
import com.example.settings.SettingsManager
import com.example.ui.theme.*
import com.example.LuxuryGold
import com.example.SoftGold
import com.example.ScreenBg
import com.example.CardBg
import com.example.BorderColor
import com.example.TextSoftColor
import com.example.TextMutedColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt
import kotlin.random.Random
import org.json.JSONArray
import org.json.JSONObject
import com.example.ui.ReelState
import com.example.generator.AlignmentCacheManager
import androidx.compose.animation.core.animateDpAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    settingsManager: SettingsManager,
    isArabic: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    data class EditorState(
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
    )
    val undoStack = remember { mutableStateListOf<EditorState>() }
    val redoStack = remember { mutableStateListOf<EditorState>() }
    
    // Read current settings
    var lastGenSurah by remember { mutableIntStateOf(1) }
    var lastGenStartAyah by remember { mutableIntStateOf(1) }
    var lastGenEndAyah by remember { mutableIntStateOf(5) }
    var lastGenExists by remember { mutableStateOf(false) }
    var lastGenReciterId by remember { mutableStateOf("ar.alafasy") }
    var lastGenIncludeBasmalah by remember { mutableStateOf(true) }
    var lastGenVideoQuery by remember { mutableStateOf("") }
    var pexelsApiKey by remember { mutableStateOf("") }
    var videoQuality by remember { mutableStateOf("Ultra") }

    val showTranslation by settingsManager.showTranslation.collectAsState(initial = true)
    val textPosition by settingsManager.textPosition.collectAsState(initial = "Center")

    var arabicTextX by remember { mutableFloatStateOf(0f) }
    var arabicTextY by remember { mutableFloatStateOf(0f) }
    var translationTextX by remember { mutableFloatStateOf(0f) }
    var translationTextY by remember { mutableFloatStateOf(0f) }
    var surahNameX by remember { mutableFloatStateOf(0f) }
    var surahNameY by remember { mutableFloatStateOf(0f) }
    var iconX by remember { mutableFloatStateOf(0f) }
    var iconY by remember { mutableFloatStateOf(0f) }
    var iconSize by remember { mutableFloatStateOf(20f) }
    var iconOpacity by remember { mutableFloatStateOf(0.8f) }

    var fontSize by remember { mutableFloatStateOf(20f) }
    var translationFontSize by remember { mutableFloatStateOf(8f) }
    var textColor by remember { mutableStateOf("#FFFFFF") }

    var quranFontFamily by remember { mutableStateOf("Default") }
    var surahNameFontFamily by remember { mutableStateOf("Default") }
    var translationFontFamily by remember { mutableStateOf("Default") }
    var textAlignStr by remember { mutableStateOf("Center") }
    var showTextBackground by remember { mutableStateOf(true) }
    var textBgColorStr by remember { mutableStateOf("#000000") }
    var textBgOpacity by remember { mutableFloatStateOf(0.4f) }
    var textBgRadius by remember { mutableIntStateOf(16) }
    var surahNameColorStr by remember { mutableStateOf("#FFFFFF") }
    var translationColorStr by remember { mutableStateOf("#FFFFFF") }
    var surahNameOpacity by remember { mutableFloatStateOf(1f) }
    var translationOpacity by remember { mutableFloatStateOf(1f) }
    var textOpacity by remember { mutableFloatStateOf(1f) }
    var textAnimation by remember { mutableStateOf("Fade") }

    var isLocalLoading by remember { mutableStateOf(false) }
    var localLoadingMessage by remember { mutableStateOf("") }
    data class Chunk(val arabic: String, val english: String, val startTimeMs: Long, val endTimeMs: Long, val surahName: String, val bgIndex: Int = 0)
    var timelineChunks by remember { mutableStateOf<List<Chunk>>(emptyList()) }

    LaunchedEffect(Unit) {
        lastGenExists = settingsManager.lastGenExists.first()
        lastGenSurah = settingsManager.lastGenSurah.first()
        lastGenStartAyah = settingsManager.lastGenStartAyah.first()
        lastGenEndAyah = settingsManager.lastGenEndAyah.first()
        lastGenReciterId = settingsManager.lastGenReciterId.first()
        lastGenIncludeBasmalah = settingsManager.lastGenIncludeBasmalah.first()
        lastGenVideoQuery = settingsManager.lastGenVideoQuery.first()
        pexelsApiKey = settingsManager.pexelsApiKey.first()
        videoQuality = settingsManager.videoQuality.first()
        arabicTextX = settingsManager.arabicTextX.first().toFloat()
        arabicTextY = settingsManager.arabicTextY.first().toFloat()
        translationTextX = settingsManager.translationTextX.first().toFloat()
        translationTextY = settingsManager.translationTextY.first().toFloat()
        surahNameX = settingsManager.surahNameX.first().toFloat()
        surahNameY = settingsManager.surahNameY.first().toFloat()
        iconX = settingsManager.iconX.first().toFloat()
        iconY = settingsManager.iconY.first().toFloat()
        iconSize = settingsManager.iconSize.first().toFloat()
        iconOpacity = settingsManager.iconOpacity.first()
        fontSize = settingsManager.fontSize.first().toFloat()
        translationFontSize = settingsManager.translationFontSize.first().toFloat()
        textColor = settingsManager.textColor.first()
        quranFontFamily = settingsManager.fontFamily.first()
        surahNameFontFamily = settingsManager.surahNameFontFamily.first()
        translationFontFamily = settingsManager.translationFontFamily.first()
        textAlignStr = settingsManager.textAlign.first()
        showTextBackground = settingsManager.showTextBackground.first()
        textBgColorStr = settingsManager.textBgColor.first()
        textBgOpacity = settingsManager.textBgOpacity.first()
        textBgRadius = settingsManager.textBgRadius.first()
        surahNameColorStr = settingsManager.surahNameColor.first()
        translationColorStr = settingsManager.translationColor.first()
        surahNameOpacity = settingsManager.surahNameOpacity.first()
        translationOpacity = settingsManager.translationOpacity.first()
        textOpacity = settingsManager.textOpacity.first()
        textAnimation = settingsManager.textAnimation.first()
        
        try {
            val file = File(context.cacheDir, "reel_timeline.json")
            if (file.exists()) {
                val jsonStr = file.readText()
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<Chunk>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Chunk(
                        arabic = obj.getString("arabic"),
                        english = obj.optString("english", ""),
                        startTimeMs = obj.getLong("startTimeMs"),
                        endTimeMs = obj.getLong("endTimeMs"),
                        surahName = obj.optString("surahName", ""),
                        bgIndex = obj.optInt("bgIndex", 0)
                    ))
                }
                timelineChunks = list
            }
        } catch (e: Exception) {}

        val lastGenWasClean = settingsManager.lastGenWasClean.first()
        if (!lastGenWasClean) {
            isLocalLoading = true
            localLoadingMessage = if (isArabic) "جاري تجهيز بيئة المحرر المستقلة..." else "Preparing independent editor environment..."
            coroutineScope.launch {
                val videoGenerator = com.example.generator.VideoGenerator()
                try {
                    videoGenerator.generateReel(
                        context = context,
                        surah = lastGenSurah,
                        startAyah = lastGenStartAyah,
                        endAyah = lastGenEndAyah,
                        reciterId = lastGenReciterId,
                        showTranslation = showTranslation,
                        pexelsApiKey = pexelsApiKey,
                        videoQuality = videoQuality,
                        isRetry = true,
                        isPreviewMode = true,
                        includeBasmalah = lastGenIncludeBasmalah,
                        videoQuery = lastGenVideoQuery,
                        chunkIndexToReplace = -1,
                        onProgress = { msg, _ -> localLoadingMessage = msg },
                        onComplete = { _ ->
                            isLocalLoading = false
                            coroutineScope.launch { settingsManager.setLastGenWasClean(true) }
                        },
                        onError = {
                            isLocalLoading = false
                        }
                    )
                } catch (e: Exception) {
                    isLocalLoading = false
                }
            }
        }
    }

    if (!lastGenExists) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isArabic) "لم تقم بإنشاء أي فيديو بعد للتعديل عليه." else "No video generated yet to edit.",
                color = TextSoftColor
            )
        }
        return
    }

    var selectedElement by remember { mutableStateOf<String?>(null) }
    var showBackgroundOptions by remember { mutableStateOf(false) }
    var showResyncDialog by remember { mutableStateOf(false) }
    var showRecreateDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var timelineZoom by remember { mutableStateOf(1f) }
    var showChangeBgDialog by remember { mutableStateOf(false) }
    var bgIdxToChange by remember { mutableStateOf(-1) }
    var showExportProgress by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0f) }
    var showCancelExportDialog by remember { mutableStateOf(false) }
    var exportJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    val serviceState by VideoGenerationService.serviceState.collectAsState()
    
    val videoFile = File(context.cacheDir, "playable_reel.mp4")
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            if (videoFile.exists()) {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
                prepare()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ALL
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    LaunchedEffect(isLocalLoading) {
        if (!isLocalLoading && videoFile.exists()) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    var currentTime by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    
    LaunchedEffect(exoPlayer) {
        while(true) {
            currentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
            duration = exoPlayer.duration.coerceAtLeast(0L)
            kotlinx.coroutines.delay(100)
        }
    }
    
    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format("%02d:%02d", m, s)
    }
    
    fun savePosition(element: String, x: Float, y: Float) {
        // Do not modify global defaults during preview.
    }

    fun captureState(): EditorState {
        return EditorState(arabicTextX, arabicTextY, translationTextX, translationTextY, surahNameX, surahNameY, iconX, iconY, iconSize, iconOpacity, fontSize, translationFontSize, textColor)
    }

    fun restoreState(state: EditorState) {
        arabicTextX = state.arabicX
        arabicTextY = state.arabicY
        translationTextX = state.transX
        translationTextY = state.transY
        surahNameX = state.surahX
        surahNameY = state.surahY
        iconX = state.iconX
        iconY = state.iconY
        iconSize = state.iconSize
        iconOpacity = state.iconOpacity
        fontSize = state.fontSize
        translationFontSize = state.transFontSize
        textColor = state.textColor
    }

    fun triggerReRender(isRetry: Boolean, isPreview: Boolean = false) {
        exoPlayer.pause()
        val intent = Intent(context, VideoGenerationService::class.java).apply {
            putExtra("surah", lastGenSurah)
            putExtra("startAyah", lastGenStartAyah)
            putExtra("endAyah", lastGenEndAyah)
            putExtra("reciterId", lastGenReciterId)
            putExtra("showTranslation", showTranslation)
            putExtra("includeBasmalah", lastGenIncludeBasmalah)
            putExtra("pexelsApiKey", pexelsApiKey)
            putExtra("videoQuality", videoQuality)
            putExtra("isRetry", isRetry)
            putExtra("isPreviewMode", isPreview)
            putExtra("videoQuery", lastGenVideoQuery)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        onNavigateBack() // Always go back to home when triggering a global service process
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).background(Color(0xFF2C2C2C)).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (undoStack.isNotEmpty()) {
                        redoStack.add(captureState())
                        restoreState(undoStack.removeLast())
                    }
                }, enabled = undoStack.isNotEmpty()) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = if (undoStack.isNotEmpty()) Color.White else Color.Gray)
                }
                IconButton(onClick = {
                    if (redoStack.isNotEmpty()) {
                        undoStack.add(captureState())
                        restoreState(redoStack.removeLast())
                    }
                }, enabled = redoStack.isNotEmpty()) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo", tint = if (redoStack.isNotEmpty()) Color.White else Color.Gray)
                }
            }
            Text(if (isArabic) "محرر الفيديو" else "Video Editor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(
                onClick = { 
                    showExportProgress = true
                    exportJob = coroutineScope.launch {
                        val videoGenerator = com.example.generator.VideoGenerator()
                        try {
                            videoGenerator.generateReel(
                                context = context,
                                surah = lastGenSurah,
                                startAyah = lastGenStartAyah,
                                endAyah = lastGenEndAyah,
                                reciterId = lastGenReciterId,
                                showTranslation = showTranslation,
                                pexelsApiKey = pexelsApiKey,
                                videoQuality = videoQuality,
                                isRetry = true,
                                isPreviewMode = false,
                                includeBasmalah = lastGenIncludeBasmalah,
                                videoQuery = lastGenVideoQuery,
                                arabicTextXOverride = arabicTextX,
                                arabicTextYOverride = arabicTextY,
                                translationTextXOverride = translationTextX,
                                translationTextYOverride = translationTextY,
                                surahNameXOverride = surahNameX,
                                surahNameYOverride = surahNameY,
                                iconXOverride = iconX,
                                iconYOverride = iconY,
                                iconSizeOverride = iconSize,
                                iconOpacityOverride = iconOpacity,
                                fontSizeOverride = fontSize,
                                translationFontSizeOverride = translationFontSize,
                                textColorOverride = textColor,
                                onProgress = { msg, prog -> 
                                    localLoadingMessage = msg
                                    exportProgress = prog
                                },
                                onComplete = { uri ->
                                    showExportProgress = false
                                    android.widget.Toast.makeText(context, if(isArabic) "تم التصدير بنجاح!" else "Exported successfully!", android.widget.Toast.LENGTH_LONG).show()
                                },
                                onError = {
                                    showExportProgress = false
                                }
                            )
                        } catch (e: Exception) {
                            showExportProgress = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(if (isArabic) "تصدير" else "Export", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Preview Area
        Box(modifier = Modifier.fillMaxWidth().weight(1.2f).background(Color.Black)) {
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures(onTap = { selectedElement = null }) },
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(9f / 16f)
                    .background(Color.Black)
            ) {
                val density = androidx.compose.ui.platform.LocalDensity.current.density
                val actualWidthDp = minOf(maxWidth, maxHeight * (9f / 16f))
                val viewWidthPx = actualWidthDp.value * density
                val scalePx = viewWidthPx / 720f

                if (videoFile.exists()) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = if (isArabic) "جاري التحميل..." else "Loading...",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                fun resolveFontFamily(fontName: String, ctx: android.content.Context): androidx.compose.ui.text.font.FontFamily {
                    if (fontName == "Default") return androidx.compose.ui.text.font.FontFamily.Default
                    if (fontName.startsWith("/")) {
                        return try { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(java.io.File(fontName))) } catch (e: Exception) { androidx.compose.ui.text.font.FontFamily.Default }
                    }
                    val nameNoSpace = fontName.replace(" ", "")
                    val file = java.io.File(ctx.cacheDir, "$nameNoSpace.ttf")
                    if (file.exists() && file.length() > 1000) {
                        return try { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(file)) } catch (e: Exception) { androidx.compose.ui.text.font.FontFamily.Default }
                    }
                    return androidx.compose.ui.text.font.FontFamily.Default
                }
                
                val currentSurahFont = remember(surahNameFontFamily) { resolveFontFamily(surahNameFontFamily, context) }
                val currentQuranFont = remember(quranFontFamily) { resolveFontFamily(quranFontFamily, context) }
                val currentTransFont = remember(translationFontFamily) { resolveFontFamily(translationFontFamily, context) }

                val sColor = try { Color(android.graphics.Color.parseColor(surahNameColorStr)) } catch (e: Exception) { Color.White }
                val qColor = try { Color(android.graphics.Color.parseColor(textColor)) } catch (e: Exception) { Color.White }
                val tColor = try { Color(android.graphics.Color.parseColor(translationColorStr)) } catch (e: Exception) { Color.White }
                val tBgColor = try { Color(android.graphics.Color.parseColor(textBgColorStr)) } catch (e: Exception) { Color.Black }
                
                val alignEnum = when(textAlignStr) {
                    "Left" -> androidx.compose.ui.text.style.TextAlign.Left
                    "Right" -> androidx.compose.ui.text.style.TextAlign.Right
                    else -> androidx.compose.ui.text.style.TextAlign.Center
                }

            // Overlay Elements - They act as draggable handles over the video
            
            val activeChunk = timelineChunks.find { currentTime >= it.startTimeMs && currentTime <= it.endTimeMs } ?: timelineChunks.firstOrNull()
            val currentArabic = activeChunk?.arabic ?: if (isArabic) "النص العربي" else "Arabic Text"
            val currentEnglish = activeChunk?.english ?: if (isArabic) "الترجمة" else "Translation"
            val currentSurah = activeChunk?.surahName ?: if (isArabic) "سورة البقرة" else "Surah Al-Baqarah"

            // Surah Name Handle
            Box(
                modifier = Modifier
                    .offset { IntOffset((surahNameX * 2f * scalePx).roundToInt(), (surahNameY * 2f * scalePx).roundToInt()) }
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                undoStack.add(captureState())
                                redoStack.clear()
                            },
                            onDragEnd = { savePosition("surah", surahNameX, surahNameY) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dx = dragAmount.x
                                surahNameX = (surahNameX + dx / (scalePx * 2f)).coerceIn(-300f, 300f)
                                surahNameY = (surahNameY + dragAmount.y / (scalePx * 2f)).coerceIn(-800f, 800f)
                            }
                        )
                    }
                    .clickable { selectedElement = "surah" }
                    .border(if (selectedElement == "surah") 2.dp else 0.dp, if (selectedElement == "surah") LuxuryGold else Color.Transparent, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = currentSurah,
                    fontFamily = currentSurahFont,
                    color = sColor.copy(alpha = surahNameOpacity),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Icon Handle
            if (iconOpacity > 0f || selectedElement == "icon") {
                Box(
                    modifier = Modifier
                        .offset { IntOffset((iconX * 2f * scalePx).roundToInt(), ((iconY * 2f + 45f) * scalePx).roundToInt()) }
                        .align(Alignment.Center)
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
                    Text("♡", color = Color.White.copy(alpha = iconOpacity.coerceAtLeast(0.3f)), fontSize = iconSize.sp)
                }
            }


            // Grouped Text Container (Matches Generator and Live Preview logic)
            Column(
                modifier = Modifier.align(
                    when (textPosition) {
                        "Top" -> Alignment.TopCenter
                        "Bottom" -> Alignment.BottomCenter
                        else -> Alignment.Center
                    }
                ).offset {
                    when (textPosition) {
                        "Top" -> IntOffset(0, (100f * scalePx).roundToInt())
                        "Bottom" -> IntOffset(0, (-100f * scalePx).roundToInt())
                        else -> IntOffset(0, (150f * scalePx).roundToInt())
                    }
                }.then(
                    if (showTextBackground) {
                        Modifier
                            .background(
                                color = tBgColor.copy(alpha = textBgOpacity),
                                shape = RoundedCornerShape(textBgRadius.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    } else {
                        Modifier
                    }
                ),
                horizontalAlignment = when (textAlignStr) {
                    "Left" -> Alignment.Start
                    "Right" -> Alignment.End
                    else -> Alignment.CenterHorizontally
                },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Arabic Text Handle
                Box(
                    modifier = Modifier
                        .offset { IntOffset((arabicTextX * 2f * scalePx).roundToInt(), ((arabicTextY * 2f - 90f) * scalePx).roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    undoStack.add(captureState())
                                    redoStack.clear()
                                },
                                onDragEnd = { savePosition("arabic", arabicTextX, arabicTextY) },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dx = dragAmount.x
                                    arabicTextX = (arabicTextX + dx / (scalePx * 2f)).coerceIn(-300f, 300f)
                                    arabicTextY = (arabicTextY + dragAmount.y / (scalePx * 2f)).coerceIn(-800f, 800f)
                                }
                            )
                        }
                        .clickable { selectedElement = "arabic" }
                        .border(if (selectedElement == "arabic") 2.dp else 0.dp, if (selectedElement == "arabic") LuxuryGold else Color.Transparent, RoundedCornerShape(4.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentArabic,
                        color = qColor.copy(alpha = textOpacity),
                        fontFamily = currentQuranFont,
                        fontSize = (fontSize / 2).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = alignEnum
                    )
                }

                // Translation Text Handle
                if (showTranslation) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((translationTextX * 2f * scalePx).roundToInt(), ((translationTextY * 2f - 115f) * scalePx).roundToInt()) }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        undoStack.add(captureState())
                                        redoStack.clear()
                                    },
                                    onDragEnd = { savePosition("translation", translationTextX, translationTextY) },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val dx = dragAmount.x
                                        translationTextX = (translationTextX + dx / (scalePx * 2f)).coerceIn(-300f, 300f)
                                        translationTextY = (translationTextY + dragAmount.y / (scalePx * 2f)).coerceIn(-800f, 800f)
                                    }
                                )
                            }
                            .clickable { selectedElement = "translation" }
                            .border(if (selectedElement == "translation") 2.dp else 0.dp, if (selectedElement == "translation") LuxuryGold else Color.Transparent, RoundedCornerShape(4.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentEnglish,
                            color = tColor.copy(alpha = translationOpacity),
                            fontFamily = currentTransFont,
                            fontSize = (translationFontSize / 2).sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = alignEnum
                        )
                    }
                }
            }
            
            // Loading Overlay for background change, resync or export
            if (serviceState is ReelState.Loading || isLocalLoading || showExportProgress) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.8f)).pointerInput(Unit) {}, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)).padding(24.dp)) {
                        CircularProgressIndicator(color = LuxuryGold)
                        Spacer(modifier = Modifier.height(16.dp))
                        val msg = if (showExportProgress) localLoadingMessage else if (isLocalLoading) localLoadingMessage else (serviceState as? ReelState.Loading)?.message ?: ""
                        Text(msg, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        if (showExportProgress) {
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.LinearProgressIndicator(progress = { exportProgress }, modifier = Modifier.fillMaxWidth(0.8f), color = LuxuryGold)
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { showCancelExportDialog = true }) {
                                Text(if (isArabic) "إلغاء التصدير" else "Cancel Export", color = Color.Red)
                            }
                        }
                    }
                }
            }
            } // Close BoxWithConstraints
            } // Close inner Box
            } // Close LTR Provider
        } // Close outer Box

        // Timeline and Controls Area
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF1E1E1E))
        ) {
            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    exoPlayer.seekTo(0) 
                }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Start", tint = Color.White)
                }
                IconButton(onClick = { 
                    isPlaying = !isPlaying
                    if (isPlaying) exoPlayer.play() else exoPlayer.pause()
                }) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { showRecreateDialog = true }) {
                    Icon(Icons.Default.Refresh, contentDescription = "New Background", tint = LuxuryGold)
                }
                IconButton(onClick = { showResyncDialog = true }) {
                    Icon(Icons.Default.Sync, contentDescription = "Re-sync", tint = LuxuryGold)
                }
            }
            
            if (showRecreateDialog) {
                AlertDialog(
                    onDismissRequest = { showRecreateDialog = false },
                    title = { Text(if (isArabic) "إعادة إنشاء الفيديو من جديد" else "Recreate Video Entirely", color = Color.White) },
                    text = { Text(if (isArabic) "هل أنت متأكد من أنك تود إعادة إنشاء الفيديو من جديد كلياً؟ سيتم تجاهل كافة التعديلات والمزامنة السابقة وبدء عملية جديدة تماماً." else "Are you sure you want to completely recreate the video? All previous edits and syncs will be discarded.", color = Color.LightGray) },
                    confirmButton = {
                        TextButton(onClick = {
                            showRecreateDialog = false
                            triggerReRender(isRetry = false, isPreview = true)
                        }) {
                            Text(if (isArabic) "نعم، إعادة إنشاء" else "Yes, Recreate", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRecreateDialog = false }) {
                            Text(if (isArabic) "إلغاء" else "Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF2C2C2C)
                )
            }

            if (showResyncDialog) {
                AlertDialog(
                    onDismissRequest = { showResyncDialog = false },
                    title = { Text(if (isArabic) "إعادة مزامنة النصوص" else "Re-sync Texts", color = Color.White) },
                    text = { Text(if (isArabic) "هل أنت متأكد من أنك تود إعادة مزامنة وموائمة النصوص مع الصوت؟ سيتم إرسال البيانات للمعالجة مجدداً." else "Are you sure you want to re-sync texts with audio?", color = Color.LightGray) },
                    confirmButton = {
                        TextButton(onClick = {
                            showResyncDialog = false
                            AlignmentCacheManager.clearCache(context)
                            exoPlayer.pause()
                            isLocalLoading = true
                            localLoadingMessage = if (isArabic) "جاري إعادة المزامنة مع الخادم..." else "Re-syncing with server..."
                            
                            coroutineScope.launch {
                                val videoGenerator = com.example.generator.VideoGenerator()
                                try {
                                    videoGenerator.generateReel(
                                        context = context,
                                        surah = lastGenSurah,
                                        startAyah = lastGenStartAyah,
                                        endAyah = lastGenEndAyah,
                                        reciterId = lastGenReciterId,
                                        showTranslation = showTranslation,
                                        pexelsApiKey = pexelsApiKey,
                                        videoQuality = videoQuality,
                                        isRetry = true,
                                        isPreviewMode = true,
                                        includeBasmalah = lastGenIncludeBasmalah,
                                        videoQuery = lastGenVideoQuery,
                                        onlyUpdateTimeline = true,
                                        onProgress = { msg, _ -> localLoadingMessage = msg },
                                        onComplete = { _ ->
                                            isLocalLoading = false
                                            try {
                                                val file = File(context.cacheDir, "reel_timeline.json")
                                                if (file.exists()) {
                                                    val jsonStr = file.readText()
                                                    val arr = JSONArray(jsonStr)
                                                    val list = mutableListOf<Chunk>()
                                                    for (i in 0 until arr.length()) {
                                                        val obj = arr.getJSONObject(i)
                                                        list.add(Chunk(
                                                            arabic = obj.getString("arabic"),
                                                            english = obj.optString("english", ""),
                                                            startTimeMs = obj.getLong("startTimeMs"),
                                                            endTimeMs = obj.getLong("endTimeMs"),
                                                            surahName = obj.optString("surahName", ""),
                                                            bgIndex = obj.optInt("bgIndex", 0)
                                                        ))
                                                    }
                                                    timelineChunks = list
                                                }
                                            } catch (e: Exception) {}
                                            exoPlayer.stop()
                                            exoPlayer.clearMediaItems()
                                            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
                                            exoPlayer.prepare()
                                            exoPlayer.playWhenReady = true
                                        },
                                        onError = {
                                            isLocalLoading = false
                                        }
                                    )
                                } catch (e: Exception) {
                                    isLocalLoading = false
                                }
                            }
                        }) {
                            Text(if (isArabic) "نعم، مزامنة" else "Yes, Sync", color = LuxuryGold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResyncDialog = false }) {
                            Text(if (isArabic) "إلغاء" else "Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF2C2C2C)
                )
            }
            
            if (showChangeBgDialog) {
                AlertDialog(
                    onDismissRequest = { showChangeBgDialog = false },
                    title = { Text(if (isArabic) "تغيير الخلفية" else "Change Background", color = Color.White) },
                    text = { Text(if (isArabic) "هل أنت متأكد من تغيير هذه الخلفية؟ سيتم جلب مشهد طبيعي جديد بحجم مناسب واستبداله." else "Are you sure you want to change this background? A new nature scene will be fetched and replaced.", color = Color.LightGray) },
                    confirmButton = {
                        TextButton(onClick = {
                            showChangeBgDialog = false
                            exoPlayer.pause()
                            isLocalLoading = true
                            localLoadingMessage = if (isArabic) "جاري استبدال الخلفية ${bgIdxToChange + 1}..." else "Changing BG ${bgIdxToChange + 1}..."
                            
                            coroutineScope.launch {
                                val videoGenerator = com.example.generator.VideoGenerator()
                                try {
                                    videoGenerator.generateReel(
                                        context = context,
                                        surah = lastGenSurah,
                                        startAyah = lastGenStartAyah,
                                        endAyah = lastGenEndAyah,
                                        reciterId = lastGenReciterId,
                                        showTranslation = showTranslation,
                                        pexelsApiKey = pexelsApiKey,
                                        videoQuality = videoQuality,
                                        isRetry = true,
                                        isPreviewMode = true,
                                        includeBasmalah = lastGenIncludeBasmalah,
                                        videoQuery = lastGenVideoQuery,
                                        chunkIndexToReplace = bgIdxToChange,
                                        onlyUpdateTimeline = false,
                                        onProgress = { msg, _ -> localLoadingMessage = msg },
                                        onComplete = { _ ->
                                            isLocalLoading = false
                                            exoPlayer.stop()
                                            exoPlayer.clearMediaItems()
                                            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
                                            exoPlayer.prepare()
                                            exoPlayer.playWhenReady = true
                                        },
                                        onError = {
                                            isLocalLoading = false
                                        }
                                    )
                                } catch (e: Exception) {
                                    isLocalLoading = false
                                }
                            }
                        }) {
                            Text(if (isArabic) "نعم، تبديل" else "Yes, Change", color = LuxuryGold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showChangeBgDialog = false }) {
                            Text(if (isArabic) "إلغاء" else "Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF2C2C2C)
                )
            }
            
            if (showCancelExportDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelExportDialog = false },
                    title = { Text(if (isArabic) "إلغاء التصدير" else "Cancel Export", color = Color.White) },
                    text = { Text(if (isArabic) "هل أنت متأكد من إلغاء عملية التصدير؟" else "Are you sure you want to cancel the export?", color = Color.LightGray) },
                    confirmButton = {
                        TextButton(onClick = {
                            showCancelExportDialog = false
                            showExportProgress = false
                            exportJob?.cancel()
                        }) {
                            Text(if (isArabic) "نعم، إلغاء" else "Yes, Cancel", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelExportDialog = false }) {
                            Text(if (isArabic) "تراجع" else "Go Back", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF2C2C2C)
                )
            }
            
            Text(
                text = "${formatTime(currentTime)} / ${formatTime(duration)}",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp)
            )
            
            Divider(color = Color(0xFF333333))

            // Timeline Tracks
            val waveformData = remember { List(100) { Random.nextFloat() * 0.8f + 0.2f } }
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp)) {
                val scrollState = androidx.compose.foundation.rememberScrollState()
                
                val config = androidx.compose.ui.platform.LocalConfiguration.current
                val screenWidthDp = config.screenWidthDp.dp
                val baseTrackWidth = maxOf(screenWidthDp - 60.dp, 300.dp)
                val trackWidthDp = baseTrackWidth * timelineZoom
                val defaultTrackHeight = 24.dp
                val expandedTrackHeight = 64.dp
                val shrunkenTrackHeight = 8.dp
                
                val videoHeight by animateDpAsState(if (selectedElement?.startsWith("video") == true) expandedTrackHeight else if (selectedElement != null) shrunkenTrackHeight else defaultTrackHeight)
                val audioHeight by animateDpAsState(if (selectedElement == "audio") expandedTrackHeight else if (selectedElement != null) shrunkenTrackHeight else defaultTrackHeight)
                val arabicHeight by animateDpAsState(if (selectedElement == "arabic") expandedTrackHeight else if (selectedElement != null) shrunkenTrackHeight else defaultTrackHeight)
                val transHeight by animateDpAsState(if (selectedElement == "translation") expandedTrackHeight else if (selectedElement != null) shrunkenTrackHeight else defaultTrackHeight)

                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Icon Column
                    Column(modifier = Modifier.width(40.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(videoHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement?.startsWith("video") == true) null else "video_0" }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = if (selectedElement?.startsWith("video") == true) LuxuryGold else TextMutedColor, modifier = Modifier.size(if (selectedElement?.startsWith("video") == true) 24.dp else 16.dp))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(audioHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "audio") null else "audio" }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null, tint = if (selectedElement == "audio") LuxuryGold else TextMutedColor, modifier = Modifier.size(if (selectedElement == "audio") 24.dp else 16.dp))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(arabicHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "arabic") null else "arabic" }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Title, contentDescription = null, tint = if (selectedElement == "arabic") LuxuryGold else TextMutedColor, modifier = Modifier.size(if (selectedElement == "arabic") 24.dp else 16.dp))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(transHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "translation") null else "translation" }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = if (selectedElement == "translation") LuxuryGold else TextMutedColor, modifier = Modifier.size(if (selectedElement == "translation") 24.dp else 16.dp))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "icon") null else "icon" }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = if (selectedElement == "icon") LuxuryGold else TextMutedColor, modifier = Modifier.size(if (selectedElement == "icon") 24.dp else 16.dp))
                        }
                    }
                    
                    // Scrollable Tracks Container
                    val transformableState = androidx.compose.foundation.gestures.rememberTransformableState { zoomChange, _, _ ->
                        timelineZoom = (timelineZoom * zoomChange).coerceIn(1f, 5f)
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 4.dp).transformable(transformableState)) {
                        Box(modifier = Modifier.fillMaxSize().horizontalScroll(scrollState)) {
                            Box(modifier = Modifier.width(trackWidthDp).fillMaxHeight()) {
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(20.dp)
                                            .pointerInput(duration, trackWidthDp) {
                                                detectTapGestures { offset ->
                                                    if (duration > 0) {
                                                        val newTime = (offset.x / trackWidthDp.toPx()) * duration
                                                        exoPlayer.seekTo(newTime.toLong().coerceIn(0L, duration))
                                                    }
                                                }
                                            }
                                            .pointerInput(duration, trackWidthDp) {
                                                detectDragGestures { change, _ ->
                                                    change.consume()
                                                    if (duration > 0) {
                                                        val newTime = (change.position.x / trackWidthDp.toPx()) * duration
                                                        exoPlayer.seekTo(newTime.toLong().coerceIn(0L, duration))
                                                    }
                                                }
                                            }
                                    )
                                    // Video Track Content
                                    Box(modifier = Modifier.fillMaxWidth().height(videoHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement?.startsWith("video") == true) null else "video_0" }) {
                                        if (timelineChunks.isNotEmpty() && duration > 0) {
                                            val bgSegments = timelineChunks.groupBy { it.bgIndex }
                                            for ((bgIdx, chunks) in bgSegments) {
                                                val startTimeMs = chunks.minOf { it.startTimeMs }
                                                val endTimeMs = chunks.maxOf { it.endTimeMs }
                                                val startPercent = (startTimeMs.toFloat() / duration).coerceIn(0f, 1f)
                                                val widthPercent = ((endTimeMs - startTimeMs).toFloat() / duration).coerceIn(0.01f, 1f)
                                                val startXDp = trackWidthDp * startPercent
                                                val itemWidthDp = trackWidthDp * widthPercent
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = startXDp)
                                                        .width(itemWidthDp)
                                                        .fillMaxHeight()
                                                        .padding(vertical = 4.dp, horizontal = 1.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (selectedElement == "video_$bgIdx") LuxuryGold.copy(alpha=0.5f) else Color(0xFF444444))
                                                        .clickable { selectedElement = "video_$bgIdx" }
                                                ) {
                                                    if (videoHeight > 20.dp) {
                                                        Text(if (isArabic) "خلفية ${bgIdx + 1}" else "BG ${bgIdx + 1}", color = Color.White, fontSize = 8.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp))
                                                    }
                                                }
                                            }
                                        } else {
                                            Box(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "video_0") LuxuryGold.copy(alpha=0.5f) else Color(0xFF444444))) {
                                                if (videoHeight > 20.dp) {
                                                    Text(if (isArabic) "الخلفية السينمائية" else "Cinematic Background", color = Color.White, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
                                                }
                                            }
                                        }
                                    }
                            
                            // Audio Track Content
                            Box(modifier = Modifier.fillMaxWidth().height(audioHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "audio") null else "audio" }) {
                                Box(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "audio") LuxuryGold.copy(alpha=0.5f) else Color(0xFF2E5E4E))
                                    .drawBehind {
                                        val barWidth = 4.dp.toPx()
                                        val gap = 2.dp.toPx()
                                        val count = (size.width / (barWidth + gap)).toInt()
                                        val actualCount = minOf(count, waveformData.size)
                                        for (i in 0 until actualCount) {
                                            val height = size.height * waveformData[i]
                                            val x = i * (barWidth + gap) + barWidth / 2
                                            val yOffset = (size.height - height) / 2
                                            drawLine(color = Color.White.copy(alpha=0.7f), start = Offset(x, yOffset), end = Offset(x, yOffset + height), strokeWidth = barWidth, cap = StrokeCap.Round)
                                        }
                                    }
                                ) {
                                    if (audioHeight > 20.dp) {
                                        Text(if (isArabic) "تلاوة القارئ" else "Recitation Audio", color = Color.White, fontSize = 10.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 2.dp))
                                    }
                                }
                            }
                            // Arabic Track Content
                            Box(modifier = Modifier.fillMaxWidth().height(arabicHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "arabic") null else "arabic" }) {
                                if (timelineChunks.isNotEmpty() && duration > 0) {
                                    for (chunk in timelineChunks) {
                                        val startPercent = (chunk.startTimeMs.toFloat() / duration).coerceIn(0f, 1f)
                                        val widthPercent = ((chunk.endTimeMs - chunk.startTimeMs).toFloat() / duration).coerceIn(0.01f, 1f)
                                        val startXDp = trackWidthDp * startPercent
                                        val itemWidthDp = trackWidthDp * widthPercent
                                        Box(modifier = Modifier.padding(start = startXDp).width(itemWidthDp).fillMaxHeight().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "arabic") LuxuryGold.copy(alpha=0.5f) else Color(0xFF5E2E2E))) {
                                            if (arabicHeight > 20.dp) {
                                                Text(chunk.arabic, color = Color.White, fontSize = 8.sp, maxLines = 1, modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 2.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "arabic") LuxuryGold.copy(alpha=0.5f) else Color(0xFF5E2E2E))) {
                                        if (arabicHeight > 20.dp) Text(if (isArabic) "النص العربي" else "Arabic Text", color = Color.White, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp))
                                    }
                                }
                            }
                    
                            // Translation Track Content
                            Box(modifier = Modifier.fillMaxWidth().height(transHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "translation") null else "translation" }) {
                                if (timelineChunks.isNotEmpty() && duration > 0) {
                                    for (chunk in timelineChunks) {
                                        val startPercent = (chunk.startTimeMs.toFloat() / duration).coerceIn(0f, 1f)
                                        val widthPercent = ((chunk.endTimeMs - chunk.startTimeMs).toFloat() / duration).coerceIn(0.01f, 1f)
                                        val startXDp = trackWidthDp * startPercent
                                        val itemWidthDp = trackWidthDp * widthPercent
                                        Box(modifier = Modifier.padding(start = startXDp).width(itemWidthDp).fillMaxHeight().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "translation") LuxuryGold.copy(alpha=0.5f) else Color(0xFF5E4B2E))) {
                                            if (transHeight > 20.dp) {
                                                Text(chunk.english ?: "", color = Color.White, fontSize = 8.sp, maxLines = 1, modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 2.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "translation") LuxuryGold.copy(alpha=0.5f) else Color(0xFF5E4B2E))) {
                                        if (transHeight > 20.dp) Text(if (isArabic) "الترجمة" else "Translation", color = Color.White, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp))
                                    }
                                }
                            }
                        }
                                                
                        // Playhead overlay
                        val playheadX = if (duration > 0L) {
                            ((currentTime.toFloat() / duration.toFloat()) * trackWidthDp.value).dp
                        } else 0.dp
                        
                        Box(modifier = Modifier.offset(x = playheadX).fillMaxHeight().width(2.dp).background(Color.Red))
                        Box(
                            modifier = Modifier
                                .offset(x = playheadX - 10.dp)
                                .width(20.dp)
                                .height(20.dp)
                                .pointerInput(duration, trackWidthDp) {
                                    var dragPosition = 0f
                                    detectDragGestures(
                                        onDragStart = { _ -> dragPosition = playheadX.toPx() }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        dragPosition += dragAmount.x
                                        if (duration > 0) {
                                            val newTime = (dragPosition / trackWidthDp.toPx()) * duration
                                            exoPlayer.seekTo(newTime.toLong().coerceIn(0L, duration))
                                        }
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.align(Alignment.Center).size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.Red))
                        }
                    }
                } // End Row
            }
            } // End CompositionLocalProvider

            // Context Menu / Options for selected item
            if (selectedElement != null) {
                Divider(color = Color(0xFF333333))
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp).background(Color(0xFF1E1E1E)),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedElement == "arabic") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isArabic) "الحجم" else "Size", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = fontSize,
                                onValueChange = { fontSize = it },
                                valueRange = 20f..100f,
                                modifier = Modifier.width(120.dp),
                                colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold)
                            )
                        }
                        
                        // Color options
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("#FFFFFF", "#D29E57", "#FFD700", "#FF0000", "#00FF00", "#0000FF").forEach { colorStr ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(colorStr)))
                                        .border(2.dp, if (textColor == colorStr) Color.White else Color.Transparent, androidx.compose.foundation.shape.CircleShape)
                                        .clickable { 
                                            textColor = colorStr
                                        }
                                )
                            }
                        }
                    } else if (selectedElement == "translation") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isArabic) "الحجم" else "Size", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = translationFontSize,
                                onValueChange = { translationFontSize = it },
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
                                valueRange = 0f..1f,
                                modifier = Modifier.width(100.dp),
                                colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold)
                            )
                        }
                    } else if (selectedElement?.startsWith("video") == true) {
                        val bgIdx = selectedElement?.substringAfter("video_")?.toIntOrNull() ?: 0
                        Button(onClick = {
                            bgIdxToChange = bgIdx
                            showChangeBgDialog = true
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                            Text(if (isArabic) "تبديل خلفية ${bgIdx + 1}" else "Change BG ${bgIdx + 1}", color = Color.White)
                        }
                    } else {
                        Text(if (isArabic) "اسحب العنصر في الشاشة لتغيير موضعه" else "Drag element in preview to reposition", color = TextMutedColor, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
}
}
