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

    var arabicTextX by remember { mutableFloatStateOf(0f) }
    var arabicTextY by remember { mutableFloatStateOf(0f) }
    var translationTextX by remember { mutableFloatStateOf(0f) }
    var translationTextY by remember { mutableFloatStateOf(0f) }
    var surahNameX by remember { mutableFloatStateOf(0f) }
    var surahNameY by remember { mutableFloatStateOf(0f) }

    var fontSize by remember { mutableFloatStateOf(50f) }
    var translationFontSize by remember { mutableFloatStateOf(25f) }
    var textColor by remember { mutableStateOf("#FFFFFF") }

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

        fontSize = settingsManager.fontSize.first().toFloat()
        translationFontSize = settingsManager.translationFontSize.first().toFloat()
        textColor = settingsManager.textColor.first()
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
    var isPlaying by remember { mutableStateOf(true) }
    var timelineZoom by remember { mutableStateOf(1f) }
    
    val serviceState by VideoGenerationService.serviceState.collectAsState()
    
    data class Chunk(val arabic: String, val english: String, val startTimeMs: Long, val endTimeMs: Long, val surahName: String)
    var timelineChunks by remember { mutableStateOf<List<Chunk>>(emptyList()) }
    
    LaunchedEffect(Unit) {
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
                        surahName = obj.optString("surahName", "")
                    ))
                }
                timelineChunks = list
            }
        } catch (e: Exception) {}
    }
    
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
        coroutineScope.launch {
            when (element) {
                "arabic" -> {
                    settingsManager.setArabicTextX(x.roundToInt())
                    settingsManager.setArabicTextY(y.roundToInt())
                }
                "translation" -> {
                    settingsManager.setTranslationTextX(x.roundToInt())
                    settingsManager.setTranslationTextY(y.roundToInt())
                }
                "surah" -> {
                    settingsManager.setSurahNameX(x.roundToInt())
                    settingsManager.setSurahNameY(y.roundToInt())
                }
            }
        }
    }

    fun captureState(): EditorState {
        return EditorState(arabicTextX, arabicTextY, translationTextX, translationTextY, surahNameX, surahNameY, fontSize, translationFontSize, textColor)
    }

    fun restoreState(state: EditorState) {
        arabicTextX = state.arabicX
        arabicTextY = state.arabicY
        translationTextX = state.transX
        translationTextY = state.transY
        surahNameX = state.surahX
        surahNameY = state.surahY
        fontSize = state.fontSize
        translationFontSize = state.transFontSize
        textColor = state.textColor
        
        savePosition("arabic", arabicTextX, arabicTextY)
        savePosition("translation", translationTextX, translationTextY)
        savePosition("surah", surahNameX, surahNameY)
        coroutineScope.launch {
            settingsManager.setFontSize(fontSize.roundToInt())
            settingsManager.setTranslationFontSize(translationFontSize.roundToInt())
            settingsManager.setTextColor(textColor)
        }
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
        if (!isPreview) {
            onNavigateBack() // Go back to home to see progress for export
        }
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
                onClick = { triggerReRender(isRetry = true, isPreview = false) },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(if (isArabic) "تصدير" else "Export", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .clickable { selectedElement = null },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(9f / 16f)
                    .background(Color.Black)
            ) {
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

            // Overlay Elements - They act as draggable handles over the video
            
            val activeChunk = timelineChunks.find { currentTime >= it.startTimeMs && currentTime <= it.endTimeMs } ?: timelineChunks.firstOrNull()
            val currentArabic = activeChunk?.arabic ?: if (isArabic) "النص العربي" else "Arabic Text"
            val currentEnglish = activeChunk?.english ?: if (isArabic) "الترجمة" else "Translation"
            val currentSurah = activeChunk?.surahName ?: if (isArabic) "سورة البقرة" else "Surah Al-Baqarah"

            // Surah Name Handle
            Box(
                modifier = Modifier
                    .offset { IntOffset(surahNameX.roundToInt(), surahNameY.roundToInt()) }
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
                                val dx = if (isRtl) -dragAmount.x else dragAmount.x
                                surahNameX = (surahNameX + dx).coerceIn(-300f, 300f)
                                surahNameY = (surahNameY + dragAmount.y).coerceIn(-800f, 800f)
                            }
                        )
                    }
                    .clickable { selectedElement = "surah" }
                    .border(if (selectedElement == "surah") 2.dp else 0.dp, if (selectedElement == "surah") LuxuryGold else Color.Transparent, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(currentSurah, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Arabic Text Handle
            Box(
                modifier = Modifier
                    .offset { IntOffset(arabicTextX.roundToInt(), arabicTextY.roundToInt()) }
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                undoStack.add(captureState())
                                redoStack.clear()
                            },
                            onDragEnd = { savePosition("arabic", arabicTextX, arabicTextY) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dx = if (isRtl) -dragAmount.x else dragAmount.x
                                arabicTextX = (arabicTextX + dx).coerceIn(-300f, 300f)
                                arabicTextY = (arabicTextY + dragAmount.y).coerceIn(-800f, 800f)
                            }
                        )
                    }
                    .clickable { selectedElement = "arabic" }
                    .border(if (selectedElement == "arabic") 2.dp else 0.dp, if (selectedElement == "arabic") LuxuryGold else Color.Transparent, RoundedCornerShape(4.dp))
                    .padding(16.dp)
            ) {
                Text(currentArabic, color = Color(android.graphics.Color.parseColor(textColor)), fontSize = (fontSize / 2).sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }

            // Translation Text Handle
            Box(
                modifier = Modifier
                    .offset { IntOffset(translationTextX.roundToInt(), translationTextY.roundToInt()) }
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                undoStack.add(captureState())
                                redoStack.clear()
                            },
                            onDragEnd = { savePosition("translation", translationTextX, translationTextY) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dx = if (isRtl) -dragAmount.x else dragAmount.x
                                translationTextX = (translationTextX + dx).coerceIn(-300f, 300f)
                                translationTextY = (translationTextY + dragAmount.y).coerceIn(-800f, 800f)
                            }
                        )
                    }
                    .clickable { selectedElement = "translation" }
                    .border(if (selectedElement == "translation") 2.dp else 0.dp, if (selectedElement == "translation") LuxuryGold else Color.Transparent, RoundedCornerShape(4.dp))
                    .padding(16.dp)
            ) {
                Text(currentEnglish, color = Color.White, fontSize = (translationFontSize / 2).sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            
            // Loading Overlay for background change or export
            if (serviceState is ReelState.Loading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.7f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LuxuryGold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text((serviceState as ReelState.Loading).message, color = Color.White)
                    }
                }
            }
            } // Close inner Box
        }

        val bottomPanelHeight by animateDpAsState(if (selectedElement == null) 160.dp else 340.dp)
        // Timeline and Controls Area
        Column(
            modifier = Modifier.fillMaxWidth().height(bottomPanelHeight).background(Color(0xFF1E1E1E))
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
                IconButton(onClick = { triggerReRender(isRetry = false, isPreview = true) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "New Background", tint = LuxuryGold)
                }
                IconButton(onClick = { showResyncDialog = true }) {
                    Icon(Icons.Default.Sync, contentDescription = "Re-sync", tint = LuxuryGold)
                }
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
                            triggerReRender(isRetry = true, isPreview = true)
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
            
            Text(
                text = "${formatTime(currentTime)} / ${formatTime(duration)}",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp)
            )
            
            Divider(color = Color(0xFF333333))

            // Zoom Slider & Timeline Controls
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Slider(
                    value = timelineZoom,
                    onValueChange = { timelineZoom = it },
                    valueRange = 1f..5f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold)
                )
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }

            // Timeline Tracks
            val waveformData = remember { List(100) { Random.nextFloat() * 0.8f + 0.2f } }
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp)) {
                val scrollState = androidx.compose.foundation.rememberScrollState()
                
                val trackWidthDp = if (timelineZoom > 1.0f) (300 * timelineZoom).dp else 300.dp
                val defaultTrackHeight = 24.dp
                val expandedTrackHeight = 64.dp
                val shrunkenTrackHeight = 8.dp
                
                val videoHeight by animateDpAsState(if (selectedElement == "video") expandedTrackHeight else if (selectedElement != null) shrunkenTrackHeight else defaultTrackHeight)
                val audioHeight by animateDpAsState(if (selectedElement == "audio") expandedTrackHeight else if (selectedElement != null) shrunkenTrackHeight else defaultTrackHeight)
                val arabicHeight by animateDpAsState(if (selectedElement == "arabic") expandedTrackHeight else if (selectedElement != null) shrunkenTrackHeight else defaultTrackHeight)
                val transHeight by animateDpAsState(if (selectedElement == "translation") expandedTrackHeight else if (selectedElement != null) shrunkenTrackHeight else defaultTrackHeight)

                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Icon Column
                    Column(modifier = Modifier.width(40.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(videoHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "video") null else "video" }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = if (selectedElement == "video") LuxuryGold else TextMutedColor, modifier = Modifier.size(if (selectedElement == "video") 24.dp else 16.dp))
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
                    }
                    
                    // Scrollable Tracks Container
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 4.dp)) {
                        Column(modifier = Modifier.horizontalScroll(scrollState).width(trackWidthDp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Video Track Content
                            Box(modifier = Modifier.fillMaxWidth().height(videoHeight).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2C)).clickable { selectedElement = if (selectedElement == "video") null else "video" }) {
                                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "video") LuxuryGold.copy(alpha=0.5f) else Color(0xFF444444))) {
                                    if (videoHeight > 20.dp) {
                                        Text(if (isArabic) "الخلفية السينمائية" else "Cinematic Background", color = Color.White, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
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
                                        Box(modifier = Modifier.offset(x = startXDp).width(itemWidthDp).fillMaxHeight().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "arabic") LuxuryGold.copy(alpha=0.5f) else Color(0xFF5E2E2E))) {
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
                                        Box(modifier = Modifier.offset(x = startXDp).width(itemWidthDp).fillMaxHeight().padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedElement == "translation") LuxuryGold.copy(alpha=0.5f) else Color(0xFF5E4B2E))) {
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
                
                        // Scrubber overlay
                        Box(
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                        .pointerInput(duration, timelineZoom) {
                            detectTapGestures { offset ->
                                if (duration > 0) {
                                    val clickX = offset.x + scrollState.value
                                    val totalWidth = trackWidthDp.toPx()
                                    val newTime = (clickX / totalWidth) * duration
                                    exoPlayer.seekTo(newTime.toLong().coerceIn(0L, duration))
                                }
                            }
                        }
                        .pointerInput(duration, timelineZoom) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                if (duration > 0) {
                                    val dragX = change.position.x + scrollState.value
                                    val totalWidth = trackWidthDp.toPx()
                                    val newTime = (dragX / totalWidth) * duration
                                    exoPlayer.seekTo(newTime.toLong().coerceIn(0L, duration))
                                }
                            }
                        }
                ) {
                    val playheadX = if (duration > 0L) {
                        ((currentTime.toFloat() / duration.toFloat()) * trackWidthDp.value)
                    } else 0f
                    val playheadXPixels = with(androidx.compose.ui.platform.LocalDensity.current) { playheadX.dp.toPx() }
                    val visualXPixels = playheadXPixels - scrollState.value
                    val visualXDp = with(androidx.compose.ui.platform.LocalDensity.current) { visualXPixels.toDp() }
                    if (visualXPixels >= 0) {
                        Box(modifier = Modifier.offset(x = visualXDp).fillMaxHeight().width(2.dp).background(Color.Red))
                        Box(modifier = Modifier.offset(x = visualXDp - 4.dp).size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.Red))
                    }
                }
                } // End inner Scroll Box
                } // End Row
            }

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
                                onValueChangeFinished = { coroutineScope.launch { settingsManager.setFontSize(fontSize.roundToInt()) } },
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
                                            coroutineScope.launch { settingsManager.setTextColor(colorStr) }
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
                                onValueChangeFinished = { coroutineScope.launch { settingsManager.setTranslationFontSize(translationFontSize.roundToInt()) } },
                                valueRange = 10f..60f,
                                modifier = Modifier.width(120.dp),
                                colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold)
                            )
                        }
                    } else if (selectedElement == "video") {
                        Button(onClick = { triggerReRender(isRetry = false, isPreview = true) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                            Text(if (isArabic) "تبديل الخلفية" else "Change Background", color = Color.White)
                        }
                    } else {
                        Text(if (isArabic) "اسحب العنصر في الشاشة لتغيير موضعه" else "Drag element in preview to reposition", color = TextMutedColor, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
