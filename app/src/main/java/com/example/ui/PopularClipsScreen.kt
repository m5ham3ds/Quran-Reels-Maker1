package com.example.ui

import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.launch
import com.example.utils.NetworkUtils
import com.example.settings.SettingsManager
import com.example.ui.ReelState
import com.example.ui.ReelViewModel
import com.example.SURAH_NAMES
import com.example.LuxuryGold
import com.example.SoftGold
import com.example.ScreenBg
import com.example.CardBg
import com.example.BorderColor
import com.example.TextSoftColor
import com.example.TextMutedColor

data class CuratedClip(
    val id: String,
    val reciter: String,
    val reciterId: String,
    val title: String,
    val surah: Int,
    val ayahStart: Int,
    val ayahEnd: Int,
    val audioUrl: String,
    val category: String,
    val videoQuery: String? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PopularClipsScreen(
    viewModel: ReelViewModel,
    isArabic: Boolean,
    settingsManager: SettingsManager
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val backgroundKeywords by settingsManager.backgroundKeywords.collectAsState(initial = emptySet())
    
    // 1. Audio Preview Player Setup
    val previewPlayer = remember { 
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSourceFactory))
            .build() 
    }
    var playingClipId by remember { mutableStateOf<String?>(null) }
    var isPreviewLoading by remember { mutableStateOf(false) }

    DisposableEffect(previewPlayer) {
        onDispose {
            previewPlayer.release()
        }
    }

    LaunchedEffect(previewPlayer) {
        previewPlayer.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isPreviewLoading = (playbackState == androidx.media3.common.Player.STATE_BUFFERING)
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    playingClipId = null
                }
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("PopularClipsScreen", "ExoPlayer error: ${error.message}", error)
                isPreviewLoading = false
                playingClipId = null
                Toast.makeText(
                    context, 
                    if (isArabic) "تعذر تشغيل العينة: تأكد من اتصالك بالإنترنت" else "Could not play audio: check your connection", 
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // Core built-in Curated database with dynamic video search terms matching custom recitations
    val baseClipsList = remember {
        mutableStateListOf(
            CuratedClip(
                id = "clip_yasser_rahman",
                reciter = "ياسر الدوسري",
                reciterId = "ar.yasserdossari",
                title = "الرحمن • علّم القرآن • خلق الإنسان",
                surah = 55,
                ayahStart = 1,
                ayahEnd = 13,
                audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.yasserdossari/4901.mp3",
                category = "طمأنينة",
                videoQuery = "quran+recitation"
            ),
            CuratedClip(
                id = "clip_yasser_sajdah",
                reciter = "ياسر الدوسري",
                reciterId = "ar.yasserdossari",
                title = "تنزيل الكتاب لا ريب فيه من رب العالمين",
                surah = 32,
                ayahStart = 1,
                ayahEnd = 9,
                audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.yasserdossari/3504.mp3",
                category = "طمأنينة",
                videoQuery = "muslim+praying+mosque"
            ),
            CuratedClip(
                id = "clip_yasser_mulk",
                reciter = "ياسر الدوسري",
                reciterId = "ar.yasserdossari",
                title = "تبارك الذي بيده الملك وهو على كل شيء قدير",
                surah = 67,
                ayahStart = 1,
                ayahEnd = 5,
                audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.yasserdossari/5242.mp3",
                category = "سكينة",
                videoQuery = "islamic+man+reading+quran"
            ),
            CuratedClip(
                id = "clip_yasser_anbiya",
                reciter = "ياسر الدوسري",
                reciterId = "ar.yasserdossari",
                title = "دعاء ذي النون - لا إله إلا أنت سبحانك إني كنت من الظالمين",
                surah = 21,
                ayahStart = 87,
                ayahEnd = 88,
                audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.yasserdossari/2570.mp3",
                category = "دعاء",
                videoQuery = "kaaba+mecca+aesthetic"
            ),
            CuratedClip(
                id = "clip_yasser_infitar",
                reciter = "ياسر الدوسري",
                reciterId = "ar.yasserdossari",
                title = "يا أيها الإنسان ما غرك بربك الكريم",
                surah = 82,
                ayahStart = 6,
                ayahEnd = 12,
                audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.yasserdossari/5835.mp3",
                category = "خشوع",
                videoQuery = "rainy+window+aesthetic"
            ),
            CuratedClip(
                id = "clip_maher_isra",
                reciter = "ماهر المعيقلي",
                reciterId = "ar.mahermuaiqly",
                title = "إن هذا القرآن يهدي للتي هي أقوم ويبشر المؤمنين",
                surah = 17,
                ayahStart = 9,
                ayahEnd = 11,
                audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.mahermuaiqly/2038.mp3",
                category = "طمأنينة",
                videoQuery = "reading+quran+man"
            ),
            CuratedClip(
                id = "clip_maher_kahf",
                reciter = "ماهر المعيقلي",
                reciterId = "ar.mahermuaiqly",
                title = "المال والبنون زينة الحياة الدنيا والباقيات الصالحات",
                surah = 18,
                ayahStart = 46,
                ayahEnd = 49,
                audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.mahermuaiqly/2186.mp3",
                category = "سكينة",
                videoQuery = "nature+waterfall+mountains"
            ),
            CuratedClip(
                id = "clip_alafasy_hashr",
                reciter = "مشاري العفاسي",
                reciterId = "ar.alafasy",
                title = "لو أنزلنا هذا القرآن على جبل لرأيته خاشعاً متصدعاً",
                surah = 59,
                ayahStart = 21,
                ayahEnd = 24,
                audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5147.mp3",
                category = "خشوع",
                videoQuery = "mosque+interior+lighting"
            )
        )
    }

    val categories = listOf(
        if (isArabic) "الكل" else "All",
        if (isArabic) "طمأنينة" else "Tranquility",
        if (isArabic) "خشوع" else "Devotion",
        if (isArabic) "سكينة" else "Serenity",
        if (isArabic) "دعاء" else "Dua"
    )
    
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedClip by remember { mutableStateOf<CuratedClip?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var diagnosticReportText by remember { mutableStateOf("") }
    var isRunningAudit by remember { mutableStateOf(false) }
    var clipToDelete by remember { mutableStateOf<CuratedClip?>(null) }
    val scope = rememberCoroutineScope()

    var customClipsJson by remember { mutableStateOf("[]") }
    
    LaunchedEffect(Unit) {
        customClipsJson = settingsManager.getCustomCuratedClipsSync()
    }

    val saveCustomClipsToSettings: (List<CuratedClip>) -> Unit = { currentList ->
        try {
            val customOnly = currentList.filter { it.id.startsWith("clip_custom") }
            val array = org.json.JSONArray()
            customOnly.forEach { clip ->
                val obj = org.json.JSONObject()
                obj.put("id", clip.id)
                obj.put("reciter", clip.reciter)
                obj.put("reciterId", clip.reciterId)
                obj.put("title", clip.title)
                obj.put("surah", clip.surah)
                obj.put("ayahStart", clip.ayahStart)
                obj.put("ayahEnd", clip.ayahEnd)
                obj.put("audioUrl", clip.audioUrl)
                obj.put("category", clip.category)
                obj.put("videoQuery", clip.videoQuery)
                array.put(obj)
            }
            settingsManager.saveCustomCuratedClipsSync(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(customClipsJson) {
        if (customClipsJson.isNotBlank() && customClipsJson != "[]") {
            try {
                val array = org.json.JSONArray(customClipsJson)
                val newCustomList = mutableListOf<CuratedClip>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    newCustomList.add(
                        CuratedClip(
                            id = obj.getString("id"),
                            reciter = obj.getString("reciter"),
                            reciterId = obj.getString("reciterId"),
                            title = obj.getString("title"),
                            surah = obj.getInt("surah"),
                            ayahStart = obj.getInt("ayahStart"),
                            ayahEnd = obj.getInt("ayahEnd"),
                            audioUrl = obj.getString("audioUrl"),
                            category = obj.getString("category"),
                            videoQuery = obj.optString("videoQuery", "islamic videos")
                        )
                    )
                }
                
                val existingIds = baseClipsList.map { it.id }.toSet()
                val toAdd = newCustomList.filter { !existingIds.contains(it.id) }
                if (toAdd.isNotEmpty()) {
                    baseClipsList.addAll(toAdd)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var showPromptDialog by remember { mutableStateOf(false) }
    var showSystemLogsDialog by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Promotional Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(LuxuryGold.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .border(1.dp, LuxuryGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "توليد المقاطع الرائجة بنقرة واحدة" else "Trending One-Click Production",
                        color = LuxuryGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) 
                            "مقاطع تم تلاوتها بنشيد روحي خاشع وهادئ، جاهزة تلقائياً للمونتاج والتوافق البصري بالذكاء الاصطناعي."
                        else 
                            "Curated, peaceful verses read by elite reciters, optimized instantly for WhisperX word-alignment.",
                        color = TextMutedColor,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LuxuryGold.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = LuxuryGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Active Popular Clip Video Generation Progress UI Controller
        val activeReciterState by viewModel.activeReciterId.collectAsState()
        val isActivePopular = activeReciterState.startsWith("popular|")
        
        AnimatedVisibility(
            visible = state !is ReelState.Idle && isActivePopular,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            var showCancelConfirmationDialog by remember { mutableStateOf(false) }
            val isGenerationPaused by viewModel.isGenerationPausedFlow.collectAsState()
            
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (state) {
                        is ReelState.Error -> {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
                            Text(
                                text = (state as ReelState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.resumeGeneration(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = ScreenBg),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isArabic) "إعادة المحاولة والمتابعة" else "Retry & Resume",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                
                                Button(
                                    onClick = { viewModel.reset() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BorderColor, contentColor = TextSoftColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "إلغاء وتجاوز" else "Dismiss",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isRunningAudit = true
                                    showDiagnosticDialog = true
                                    scope.launch {
                                        val report = com.example.generator.SystemDiagnosticTracker.runFullSystemAudit(context)
                                        diagnosticReportText = report
                                        isRunningAudit = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x22F44336), contentColor = Color(0xFFFF8A80)),
                                border = BorderStroke(1.dp, Color(0x33F44336)),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "تشغيل الفحص والتشخيص الشامل 🔍" else "Run Comprehensive Diagnostics 🔍",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        is ReelState.Loading -> {
                            val loadingState = state as ReelState.Loading
                            CircularProgressIndicator(color = LuxuryGold, strokeWidth = 3.dp)
                            Text(
                                text = loadingState.message,
                                color = TextSoftColor,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            LinearProgressIndicator(
                                progress = { loadingState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LuxuryGold,
                                trackColor = BorderColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.togglePauseGeneration() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isGenerationPaused) LuxuryGold else BorderColor,
                                        contentColor = if (isGenerationPaused) ScreenBg else Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isGenerationPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isGenerationPaused) {
                                            if (isArabic) "استئناف المؤقت" else "Resume Video"
                                        } else {
                                            if (isArabic) "إيقاف المؤقت" else "Pause Video"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Button(
                                    onClick = { showCancelConfirmationDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x11EF5350),
                                        contentColor = Color(0xFFEF5350)
                                    ),
                                    border = BorderStroke(1.dp, Color(0x33EF5350)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "إلغاء العملية" else "Cancel Process",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (showCancelConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = { showCancelConfirmationDialog = false },
                                    title = {
                                        Text(
                                            text = if (isArabic) "تأكيد إلغاء عملية المقطع الرائج" else "Confirm Cancel",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = if (isArabic) "هل أنت متأكد من رغبتك في إلغاء عملية تصميم وإنتاج هذا المقطع الرائج؟" else "Are you sure you want to cancel generating this clip?",
                                            color = TextSoftColor
                                        )
                                    },
                                    confirmButton = {
                                         Button(
                                             onClick = {
                                                 showCancelConfirmationDialog = false
                                                 viewModel.cancelGeneration()
                                             },
                                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350), contentColor = Color.White),
                                             shape = RoundedCornerShape(10.dp)
                                         ) {
                                             Text(text = if (isArabic) "نعم، إلغاء" else "Yes, Cancel", fontWeight = FontWeight.Bold)
                                         }
                                    },
                                    dismissButton = {
                                         Button(
                                             onClick = { showCancelConfirmationDialog = false },
                                             colors = ButtonDefaults.buttonColors(containerColor = BorderColor, contentColor = TextSoftColor),
                                             shape = RoundedCornerShape(10.dp)
                                         ) {
                                             Text(text = if (isArabic) "تراجع" else "Keep Generating", fontWeight = FontWeight.Bold)
                                         }
                                    },
                                    containerColor = ScreenBg,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                        is ReelState.Success -> {
                            val successState = state as ReelState.Success
                            val uri = successState.uri
                            val generatedMeta = successState.generatedMeta
                            
                            Text(
                                text = if (isArabic) "اكتمل مونتاج مقطع الشيخ الرائج بنجاح باهر! 🎉" else "Trending clip created successfully! 🎉",
                                color = LuxuryGold,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 15.sp
                            )

                            val successPlayer = remember(uri) {
                                ExoPlayer.Builder(context).build().apply {
                                    setMediaItem(MediaItem.fromUri(uri))
                                    prepare()
                                    playWhenReady = true
                                }
                            }
                            DisposableEffect(successPlayer) {
                                onDispose {
                                    successPlayer.release()
                                }
                            }

                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = successPlayer
                                        useController = true
                                    }
                                },
                                update = { view ->
                                    view.player = successPlayer
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                                    .clickable {
                                        isRunningAudit = true
                                        showDiagnosticDialog = true
                                        scope.launch {
                                            val report = com.example.generator.SystemDiagnosticTracker.runFullSystemAudit(context)
                                            diagnosticReportText = report
                                            isRunningAudit = false
                                        }
                                    }
                            )

                            Button(
                                onClick = { 
                                    isRunningAudit = true
                                    showDiagnosticDialog = true
                                    scope.launch {
                                        val report = com.example.generator.SystemDiagnosticTracker.runFullSystemAudit(context)
                                        diagnosticReportText = report
                                        isRunningAudit = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardBg, contentColor = LuxuryGold),
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = if (isArabic) "فاحص عمليات الإنتاج النظامي" else "System Operation Diagnostic", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Clean description and hashtags metadata display card
                            generatedMeta?.let { meta ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ScreenBg),
                                    border = BorderStroke(1.dp, BorderColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = if (isArabic) "عنوان التيك توك والانستغرام المقترح :" else "Suggested Captions:",
                                            color = LuxuryGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = meta.tiktok?.description ?: "",
                                            color = TextSoftColor,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = meta.tiktok?.hashtags ?: "",
                                            color = LuxuryGold,
                                            fontSize = 11.sp
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clipData = ClipData.newPlainText("Reel Caption", "${meta.tiktok?.description}\n${meta.tiktok?.hashtags}")
                                                clipboard.setPrimaryClip(clipData)
                                                Toast.makeText(context, if (isArabic) "تم نسخ الكابشن والهاشتاقات بنجاح!" else "Caption copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BorderColor, contentColor = TextSoftColor),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(36.dp)
                                        ) {
                                            Text(if (isArabic) "نسخ الكابشن بالكامل 📋" else "Copy Full Caption 📋", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, if (isArabic) "مشاركة مقطع الشيخ الرائج" else "Share Trending Reel"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = ScreenBg),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isArabic) "مشاركة المقطع" else "Share Clip", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Button(
                                    onClick = { viewModel.reset() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BorderColor, contentColor = TextSoftColor),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isArabic) "إنهاء و تصفح" else "Close", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }
                        ReelState.Idle -> {}
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categories Chips List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) LuxuryGold else CardBg)
                        .border(1.dp, if (isSelected) Color.Transparent else BorderColor, RoundedCornerShape(50.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("category_chip_$cat"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) ScreenBg else TextSoftColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Custom Clip Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_custom_clip_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CardBg,
                contentColor = LuxuryGold
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.4f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "إضافة مقطع رائج مخصص" else "Add Custom Curated Clip",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtering list of clips
        val filteredClips = if (selectedCategory == "الكل" || selectedCategory == "All") {
            baseClipsList
        } else {
            baseClipsList.filter { it.category == selectedCategory }
        }

        if (filteredClips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "لا توجد مقاطع مضافة في هذا التصنيف حالياً" else "No clips found in this category",
                    color = TextMutedColor,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            filteredClips.forEach { clip ->
                val isCurrentSelected = selectedClip?.id == clip.id
                val isPlayingThis = playingClipId == clip.id
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentSelected) CardBg.copy(alpha = 0.8f) else CardBg
                    ),
                    border = BorderStroke(
                        width = if (isCurrentSelected) 1.5.dp else 1.dp,
                        color = if (isCurrentSelected) LuxuryGold else BorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .combinedClickable(
                            onClick = { selectedClip = if (isCurrentSelected) null else clip },
                            onLongClick = { clipToDelete = clip }
                        )
                        .testTag("clip_card_${clip.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // CLICKABLE AUDIO PREVIEW HEADPHONE PIN COUTOUT
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlayingThis) LuxuryGold else LuxuryGold.copy(alpha = 0.15f))
                                        .clickable {
                                            if (isPlayingThis) {
                                                previewPlayer.pause()
                                                playingClipId = null
                                            } else {
                                                if (!NetworkUtils.isNetworkAvailable(context)) {
                                                    Toast.makeText(context, if (isArabic) "تعذر تشغيل العينة: تأكد من اتصالك بالإنترنت" else "Could not play audio: check your connection", Toast.LENGTH_LONG).show()
                                                } else {
                                                    playingClipId = clip.id
                                                    isPreviewLoading = true
                                                    previewPlayer.stop()
                                                    
                                                    val isSocialUrl = clip.audioUrl.contains("youtube.com") || clip.audioUrl.contains("youtu.be") || clip.audioUrl.contains("tiktok.com") || clip.audioUrl.contains("instagram.com")
                                                    if (isSocialUrl) {
                                                        Toast.makeText(context, if (isArabic) "المعاينة الصوتية غير متاحة للروابط الخارجية، يتم معالجتها أثناء المونتاج." else "Audio preview not available for external links, it will be processed during production.", Toast.LENGTH_LONG).show()
                                                        playingClipId = null
                                                        isPreviewLoading = false
                                                    } else {
                                                        previewPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(clip.audioUrl))
                                                        previewPlayer.prepare()
                                                        previewPlayer.playWhenReady = true
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlayingThis && isPreviewLoading) {
                                        CircularProgressIndicator(
                                            color = ScreenBg,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.Headset,
                                            contentDescription = if (isPlayingThis) "إيقاف مؤقت المعاينة الصوتية" else "تقديم سماع المعاينة الصوتية",
                                            tint = if (isPlayingThis) ScreenBg else LuxuryGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = clip.reciter,
                                            color = TextSoftColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Headset,
                                            contentDescription = null,
                                            tint = if (isPlayingThis) LuxuryGold else TextMutedColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    val surahName = SURAH_NAMES.getOrNull(clip.surah - 1) ?: "سورة ${clip.surah}"
                                    val rangeText = if (clip.ayahStart == clip.ayahEnd) "${clip.ayahStart}" else "${clip.ayahStart}-${clip.ayahEnd}"
                                    Text(
                                        text = "$surahName • الآية $rangeText",
                                        color = TextMutedColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LuxuryGold.copy(alpha = 0.08f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = clip.category,
                                    color = LuxuryGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = clip.title,
                            color = SoftGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )

                        AnimatedVisibility(
                            visible = isCurrentSelected,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                HorizontalDivider(color = BorderColor, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = LuxuryGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "رابط البث: ${clip.audioUrl.take(45)}..." else "Source: ${clip.audioUrl.take(45)}...",
                                        color = TextMutedColor,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Generate Trigger Button
                                Button(
                                    onClick = {
                                        if (!NetworkUtils.isNetworkAvailable(context)) {
                                            Toast.makeText(context, if (isArabic) "تعذر بدء المونتاج: تأكد من اتصالك بالإنترنت وحاول مجدداً" else "Could not start: check your internet connection", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        // Stop any ongoing audio preview to prevent overlapping sounds
                                        previewPlayer.stop()
                                        playingClipId = null
                                        
                                        viewModel.generate(
                                            context = context,
                                            surah = clip.surah,
                                            startAyah = clip.ayahStart,
                                            endAyah = clip.ayahEnd,
                                            reciterId = "popular|" + clip.reciterId,
                                            videoQuery = clip.videoQuery
                                        )
                                        Toast.makeText(context, if (isArabic) "بدء المونتاج لـ ${clip.reciter}..." else "Starting production...", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("generate_popular_clip_btn"),
                                    enabled = state !is ReelState.Loading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LuxuryGold,
                                        contentColor = ScreenBg
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isArabic) "إنشاء ريل سينمائي مبارك" else "Create Cinematic Quran Reel",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        
    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FloatingActionButton(
            onClick = { showSystemLogsDialog = true },
            containerColor = CardBg,
            contentColor = LuxuryGold
        ) {
            Icon(Icons.Filled.Warning, contentDescription = "System Diagnostics")
        }
        
        FloatingActionButton(
            onClick = { showPromptDialog = true },
            containerColor = LuxuryGold,
            contentColor = ScreenBg
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Edit Prompt")
        }
    }
    } // Closes Box

    if (showSystemLogsDialog) {
        var logsList by remember { mutableStateOf(emptyList<String>()) }
        
        LaunchedEffect(Unit) {
            while(true) {
                logsList = com.example.generator.SystemDiagnosticTracker.getLogs()
                kotlinx.coroutines.delay(500)
            }
        }
        
        AlertDialog(
            onDismissRequest = { showSystemLogsDialog = false },
            containerColor = CardBg,
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
            title = {
                Text(
                    text = if (isArabic) "سجل الفحص الشامل" else "System Diagnostics Logs",
                    color = TextSoftColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    if (logsList.isEmpty()) {
                        Text("No logs yet...", color = TextMutedColor)
                    } else {
                        logsList.reversed().forEach { logMsg ->
                            Text(
                                text = logMsg,
                                color = TextMutedColor,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider(color = BorderColor.copy(alpha=0.3f))
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clipData = android.content.ClipData.newPlainText("Logs", logsList.joinToString("\n"))
                            cm.setPrimaryClip(clipData)
                            Toast.makeText(context, if (isArabic) "تم نسخ السجل" else "Logs copied", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LuxuryGold),
                        border = BorderStroke(1.dp, LuxuryGold)
                    ) {
                        Text(if (isArabic) "نسخ السجل" else "Copy Logs")
                    }
                    Button(
                        onClick = { showSystemLogsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold)
                    ) {
                        Text(if (isArabic) "إغلاق" else "Close", color = ScreenBg)
                    }
                }
            }
        )
    }

    if (showPromptDialog) {
        val initialPrompt = settingsManager.geminiPrompt.collectAsState(initial = "").value
        var editablePrompt by remember { mutableStateOf(initialPrompt) }
        
        LaunchedEffect(initialPrompt) {
            if (editablePrompt.isEmpty()) {
                editablePrompt = initialPrompt
            }
        }

        AlertDialog(
            onDismissRequest = { showPromptDialog = false },
            containerColor = CardBg,
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
            title = {
                Text(
                    text = if (isArabic) "تعديل موجه جيمي ناي (Gemini Prompt)" else "Edit Gemini Prompt",
                    color = TextSoftColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (isArabic) "تستطيع استخدام المتغيرات التالية: [URL] و [WHISPER_TEXT]" else "You can use the following variables: [URL] and [WHISPER_TEXT]",
                        color = TextMutedColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = editablePrompt,
                        onValueChange = { editablePrompt = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextSoftColor,
                            unfocusedTextColor = TextSoftColor,
                            cursorColor = LuxuryGold
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            settingsManager.saveGeminiPrompt(editablePrompt)
                            showPromptDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold)
                ) {
                    Text(if (isArabic) "حفظ" else "Save", color = ScreenBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromptDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel", color = TextSoftColor)
                }
            }
        )
    }

    // Modern Dialog to Add Custom Clips
    if (showAddDialog) {
        var addReciter by remember { mutableStateOf("") }
        var addTitle by remember { mutableStateOf("") }
        var addSurahStr by remember { mutableStateOf("1") }
        var addStartStr by remember { mutableStateOf("1") }
        var addEndStr by remember { mutableStateOf("1") }
        var addUrl by remember { mutableStateOf("") }
        var isYoutubeUrl by remember { mutableStateOf(false) }
        var addCategory by remember { mutableStateOf("سكينة") }
        var isExtracting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CardBg,
            title = {
                Text(
                    text = if (isArabic) "إضافة مقطع تلاوة جديدة" else "Add New Recitation Clip",
                    color = LuxuryGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = addReciter,
                        onValueChange = { addReciter = it },
                        label = { Text(if (isArabic) "القارئ" else "Reciter") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addTitle,
                        onValueChange = { addTitle = it },
                        label = { Text(if (isArabic) "عنوان المقطع / الآية المقروءة" else "Clip Title / Verse Preview") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = addSurahStr,
                            onValueChange = { addSurahStr = it },
                            label = { Text(if (isArabic) "السورة (رقم)" else "Surah No") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedLabelColor = LuxuryGold
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = addStartStr,
                            onValueChange = { addStartStr = it },
                            label = { Text(if (isArabic) "من آية" else "From") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedLabelColor = LuxuryGold
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = addEndStr,
                            onValueChange = { addEndStr = it },
                            label = { Text(if (isArabic) "إلى آية" else "To") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedLabelColor = LuxuryGold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = addUrl,
                        onValueChange = { addUrl = it },
                        label = { Text(if (isArabic) "رابط مباشر (MP3) أو يوتيوب/تيك توك" else "Direct URL / Youtube / TikTok") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = isYoutubeUrl,
                            onCheckedChange = { isYoutubeUrl = it },
                            colors = CheckboxDefaults.colors(checkedColor = LuxuryGold)
                        )
                        Text(
                            text = if (isArabic) "رابط منصة تواصل (يوتيوب وغيرها)؟" else "Social Platform Link (YouTube)?",
                            color = TextSoftColor,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Simple select category
                    Text(
                        text = if (isArabic) "التصنيف الروحي" else "Spiritual Theme",
                        color = TextSoftColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val availableThemes = listOf("طمأنينة", "خشوع", "سكينة", "دعاء")
                        availableThemes.forEach { th ->
                            val isSelected = addCategory == th
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LuxuryGold else ScreenBg)
                                    .border(1.dp, if (isSelected) Color.Transparent else BorderColor, RoundedCornerShape(8.dp))
                                    .clickable { addCategory = th }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = th,
                                    color = if (isSelected) ScreenBg else TextSoftColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isExtracting,
                    onClick = {
                                if (isExtracting) return@Button
                                
                                if (addUrl.isBlank()) {
                                    Toast.makeText(context, if (isArabic) "يرجى ملء الرابط!" else "Please provide link", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isYoutubeUrl) {
                                    val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                                    val network = cm.activeNetwork
                                    val caps = cm.getNetworkCapabilities(network)
                                    val isOnline = caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                                    if (!isOnline) {
                                        Toast.makeText(context, if (isArabic) "الرجاء التأكد من الاتصال بالانترنت" else "Please check internet connection", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    
                                    isExtracting = true
                                    showSystemLogsDialog = true
                                    com.example.generator.SystemDiagnosticTracker.clearLogs()
                                    scope.launch {
                                try {
                                    Toast.makeText(context, if (isArabic) "جاري جلب المعلومات من خلال Gemini..." else "Fetching AI info...", Toast.LENGTH_LONG).show()
                                    val generator = com.example.generator.GeminiMetaGenerator()
                                    val result = generator.analyzeClipUrl(context, addUrl)
                                    
                                    if (result != null) {
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
                                    }
                                } catch (e: Exception) {
                                    com.example.generator.SystemDiagnosticTracker.addLog("SYSTEM", "استثناء أثناء العملية: ${e.message}")
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isExtracting = false
                                }
                            }
                        } else {
                            val sNum = addSurahStr.toIntOrNull() ?: 1
                            val startNum = addStartStr.toIntOrNull() ?: 1
                            val endNum = addEndStr.toIntOrNull() ?: 1
                            
                            if (addReciter.isBlank() || addTitle.isBlank()) {
                                Toast.makeText(context, if (isArabic) "يرجى ملء جميع الحقول العامة والرابط!" else "Fill all fields", Toast.LENGTH_SHORT).show()
                            } else {
                                val newItem = CuratedClip(
                                        id = "clip_custom_${System.currentTimeMillis()}",
                                        reciter = addReciter,
                                        reciterId = addUrl,
                                        title = addTitle,
                                        surah = sNum,
                                        ayahStart = startNum,
                                        ayahEnd = endNum,
                                        audioUrl = addUrl,
                                        category = addCategory,
                                        videoQuery = if (backgroundKeywords.isNotEmpty()) backgroundKeywords.random() else "islamic architecture nature"
                                    )
                                baseClipsList.add(newItem)
                                saveCustomClipsToSettings(baseClipsList.toList())
                                showAddDialog = false
                                Toast.makeText(context, if (isArabic) "تمت إضافة المقطع الرائج لقائمتك بنجاح!" else "Clip added successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = ScreenBg)
                ) {
                    if (isExtracting) {
                        CircularProgressIndicator(
                            color = ScreenBg,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "جاري..." else "Extracting...")
                    } else {
                        Text(
                            if (isYoutubeUrl) {
                                if (isArabic) "البحث و حفظ البيانات ✨" else "Search & Save ✨"
                            } else {
                                if (isArabic) "حفظ المقرأ" else "Save Clip"
                            }
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isArabic) "إلغاء الأمر" else "Cancel", color = TextMutedColor)
                }
            }
        )
    }

    if (showDiagnosticDialog) {
        com.example.DiagnosticReportDialog(
            reportText = diagnosticReportText,
            isRunning = isRunningAudit,
            isArabic = isArabic,
            onDismiss = { showDiagnosticDialog = false },
            onSaveReport = {
                val path = com.example.generator.SystemDiagnosticTracker.saveReportToFilesAndGetPath(context, diagnosticReportText)
                Toast.makeText(context, if (isArabic) "تم حفظ ملف التقرير بنجاح في:\n$path" else "Report saved successfully at:\n$path", Toast.LENGTH_LONG).show()
            },
            onCopyReport = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("System Diagnostic Report", diagnosticReportText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, if (isArabic) "تم نسخ نص التقرير بالكامل!" else "Full report copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            onShareReport = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, diagnosticReportText)
                }
                context.startActivity(Intent.createChooser(shareIntent, if (isArabic) "مشاركة تقرير النظام" else "Share System Report"))
            }
        )
    }

    clipToDelete?.let { clipInfo ->
        AlertDialog(
            onDismissRequest = { clipToDelete = null },
            containerColor = CardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "حذف المقطع الرائج" else "Delete Popular Clip",
                        color = TextSoftColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = if (isArabic) "هل أنت متأكد من أنك تود حذف هذا المقطع الرائج؟ (${clipInfo.reciter} - سورة ${SURAH_NAMES.getOrNull(clipInfo.surah - 1)} ${clipInfo.ayahStart}-${clipInfo.ayahEnd})" 
                           else "Are you sure you want to delete this popular clip? (${clipInfo.reciter})",
                    color = TextMutedColor,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        baseClipsList.remove(clipInfo)
                        saveCustomClipsToSettings(baseClipsList.toList())
                        clipToDelete = null
                        if (selectedClip?.id == clipInfo.id) {
                            selectedClip = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isArabic) "نعم، احذفه" else "Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { clipToDelete = null }) {
                    Text(if (isArabic) "لا ألغِ الأمر" else "No, Keep", color = TextSoftColor)
                }
            }
        )
    }
}
