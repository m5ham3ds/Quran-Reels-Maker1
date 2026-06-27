package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import java.io.File
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.settings.SettingsManager
import com.example.ui.ReelState
import com.example.ui.ReelViewModel
import com.example.ui.PopularClipsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.social.SocialMediaScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.NetworkUtils
import kotlinx.coroutines.*

fun String.parseArabicOrEnglishDigits(): Int? {
    val englishStr = this.map { ch ->
        when (ch) {
            '٠' -> '0'
            '١' -> '1'
            '٢' -> '2'
            '٣' -> '3'
            '٤' -> '4'
            '٥' -> '5'
            '٦' -> '6'
            '٧' -> '7'
            '٨' -> '8'
            '٩' -> '9'
            else -> ch
        }
    }.joinToString("").trim()
    return englishStr.toIntOrNull()
}

val SURAH_NAMES = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس", "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه", "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم", "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر", "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق", "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة", "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج", "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس", "التكوير", "الإنفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد", "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات", "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص", "الفلق", "الناس"
)

val SURAH_COUNTS = mapOf(1 to 7, 2 to 286, 3 to 200, 4 to 176, 5 to 120, 6 to 165, 7 to 206, 8 to 75, 9 to 129, 10 to 109, 11 to 123, 12 to 111, 13 to 43, 14 to 52, 15 to 99, 16 to 128, 17 to 111, 18 to 110, 19 to 98, 20 to 135, 21 to 112, 22 to 78, 23 to 118, 24 to 64, 25 to 77, 26 to 227, 27 to 93, 28 to 88, 29 to 69, 30 to 60, 31 to 34, 32 to 30, 33 to 73, 34 to 54, 35 to 45, 36 to 83, 37 to 182, 38 to 88, 39 to 75, 40 to 85, 41 to 54, 42 to 53, 43 to 89, 44 to 59, 45 to 37, 46 to 35, 47 to 38, 48 to 29, 49 to 18, 50 to 45, 51 to 60, 52 to 49, 53 to 62, 54 to 55, 55 to 78, 56 to 96, 57 to 29, 58 to 22, 59 to 24, 60 to 13, 61 to 14, 62 to 11, 63 to 11, 64 to 18, 65 to 12, 66 to 12, 67 to 30, 68 to 52, 69 to 52, 70 to 44, 71 to 28, 72 to 28, 73 to 20, 74 to 56, 75 to 40, 76 to 31, 77 to 50, 78 to 40, 79 to 46, 80 to 42, 81 to 29, 82 to 19, 83 to 36, 84 to 25, 85 to 22, 86 to 17, 87 to 19, 88 to 26, 89 to 30, 90 to 20, 91 to 15, 92 to 21, 93 to 11, 94 to 8, 95 to 8, 96 to 19, 97 to 5, 98 to 8, 99 to 8, 100 to 11, 101 to 11, 102 to 8, 103 to 3, 104 to 9, 105 to 5, 106 to 4, 107 to 7, 108 to 3, 109 to 6, 110 to 3, 111 to 5, 112 to 4, 113 to 5, 114 to 6)

// Color Palette for Dark Cinematic Feel
val LuxuryGold = Color(0xFFD29E57)
val SoftGold = Color(0xFFE5C085)
val ScreenBg = Color(0xFF0F0F12)
val CardBg = Color(0xFF18181D)
val BorderColor = Color(0xFF282830)
val TextSoftColor = Color(0xFFE0E0E6)
val TextMutedColor = Color(0xFF9E9EA5)

class MainActivity : ComponentActivity() {
    private val viewModel: ReelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val isDark by settingsManager.themeMode.collectAsState(initial = true)
            val language by settingsManager.language.collectAsState(initial = "ar")
            val isArabic = language == "ar"

            MyApplicationTheme(darkTheme = isDark) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    MainNavigationScaffold(
                        viewModel = viewModel,
                        settingsManager = settingsManager,
                        isArabic = isArabic
                    )
                }
            }
        }
    }
}

@Composable
fun ReelHeader(
    isArabic: Boolean,
    pageTitle: String,
    onMenuClick: () -> Unit
) {
    Surface(
        color = ScreenBg,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = if (isArabic) "القائمة" else "Menu",
                    tint = LuxuryGold,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isArabic) "صانع ريلز القرآن الكريم V1.1" else "Quran Reels Maker V1.1",
                    color = LuxuryGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (pageTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .border(1.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pageTitle,
                            color = LuxuryGold.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = LuxuryGold.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScaffold(
    viewModel: ReelViewModel,
    settingsManager: SettingsManager,
    isArabic: Boolean
) {
    var selectedTab by remember { mutableStateOf("home") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CardBg,
                drawerContentColor = TextSoftColor,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .border(
                        width = 1.dp,
                        color = BorderColor,
                        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                    )
            ) {
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(ScreenBg, CardBg)
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(LuxuryGold.copy(alpha = 0.15f))
                            .border(1.5.dp, LuxuryGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📖",
                            fontSize = 32.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "صانع ريلز القرآن الكريـم V1.1" else "Quran Reels Maker V1.1",
                        color = LuxuryGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isArabic) "المساعد والمنتج الذكي للمقاطع" else "Intelligent Reel Production Assistant",
                        color = TextMutedColor,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                HorizontalDivider(color = BorderColor, thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Items
                val menuItems = listOf(
                    Triple("home", if (isArabic) "الرئيسية" else "Home", Icons.Outlined.Home),
                    Triple("popular", if (isArabic) "المقاطع الرائجة" else "Popular Clips", Icons.Default.Favorite),
                    Triple("font", if (isArabic) "تنسيق الخطوط والأنماط" else "Font & Custom Style", Icons.Default.Edit),
                    Triple("social", if (isArabic) "منصات النشر الفوري" else "Publish Channels", Icons.Default.Share),
                    Triple("settings", if (isArabic) "إعدادات المنصة العامة" else "App Preferences", Icons.Outlined.Settings)
                )

                menuItems.forEach { (tabId, label, icon) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = null, tint = if (selectedTab == tabId) ScreenBg else LuxuryGold) },
                        label = { Text(label, fontWeight = if (selectedTab == tabId) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedTab == tabId,
                        onClick = {
                            selectedTab = tabId
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = LuxuryGold,
                            selectedTextColor = ScreenBg,
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = TextSoftColor
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("drawer_item_$tabId")
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "V 2.5",
                        color = TextMutedColor.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                val pageTitle = when (selectedTab) {
                    "home" -> if (isArabic) "الرئيسية" else "Home"
                    "popular" -> if (isArabic) "الرائجة" else "Trending"
                    "font" -> if (isArabic) "تنسيق الخط" else "Font Style"
                    "social" -> if (isArabic) "النشر والربط" else "Publishing"
                    "settings" -> if (isArabic) "الإعدادات" else "Settings"
                    else -> ""
                }
                ReelHeader(
                    isArabic = isArabic,
                    pageTitle = pageTitle,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = ScreenBg,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = selectedTab == "home",
                        onClick = { selectedTab = "home" },
                        icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                        label = { Text(if (isArabic) "الرئيسية" else "Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScreenBg,
                            selectedTextColor = LuxuryGold,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor,
                            indicatorColor = LuxuryGold
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == "popular",
                        onClick = { selectedTab = "popular" },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text(if (isArabic) "الرائجة" else "Trending") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScreenBg,
                            selectedTextColor = LuxuryGold,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor,
                            indicatorColor = LuxuryGold
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == "font",
                        onClick = { selectedTab = "font" },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        label = { Text(if (isArabic) "الخط" else "Font") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScreenBg,
                            selectedTextColor = LuxuryGold,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor,
                            indicatorColor = LuxuryGold
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == "social",
                        onClick = { selectedTab = "social" },
                        icon = { Icon(Icons.Default.Share, contentDescription = null) },
                        label = { Text(if (isArabic) "النشر" else "Publish") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScreenBg,
                            selectedTextColor = LuxuryGold,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor,
                            indicatorColor = LuxuryGold
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == "settings",
                        onClick = { selectedTab = "settings" },
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        label = { Text(if (isArabic) "الإعدادات" else "Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScreenBg,
                            selectedTextColor = LuxuryGold,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor,
                            indicatorColor = LuxuryGold
                        )
                    )
                }
            },
            containerColor = ScreenBg,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    "home" -> HomeScreen(viewModel = viewModel, isArabic = isArabic, settingsManager = settingsManager)
                    "popular" -> PopularClipsScreen(viewModel = viewModel, isArabic = isArabic, settingsManager = settingsManager)
                    "font" -> FontFormattingScreen(settingsManager = settingsManager, isArabic = isArabic)
                    "social" -> SocialMediaScreen(isArabic = isArabic)
                    "settings" -> SettingsScreen(onNavigateBack = { selectedTab = "home" })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ReelViewModel, isArabic: Boolean, settingsManager: SettingsManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsState()
    val recitersList by viewModel.reciters.collectAsState()
    val isGenerationPaused by viewModel.isGenerationPausedFlow.collectAsState()
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var diagnosticReportText by remember { mutableStateOf("") }
    var isRunningAudit by remember { mutableStateOf(false) }

    var selectedSurahIdx by remember { mutableIntStateOf(0) }
    var startAyahText by remember { mutableStateOf("1") }
    var endAyahText by remember { mutableStateOf("5") }
    var selectedReciterIdx by remember { mutableIntStateOf(0) }

    val ayahsAvailability by viewModel.ayahsAvailability.collectAsState()
    val isCheckingAvailability by viewModel.isCheckingAvailability.collectAsState()

    LaunchedEffect(selectedSurahIdx, startAyahText, endAyahText, selectedReciterIdx, recitersList) {
        val surah = selectedSurahIdx + 1
        val start = startAyahText.parseArabicOrEnglishDigits() ?: 1
        val end = endAyahText.parseArabicOrEnglishDigits() ?: start
        val reciterId = if (recitersList.isNotEmpty() && selectedReciterIdx < recitersList.size) {
            recitersList[selectedReciterIdx].first
        } else {
            "ar.alafasy"
        }
        viewModel.checkCurrentAyahsAvailability(surah, start, end, reciterId)
    }

    var delayedGenerateAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> 
        delayedGenerateAction?.invoke()
        delayedGenerateAction = null
    }

    // Load initial values from settings on start
    val savedSurahIdx by settingsManager.selectedSurahIdx.collectAsState(initial = -1)
    val savedStartAyah by settingsManager.startAyahText.collectAsState(initial = "INITIAL_STATE")
    val savedEndAyah by settingsManager.endAyahText.collectAsState(initial = "INITIAL_STATE")
    val savedReciterId by settingsManager.selectedReciterId.collectAsState(initial = "INITIAL_STATE")

    LaunchedEffect(savedSurahIdx) {
        if (savedSurahIdx >= 0) {
            selectedSurahIdx = savedSurahIdx
        }
    }
    LaunchedEffect(savedStartAyah) {
        if (savedStartAyah != "INITIAL_STATE") {
            startAyahText = savedStartAyah
        }
    }
    LaunchedEffect(savedEndAyah) {
        if (savedEndAyah != "INITIAL_STATE") {
            endAyahText = savedEndAyah
        }
    }
    LaunchedEffect(savedReciterId, recitersList) {
        if (savedReciterId != "INITIAL_STATE" && recitersList.isNotEmpty()) {
            val idx = recitersList.indexOfFirst { it.first == savedReciterId }
            if (idx >= 0) {
                selectedReciterIdx = idx
            }
        }
    }

    var surahExpanded by remember { mutableStateOf(false) }
    var reciterExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ScreenBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Mosque Ring Emblem
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .border(2.dp, LuxuryGold.copy(alpha = 0.3f), CircleShape)
                    .padding(8.dp)
                    .border(1.dp, LuxuryGold, CircleShape)
                    .background(CardBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(54.dp)
                )
            }
            Text(
                text = if (isArabic) "اختر السورة والآيات لبدء الإنشاء" else "Select Surah and Ayahs to start creating",
                color = TextSoftColor,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            // Main Settings Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Surah selection
                    Text(if (isArabic) "اختيار السورة" else "Select Surah", color = TextMutedColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    ExposedDropdownMenuBox(
                        expanded = surahExpanded,
                        onExpandedChange = { surahExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedSurahIdx + 1}. ${SURAH_NAMES[selectedSurahIdx]}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = surahExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSoftColor,
                                unfocusedTextColor = TextSoftColor,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = ScreenBg,
                                unfocusedContainerColor = ScreenBg,
                                disabledContainerColor = ScreenBg,
                                errorContainerColor = ScreenBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = surahExpanded,
                            onDismissRequest = { surahExpanded = false }
                        ) {
                            SURAH_NAMES.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text("${index + 1}. $name", color = TextSoftColor) },
                                    onClick = {
                                        selectedSurahIdx = index
                                        surahExpanded = false
                                        // Reset bounds when surah changes
                                        startAyahText = "1"
                                        endAyahText = ""
                                        scope.launch {
                                            settingsManager.setSelectedSurahIdx(index)
                                            settingsManager.setStartAyahText("1")
                                            settingsManager.setEndAyahText("")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Calculate clean bounds
                    val maxAyahs = SURAH_COUNTS[selectedSurahIdx + 1] ?: 1
                    val cStart = (startAyahText.parseArabicOrEnglishDigits() ?: 1).coerceIn(1, maxAyahs)
                    val cEnd = (endAyahText.parseArabicOrEnglishDigits() ?: cStart).coerceIn(cStart, maxAyahs)
                    val countSelected = cEnd - cStart + 1

                    // Determine border & background colors based on availability
                    val normalBorderFocused = LuxuryGold
                    val normalBorderUnfocused = BorderColor
                    val normalContainer = ScreenBg

                    val greenColor = Color(0xFF499F4C)
                    val redColor = Color(0xFFE53935)

                    val startBorderColor = if (countSelected <= 2 && !isCheckingAvailability && ayahsAvailability.isNotEmpty()) {
                        if (ayahsAvailability[cStart] == true) greenColor else redColor
                    } else {
                        null
                    }

                    val endBorderColor = if (countSelected <= 2 && !isCheckingAvailability && ayahsAvailability.isNotEmpty()) {
                        if (ayahsAvailability[cEnd] == true) greenColor else redColor
                    } else {
                        null
                    }

                    val startContainerColor = if (countSelected <= 2 && !isCheckingAvailability && ayahsAvailability.isNotEmpty()) {
                        if (ayahsAvailability[cStart] == true) greenColor.copy(alpha = 0.08f) else redColor.copy(alpha = 0.08f)
                    } else {
                        normalContainer
                    }

                    val endContainerColor = if (countSelected <= 2 && !isCheckingAvailability && ayahsAvailability.isNotEmpty()) {
                        if (ayahsAvailability[cEnd] == true) greenColor.copy(alpha = 0.08f) else redColor.copy(alpha = 0.08f)
                    } else {
                        normalContainer
                    }

                    // Ayah bounds row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isArabic) "من الآية" else "From Ayah", color = TextMutedColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                            OutlinedTextField(
                                value = startAyahText,
                                onValueChange = { 
                                    startAyahText = it
                                    scope.launch {
                                        settingsManager.setStartAyahText(it)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextSoftColor,
                                    unfocusedTextColor = TextSoftColor,
                                    focusedBorderColor = startBorderColor ?: normalBorderFocused,
                                    unfocusedBorderColor = startBorderColor ?: normalBorderUnfocused,
                                    focusedContainerColor = startContainerColor,
                                    unfocusedContainerColor = startContainerColor,
                                    disabledContainerColor = ScreenBg,
                                    errorContainerColor = ScreenBg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isArabic) "إلى الآية" else "To Ayah", color = TextMutedColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                            OutlinedTextField(
                                value = endAyahText,
                                onValueChange = { 
                                    endAyahText = it
                                    scope.launch {
                                        settingsManager.setEndAyahText(it)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextSoftColor,
                                    unfocusedTextColor = TextSoftColor,
                                    focusedBorderColor = endBorderColor ?: normalBorderFocused,
                                    unfocusedBorderColor = endBorderColor ?: normalBorderUnfocused,
                                    focusedContainerColor = endContainerColor,
                                    unfocusedContainerColor = endContainerColor,
                                    disabledContainerColor = ScreenBg,
                                    errorContainerColor = ScreenBg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Basmalah Option Switch
                    val includeBasmalahState by settingsManager.includeBasmalah.collectAsState(initial = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ScreenBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .clickable {
                                scope.launch {
                                    settingsManager.setIncludeBasmalah(!includeBasmalahState)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = if (isArabic) "البدء بالبسملة" else "Start with Basmalah",
                                color = TextSoftColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isArabic) "إضافة (بسم الله الرحمن الرحيم) في بداية مقطع الفيديو بصوت القارئ المختار وسليمة المزامنة" else "Begin the video rendering with the standard Basmalah recitation and timings",
                                color = TextMutedColor,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = includeBasmalahState,
                            onCheckedChange = { value ->
                                scope.launch {
                                    settingsManager.setIncludeBasmalah(value)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ScreenBg,
                                checkedTrackColor = LuxuryGold,
                                uncheckedThumbColor = TextMutedColor,
                                uncheckedTrackColor = BorderColor
                            ),
                            modifier = Modifier.testTag("basmalah_switch")
                        )
                    }

                    // Availability Indicator & Warning Banners
                    if (isCheckingAvailability) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = LuxuryGold,
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "جاري التحقق من توفر تلاوة الآيات..." else "Verifying verse recitation availability...",
                                color = TextMutedColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (countSelected > 2) {
                        val unavailableList = (cStart..cEnd).filter { ayahsAvailability[it] == false }
                        if (unavailableList.isNotEmpty()) {
                            val alertMsg = if (isArabic) {
                                "⚠️ تنبيه: الآيات التالية غير متوفرة بصوت هذا القارئ على المنصة: ${unavailableList.joinToString("، ")}"
                            } else {
                                "⚠️ Notice: The following Ayahs are not available for this reciter on the platform: ${unavailableList.joinToString(", ")}"
                            }
                            Text(
                                text = alertMsg,
                                color = Color(0xFFEF5350),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE53935).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .border(width = 1.dp, color = Color(0xFFEF5350).copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            )
                        }
                    }

                    // Reciter Dropdown
                    Text(if (isArabic) "القارئ" else "Reciter", color = TextMutedColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    ExposedDropdownMenuBox(
                        expanded = reciterExpanded,
                        onExpandedChange = { reciterExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (recitersList.isNotEmpty() && selectedReciterIdx < recitersList.size) recitersList[selectedReciterIdx].second else (if (isArabic) "جاري التحميل..." else "Loading..."),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reciterExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSoftColor,
                                unfocusedTextColor = TextSoftColor,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = ScreenBg,
                                unfocusedContainerColor = ScreenBg,
                                disabledContainerColor = ScreenBg,
                                errorContainerColor = ScreenBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = reciterExpanded,
                            onDismissRequest = { reciterExpanded = false }
                        ) {
                            recitersList.forEachIndexed { index, reciter ->
                                DropdownMenuItem(
                                    text = { Text(reciter.second, color = TextSoftColor) },
                                    onClick = {
                                        selectedReciterIdx = index
                                        reciterExpanded = false
                                        scope.launch {
                                            settingsManager.setSelectedReciterId(reciter.first)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Create Button
                    if (state is ReelState.Idle || state is ReelState.Error || state is ReelState.Success) {
                        Button(
                            onClick = {
                                if (!NetworkUtils.isNetworkAvailable(context)) {
                                    Toast.makeText(context, if (isArabic) "تعذر بدء العملية: تأكد من اتصالك بالإنترنت" else "Check internet connection", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                val permissionsNeeded = mutableListOf<String>()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                                        permissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO)
                                    }
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                        permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO)
                                    }
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    }
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    }
                                } else {
                                    try {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        }
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                                        }
                                    } catch (e: Exception) {}
                                }

                                val onGenerateAction = {
                                    val start = startAyahText.parseArabicOrEnglishDigits() ?: 1
                                    val end = endAyahText.parseArabicOrEnglishDigits() ?: start
                                    val maxAyahs = SURAH_COUNTS[selectedSurahIdx + 1] ?: 1
                                    
                                    val cStart = start.coerceIn(1, maxAyahs)
                                    val cEnd = end.coerceIn(cStart, maxAyahs)
                                    
                                    viewModel.generate(
                                        context = context,
                                        surah = selectedSurahIdx + 1,
                                        startAyah = cStart,
                                        endAyah = cEnd,
                                        reciterId = if (recitersList.isNotEmpty()) recitersList[selectedReciterIdx].first else "ar.alafasy"
                                    )
                                }

                                if (permissionsNeeded.isNotEmpty()) {
                                    delayedGenerateAction = onGenerateAction
                                    permissionLauncher.launch(permissionsNeeded.toTypedArray())
                                } else {
                                    onGenerateAction()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuxuryGold,
                                contentColor = ScreenBg
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("generate_btn"),
                            shape = RoundedCornerShape(12.dp)



                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = ScreenBg, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "إنشاء الريلز" else "Create Reel",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Status and Results
            val activeReciterState by viewModel.activeReciterId.collectAsState()
            val isActivePopular = activeReciterState.startsWith("popular|")
            AnimatedVisibility(
                visible = state !is ReelState.Idle && !isActivePopular,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                                text = if (isArabic) "استئناف ومحاولة" else "Resume & Retry",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
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
                                            text = if (isArabic) "إعادة البدء" else "Reset / Start Over",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
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
                                    Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isArabic) "تشغيل الفحص والتشخيص الشامل للمشكلة 🔍" else "Run Comprehensive System Diagnostics 🔍",
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
                                                if (isArabic) "استئناف" else "Resume"
                                            } else {
                                                if (isArabic) "إيقاف مؤقت" else "Pause"
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
                                            text = if (isArabic) "إلغاء العملية" else "Cancel",
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
                                                text = if (isArabic) "تأكيد إلغاء العملية" else "Confirm Cancellation",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = if (isArabic) "هل أنت متأكد من رغبتك في إلغاء عملية تصميم وإنتاج هذا المقطع؟" else "Are you sure you want to cancel the generation of this reel?",
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
                                    text = if (isArabic) "تم إنشاء المقطع وسُجل بالاستوديو بنجاح! 🎉" else "Reel created and saved successfully! 🎉",
                                    color = LuxuryGold,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                val exoPlayer = remember(uri) {
                                    ExoPlayer.Builder(context).build().apply {
                                        setMediaItem(MediaItem.fromUri(uri))
                                        prepare()
                                        playWhenReady = true
                                    }
                                }
                                DisposableEffect(exoPlayer) {
                                    onDispose {
                                        exoPlayer.release()
                                    }
                                }

                                AndroidView(
                                    factory = { ctx ->
                                        PlayerView(ctx).apply {
                                            player = exoPlayer
                                            useController = true
                                        }
                                    },
                                    update = { view ->
                                        view.player = exoPlayer
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1711), contentColor = LuxuryGold),
                                    border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = LuxuryGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isArabic) "فحص وتشخيص فوري لهذا المقطع (سير العملية بل كامل) 🔍" else "Run Deep Video Diagnostic Audit 🔍",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                // Insert Gemini Social Media Metadata Section
                                generatedMeta?.let { meta ->
                                    GeneratedMetaSection(meta = meta, isArabic = isArabic)
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
                                            context.startActivity(Intent.createChooser(shareIntent, if (isArabic) "مشاركة المقطع" else "Share Reel"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = ScreenBg),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isArabic) "مشاركة" else "Share", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.reset() },
                                        colors = ButtonDefaults.buttonColors(containerColor = BorderColor, contentColor = TextSoftColor),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isArabic) "إنشاء جديد" else "New Reel", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    if (showDiagnosticDialog) {
        DiagnosticReportDialog(
            reportText = diagnosticReportText,
            isRunning = isRunningAudit,
            isArabic = isArabic,
            onDismiss = { showDiagnosticDialog = false },
            onSaveReport = {
                val path = com.example.generator.SystemDiagnosticTracker.saveReportToFilesAndGetPath(context, diagnosticReportText)
                Toast.makeText(context, if (isArabic) "تم حفظ ملف التقرير بنجاح في:\n$path" else "Report saved successfully at:\n$path", Toast.LENGTH_LONG).show()
            },
            onCopyReport = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("System Diagnostic Report", diagnosticReportText)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontFormattingScreen(settingsManager: SettingsManager, isArabic: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Preferences Collectors
    val fontFamily by settingsManager.fontFamily.collectAsState(initial = "Amiri")
    val fontSize by settingsManager.fontSize.collectAsState(initial = 40)
    val textColorStr by settingsManager.textColor.collectAsState(initial = "#FFD54F")
    val textOpacity by settingsManager.textOpacity.collectAsState(initial = 1.0f)
    
    val showTextBg by settingsManager.showTextBackground.collectAsState(initial = false)
    val textBgColorStr by settingsManager.textBgColor.collectAsState(initial = "#1C1C1E")
    val textBgOpacity by settingsManager.textBgOpacity.collectAsState(initial = 0.6f)
    val textBgRadius by settingsManager.textBgRadius.collectAsState(initial = 16)
    
    val textPosition by settingsManager.textPosition.collectAsState(initial = "Center")
    val textAlign by settingsManager.textAlign.collectAsState(initial = "Center")
    
    val showTranslation by settingsManager.showTranslation.collectAsState(initial = true)
    val translationFontSize by settingsManager.translationFontSize.collectAsState(initial = 25)
    val translationColorStr by settingsManager.translationColor.collectAsState(initial = "#FFFFFF")
    val translationFontFamily by settingsManager.translationFontFamily.collectAsState(initial = "Default")

    // Expansions
    var fontTypeExpanded by remember { mutableStateOf(false) }
    var translationFontExpanded by remember { mutableStateOf(false) }

    // Download fonts effect for live preview
    var triggerRecomposition by remember { mutableStateOf(0) }
    LaunchedEffect(fontFamily, translationFontFamily) {
        withContext(Dispatchers.IO) {
            val client = okhttp3.OkHttpClient()
            val arabicFontUrls = mapOf(
                "Amiri" to "https://github.com/google/fonts/raw/main/ofl/amiriquran/AmiriQuran-Regular.ttf",
                "Cairo" to "https://github.com/google/fonts/raw/main/ofl/cairo/Cairo-Bold.ttf",
                "Scheherazade New" to "https://github.com/google/fonts/raw/main/ofl/scheherazadenew/ScheherazadeNew-Bold.ttf",
                "Lateef" to "https://github.com/google/fonts/raw/main/ofl/lateef/Lateef-Regular.ttf",
                "Reem Kufi" to "https://github.com/google/fonts/raw/main/ofl/reemkufi/ReemKufi-Bold.ttf"
            )
            val englishFontUrls = mapOf(
                "Montserrat" to "https://github.com/google/fonts/raw/main/ofl/montserrat/Montserrat-Medium.ttf",
                "Roboto" to "https://github.com/google/fonts/raw/main/ofl/roboto/static/Roboto-Medium.ttf", // fixed URL
                "Playfair" to "https://github.com/google/fonts/raw/main/ofl/playfairdisplay/PlayfairDisplay-Italic.ttf",
                "Lato" to "https://github.com/google/fonts/raw/main/ofl/lato/Lato-Regular.ttf"
            )

            fun down(name: String, urls: Map<String, String>, prefix: String = "") {
                if (name.startsWith("/")) return
                val file = File(context.cacheDir, prefix + name.replace(" ", "") + ".ttf")
                if (!file.exists() || file.length() < 1000) {
                    urls[name]?.let { url ->
                        try {
                            val request = okhttp3.Request.Builder().url(url).build()
                            client.newCall(request).execute().use { response ->
                                if (response.isSuccessful) {
                                    response.body?.byteStream()?.use { input ->
                                        file.outputStream().use { input.copyTo(it) }
                                    }
                                }
                            }
                        } catch(e: Exception) {}
                    }
                }
            }

            down(fontFamily, arabicFontUrls)
            down(translationFontFamily, englishFontUrls, "EN_")
            triggerRecomposition++
        }
    }

    var showAddFontDialog by remember { mutableStateOf(false) }
    var selectedFontUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var customFontTarget by remember { mutableStateOf("Main") }

    val fontPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFontUri = uri
    }

    if (showAddFontDialog) {
        BasicAlertDialog(
            onDismissRequest = {
                showAddFontDialog = false
                selectedFontUri = null
            }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(if (isArabic) "إضافة خط مخصص" else "Add Custom Font", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    
                    Text(if (isArabic) "اختر مكان تطبيق الخط:" else "Select target:", color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = customFontTarget == "Main", onClick = { customFontTarget = "Main" }, colors = RadioButtonDefaults.colors(selectedColor = LuxuryGold))
                        Text(if (isArabic) "الخط الأساسي" else "Main Font", color = Color.White)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = customFontTarget == "Subtitles", onClick = { customFontTarget = "Subtitles" }, colors = RadioButtonDefaults.colors(selectedColor = LuxuryGold))
                        Text(if (isArabic) "خط الترجمة" else "Subtitles Font", color = Color.White)
                    }

                    Button(
                        onClick = { fontPickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = ScreenBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isArabic) "رفع ملف الخط (TTF/OTF)" else "Upload Font (TTF/OTF)", color = LuxuryGold)
                    }

                    if (selectedFontUri != null) {
                        Text(if (isArabic) "تم اختيار الملف بنجاح" else "File selected successfully", color = Color.Green, fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { 
                            showAddFontDialog = false
                            selectedFontUri = null
                        }) {
                            Text(if (isArabic) "إلغاء" else "Cancel", color = Color.Gray)
                        }
                        TextButton(
                            onClick = {
                                val uri = selectedFontUri
                                if (uri != null) {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val inputStream = context.contentResolver.openInputStream(uri)
                                            if (inputStream != null) {
                                                val outFile = File(context.filesDir, "custom_font_${System.currentTimeMillis()}.ttf")
                                                inputStream.use { input -> outFile.outputStream().use { output -> input.copyTo(output) } }
                                                
                                                if (customFontTarget == "Main") {
                                                    settingsManager.setFontFamily(outFile.absolutePath)
                                                } else {
                                                    settingsManager.setTranslationFontFamily(outFile.absolutePath)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        showAddFontDialog = false
                                        selectedFontUri = null
                                    }
                                }
                            },
                            enabled = selectedFontUri != null
                        ) {
                            Text(if (isArabic) "حفظ" else "Save", color = LuxuryGold)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showAddFontDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Font", tint = LuxuryGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "إضافة خط مخصص" else "Add Custom Font", color = LuxuryGold, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = {
                    Toast.makeText(context, if (isArabic) "تم حفظ التنسيق تلقائياً" else "Style saved automatically", Toast.LENGTH_SHORT).show()
                }) {
                    Text(if (isArabic) "حفظ" else "Save", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            // Section 1: الخط الأساسي
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "الخط الأساسي" else "Primary Font", color = TextSoftColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Font Type Dropdown
                    Text(if (isArabic) "نوع الخط" else "Font Family", color = TextMutedColor, fontSize = 13.sp)
                    ExposedDropdownMenuBox(
                        expanded = fontTypeExpanded,
                        onExpandedChange = { fontTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = when (fontFamily) {
                                "Amiri" -> if (isArabic) "Amiri (أميري)" else "Amiri (Classical)"
                                "Cairo" -> if (isArabic) "Cairo (كايرو)" else "Cairo (Modern)"
                                "Monospace" -> if (isArabic) "Monospace (منسق)" else "Monospace"
                                else -> if (isArabic) "Kufi (كوفي) / الافتراضي" else "Kufic / Default"
                            },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontTypeExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSoftColor,
                                unfocusedTextColor = TextSoftColor,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = ScreenBg,
                                unfocusedContainerColor = ScreenBg,
                                disabledContainerColor = ScreenBg,
                                errorContainerColor = ScreenBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = fontTypeExpanded,
                            onDismissRequest = { fontTypeExpanded = false }
                        ) {
                            val arabicFonts = listOf("Amiri", "Cairo", "Scheherazade New", "Lateef", "Reem Kufi")
                            arabicFonts.forEach { font ->
                                DropdownMenuItem(
                                    text = { Text(font, color = TextSoftColor) },
                                    onClick = {
                                        scope.launch { settingsManager.setFontFamily(font) }
                                        fontTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Font Size Slider
                    Text(if (isArabic) "حجم النص ($fontSize px)" else "Text Size ($fontSize px)", color = TextMutedColor, fontSize = 13.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ScreenBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("1", color = TextMutedColor, fontSize = 12.sp)
                        Slider(
                            value = fontSize.toFloat(),
                            onValueChange = { scope.launch { settingsManager.setFontSize(it.toInt()) } },
                            valueRange = 1f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = LuxuryGold,
                                activeTrackColor = LuxuryGold.copy(alpha = 0.7f),
                                inactiveTrackColor = LuxuryGold.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text("100", color = TextMutedColor, fontSize = 12.sp)
                    }
                }
            }

            // Section 2: اللون والشفافية
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "اللون والشفافية" else "Color & Opacity", color = TextSoftColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Preset Color Dots
                    Text(if (isArabic) "لون النص" else "Text Color", color = TextMutedColor, fontSize = 13.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val colorPresets = listOf(
                            "#FFD54F" to Color(0xFFFFD54F), // Yellow/Gold
                            "#E6D5C3" to Color(0xFFE6D5C3), // Beige
                            "#A5D6A7" to Color(0xFFA5D6A7), // Light Green
                            "#FFFFFF" to Color(0xFFFFFFFF), // White
                        )
                        colorPresets.forEach { (hex, clr) ->
                            val isSelected = textColorStr.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) LuxuryGold else BorderColor,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .background(clr, CircleShape)
                                    .clickable {
                                        scope.launch { settingsManager.setTextColor(hex) }
                                    }
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = if (clr == Color.White) Color.Black else Color.White,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }

                    // Text Opacity Slider
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isArabic) "شفافية النص" else "Text Opacity", color = TextMutedColor, fontSize = 13.sp)
                        Text("${(textOpacity * 100).toInt()}%", color = LuxuryGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Slider(
                        value = textOpacity,
                        onValueChange = { scope.launch { settingsManager.setTextOpacity(it) } },
                        colors = SliderDefaults.colors(
                            thumbColor = LuxuryGold,
                            activeTrackColor = LuxuryGold,
                            inactiveTrackColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 3: خلفية النص
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "خلفية النص" else "Text Background Box", color = TextSoftColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Switch(
                            checked = showTextBg,
                            onCheckedChange = { scope.launch { settingsManager.setShowTextBackground(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ScreenBg,
                                checkedTrackColor = LuxuryGold,
                                uncheckedThumbColor = TextMutedColor,
                                uncheckedTrackColor = BorderColor
                            )
                        )
                    }

                    if (showTextBg) {
                        // Background Opacity
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isArabic) "شفافية الخلفية" else "Background Opacity", color = TextMutedColor, fontSize = 13.sp)
                            Text("${(textBgOpacity * 100).toInt()}%", color = LuxuryGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Slider(
                            value = textBgOpacity,
                            onValueChange = { scope.launch { settingsManager.setTextBgOpacity(it) } },
                            colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold, inactiveTrackColor = BorderColor)
                        )

                        // Corner Radius
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isArabic) "استدارة الزوايا" else "Corner Radius", color = TextMutedColor, fontSize = 13.sp)
                            Text("${textBgRadius}dp", color = LuxuryGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Slider(
                            value = textBgRadius.toFloat(),
                            onValueChange = { scope.launch { settingsManager.setTextBgRadius(it.toInt()) } },
                            valueRange = 0f..40f,
                            colors = SliderDefaults.colors(thumbColor = LuxuryGold, activeTrackColor = LuxuryGold, inactiveTrackColor = BorderColor)
                        )

                        // Background Colors
                        Text(if (isArabic) "اللون" else "Color", color = TextMutedColor, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val bgColors = listOf(
                                "#2C2621" to Color(0xFF2C2621), // Cozy Gold/Bronze
                                "#1C1C1E" to Color(0xFF1C1C1E), // Rich Carbon Grey
                                "#000000" to Color(0xFF000000)  // Pitch Black
                            )
                            bgColors.forEach { (hex, clr) ->
                                val isSelected = textBgColorStr.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) LuxuryGold else BorderColor,
                                            shape = CircleShape
                                        )
                                        .padding(4.dp)
                                        .background(clr, CircleShape)
                                        .clickable {
                                            scope.launch { settingsManager.setTextBgColor(hex) }
                                        }
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: الموضع
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "الموضع" else "Text Alignment Position", color = TextSoftColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val positions = listOf("Top" to "أعلى", "Center" to "وسط", "Bottom" to "أسفل")
                        positions.forEach { (pos, label) ->
                            val isSelected = textPosition == pos
                            Card(
                                onClick = { scope.launch { settingsManager.setTextPosition(pos) } },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) LuxuryGold else ScreenBg
                                ),
                                border = if (!isSelected) BorderStroke(1.dp, BorderColor) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) ScreenBg else TextSoftColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "محاذاة النص للفقرة" else "Paragraph Alignment", color = TextSoftColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val alignments = listOf("Left" to "يسار", "Center" to "وسط", "Right" to "يمين")
                        alignments.forEach { (align, label) ->
                            val isSelected = textAlign == align
                            Card(
                                onClick = { scope.launch { settingsManager.setTextAlign(align) } },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) LuxuryGold else ScreenBg
                                ),
                                border = if (!isSelected) BorderStroke(1.dp, BorderColor) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) ScreenBg else TextSoftColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: الترجمة
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "الترجمة المصاحبة" else "Translation Settings", color = TextSoftColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Switch(
                            checked = showTranslation,
                            onCheckedChange = { scope.launch { settingsManager.setShowTranslation(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ScreenBg,
                                checkedTrackColor = LuxuryGold,
                                uncheckedThumbColor = TextMutedColor,
                                uncheckedTrackColor = BorderColor
                            )
                        )
                    }

                    // Translation Font Family Dropdown
                    Text(if (isArabic) "نوع خط الترجمة" else "Subtitles Font Family", color = TextMutedColor, fontSize = 13.sp)
                    ExposedDropdownMenuBox(
                        expanded = translationFontExpanded,
                        onExpandedChange = { translationFontExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = translationFontFamily,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = translationFontExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSoftColor,
                                unfocusedTextColor = TextSoftColor,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = ScreenBg,
                                unfocusedContainerColor = ScreenBg,
                                disabledContainerColor = ScreenBg,
                                errorContainerColor = ScreenBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = translationFontExpanded,
                            onDismissRequest = { translationFontExpanded = false }
                        ) {
                            val englishFonts = listOf("Montserrat", "Roboto", "Playfair", "Lato")
                            englishFonts.forEach { font ->
                                DropdownMenuItem(
                                    text = { Text(font, color = TextSoftColor) },
                                    onClick = {
                                        scope.launch { settingsManager.setTranslationFontFamily(font) }
                                        translationFontExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Translation Text Size
                    Text(if (isArabic) "حجم خط الترجمة ($translationFontSize px)" else "Subtitles Font Size ($translationFontSize px)", color = TextMutedColor, fontSize = 13.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ScreenBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("1", color = TextMutedColor, fontSize = 12.sp)
                        Slider(
                            value = translationFontSize.toFloat(),
                            onValueChange = { scope.launch { settingsManager.setTranslationFontSize(it.toInt()) } },
                            valueRange = 1f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = LuxuryGold,
                                activeTrackColor = LuxuryGold.copy(alpha = 0.7f),
                                inactiveTrackColor = LuxuryGold.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text("100", color = TextMutedColor, fontSize = 12.sp)
                    }

                    // Translation Color presets
                    Text(if (isArabic) "لون الترجمة" else "Subtitles Text Color", color = TextMutedColor, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val transColors = listOf(
                            "#E6D5C3" to Color(0xFFE6D5C3), // Beige
                            "#FFE082" to Color(0xFFFFE082), // Light Amber
                            "#FFFFFF" to Color(0xFFFFFFFF)  // Pure White
                        )
                        transColors.forEach { (hex, clr) ->
                            val isSelected = translationColorStr.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) LuxuryGold else BorderColor,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .background(clr, CircleShape)
                                    .clickable {
                                        scope.launch { settingsManager.setTranslationColor(hex) }
                                    }
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = if (clr == Color.White) Color.Black else Color.White,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cinematic Interactive Live Preview Section
            Text(
                text = if (isArabic) "معاينة حية للمقطع" else "Live Reel Preview",
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            LivePreviewContainer(
                fontFamily = fontFamily,
                fontSize = fontSize,
                textColorStr = textColorStr,
                textOpacity = textOpacity,
                showTextBg = showTextBg,
                textBgColorStr = textBgColorStr,
                textBgOpacity = textBgOpacity,
                textBgRadius = textBgRadius,
                textPosition = textPosition,
                textAlignStr = textAlign,
                showTranslation = showTranslation,
                translationFontSize = translationFontSize,
                translationColorStr = translationColorStr,
                translationFontFamily = translationFontFamily,
                isArabic = isArabic,
                triggerRecomposition = triggerRecomposition
            )
        }
    }
}

@Composable
fun LivePreviewContainer(
    fontFamily: String,
    fontSize: Int,
    textColorStr: String,
    textOpacity: Float,
    showTextBg: Boolean,
    textBgColorStr: String,
    textBgOpacity: Float,
    textBgRadius: Int,
    textPosition: String,
    textAlignStr: String,
    showTranslation: Boolean,
    translationFontSize: Int,
    translationColorStr: String,
    translationFontFamily: String,
    isArabic: Boolean,
    triggerRecomposition: Int = 0
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Remember font families from cache
    val quranFontFamily = remember(fontFamily, triggerRecomposition) {
        if (fontFamily.startsWith("/")) {
            try { FontFamily(Font(File(fontFamily))) } catch(e: Exception) { FontFamily.Serif }
        } else {
            val file = File(context.cacheDir, fontFamily.replace(" ", "") + ".ttf")
            if (file.exists() && file.length() > 1000) {
                try { FontFamily(Font(file)) } catch(e: Exception) { FontFamily.Serif }
            } else {
                when (fontFamily) {
                    "Amiri" -> FontFamily.Serif
                    "Cairo" -> FontFamily.SansSerif
                    "Monospace" -> FontFamily.Monospace
                    else -> FontFamily.Default
                }
            }
        }
    }
    
    val transFontFamily = remember(translationFontFamily, triggerRecomposition) {
        if (translationFontFamily.startsWith("/")) {
            try { FontFamily(Font(File(translationFontFamily))) } catch(e: Exception) { FontFamily.Default }
        } else {
            val file = File(context.cacheDir, "EN_" + translationFontFamily.replace(" ", "") + ".ttf")
            if (file.exists() && file.length() > 1000) {
                try { FontFamily(Font(file)) } catch(e: Exception) { FontFamily.Default }
            } else {
                when (translationFontFamily) {
                    "Amiri" -> FontFamily.Serif
                    "Cairo" -> FontFamily.SansSerif
                    "Monospace" -> FontFamily.Monospace
                    else -> FontFamily.Default
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF070A14), Color(0xFF140D07))
                )
            )
            .border(2.dp, BorderColor, RoundedCornerShape(24.dp))
    ) {
        // Star sparkle circles inside preview
        Box(modifier = Modifier.size(6.dp).offset(x = 60.dp, y = 140.dp).background(Color.White.copy(alpha = 0.4f), CircleShape))
        Box(modifier = Modifier.size(4.dp).offset(x = 240.dp, y = 80.dp).background(Color.White.copy(alpha = 0.3f), CircleShape))
        Box(modifier = Modifier.size(5.dp).offset(x = 180.dp, y = 300.dp).background(Color.White.copy(alpha = 0.2f), CircleShape))

        // Text Positioner Frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 50.dp),
            contentAlignment = when (textPosition) {
                "Top" -> Alignment.TopCenter
                "Bottom" -> Alignment.BottomCenter
                else -> Alignment.Center
            }
        ) {
            val contentCol = @Composable {
                Column(
                    horizontalAlignment = when (textAlignStr) {
                        "Left" -> Alignment.Start
                        "Right" -> Alignment.End
                        else -> Alignment.CenterHorizontally
                    },
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = if (showTextBg) {
                        val bgColor = try { Color(android.graphics.Color.parseColor(textBgColorStr)) } catch (e: Exception) { Color.Black }
                        Modifier
                            .background(
                                color = bgColor.copy(alpha = textBgOpacity),
                                shape = RoundedCornerShape(textBgRadius.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    } else {
                        Modifier
                    }
                ) {
                    // Quran Arabic Head
                    val rawCol = try { Color(android.graphics.Color.parseColor(textColorStr)) } catch (e: Exception) { Color.White }
                    val quranTextColor = rawCol.copy(alpha = textOpacity)
                    
                    Text(
                        text = "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ",
                        fontFamily = quranFontFamily,
                        fontSize = (fontSize * 0.7f).sp, // Scaled for preview fits
                        fontWeight = FontWeight.Bold,
                        color = quranTextColor,
                        textAlign = when (textAlignStr) {
                            "Left" -> TextAlign.Left
                            "Right" -> TextAlign.Right
                            else -> TextAlign.Center
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // English Subtitle Translation
                    if (showTranslation) {
                        val transColor = try { Color(android.graphics.Color.parseColor(translationColorStr)) } catch (e: Exception) { Color.LightGray }
                        HorizontalDivider(
                            modifier = Modifier
                                .width(50.dp)
                                .alpha(0.3f)
                                .align(Alignment.CenterHorizontally),
                            thickness = 1.dp,
                            color = transColor
                        )
                        Text(
                            text = "Indeed, We have granted you, [O Muhammad], al-Kawthar.",
                            fontFamily = transFontFamily,
                            fontSize = (translationFontSize * 0.65f).sp,
                            fontWeight = FontWeight.Medium,
                            color = transColor,
                            textAlign = when (textAlignStr) {
                                "Left" -> TextAlign.Left
                                "Right" -> TextAlign.Right
                                else -> TextAlign.Center
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            contentCol()
        }

        // Realistic Reel Social Overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 40.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(26.dp))
                }
                Text("1.2K", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Text("48", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            
            // Spinning disk silhouette
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .background(Color.Black, CircleShape)
            )
        }

        // Live Preview Eyes banner (floating footer indicator)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isArabic) "معاينة حية للمقطع" else "Live Reel Mockup View",
                color = TextSoftColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GeneratedMetaSection(
    meta: com.example.generator.GeneratedMetaResult,
    isArabic: Boolean
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isArabic) "🤖 وصف وعناوين النشر بالذكاء الاصطناعي (Gemini)" else "🤖 AI Generated Publishing Metadata (Gemini)",
            color = LuxuryGold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        meta.tiktok?.let { tiktok ->
            PlatformMetaPreviewCard(
                platformName = if (isArabic) "تيك توك (TikTok Reels)" else "TikTok Reels",
                title = tiktok.title,
                description = tiktok.description,
                hashtags = tiktok.hashtags,
                isArabic = isArabic,
                onCopy = {
                    val text = "العنوان: ${tiktok.title}\n\nالوصف:\n${tiktok.description}\n\nالهاشتاجات:\n${tiktok.hashtags}"
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                    Toast.makeText(context, if (isArabic) "تم نسخ تفاصيل تيك توك" else "TikTok metadata copied!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        meta.instagram?.let { instagram ->
            PlatformMetaPreviewCard(
                platformName = if (isArabic) "انستقرام (Instagram Reels)" else "Instagram Reels",
                title = instagram.title,
                description = instagram.description,
                hashtags = instagram.hashtags,
                isArabic = isArabic,
                onCopy = {
                    val text = "العنوان: ${instagram.title}\n\nالوصف:\n${instagram.description}\n\nالهاشتاجات:\n${instagram.hashtags}"
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                    Toast.makeText(context, if (isArabic) "تم نسخ تفاصيل انستقرام" else "Instagram metadata copied!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        meta.facebook?.let { facebook ->
            PlatformMetaPreviewCard(
                platformName = if (isArabic) "فيسبوك (Facebook Reels)" else "Facebook Reels",
                title = facebook.title,
                description = facebook.description,
                hashtags = facebook.hashtags,
                isArabic = isArabic,
                onCopy = {
                    val text = "العنوان: ${facebook.title}\n\nالوصف:\n${facebook.description}\n\nالهاشتاجات:\n${facebook.hashtags}"
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                    Toast.makeText(context, if (isArabic) "تم نسخ تفاصيل فيسبوك" else "Facebook metadata copied!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        meta.youtube?.let { youtube ->
            PlatformMetaPreviewCard(
                platformName = if (isArabic) "يوتيوب (YouTube Shorts)" else "YouTube Shorts",
                title = youtube.title,
                description = youtube.description,
                hashtags = youtube.hashtags,
                isArabic = isArabic,
                onCopy = {
                    val text = "العنوان: ${youtube.title}\n\nالوصف:\n${youtube.description}\n\nالهاشتاجات:\n${youtube.hashtags}"
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                    Toast.makeText(context, if (isArabic) "تم نسخ تفاصيل يوتيوب" else "YouTube metadata copied!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun PlatformMetaPreviewCard(
    platformName: String,
    title: String,
    description: String,
    hashtags: String,
    isArabic: Boolean,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ScreenBg),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = platformName,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                TextButton(
                    onClick = onCopy,
                    colors = ButtonDefaults.textButtonColors(contentColor = LuxuryGold)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "نسخ النصوص" else "Copy Text", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

            Text(
                text = if (isArabic) "العنوان المقترح:" else "Suggested Title:",
                color = TextMutedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = if (isArabic) "الوصف والتعليق المقترح:" else "Suggested Caption:",
                color = TextMutedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = TextSoftColor,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Text(
                text = if (isArabic) "الهاشتاجات الذكية والكلمات المفتاحية:" else "Smart Hashtags & Tags:",
                color = TextMutedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = hashtags,
                color = LuxuryGold.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DiagnosticReportDialog(
    reportText: String,
    isRunning: Boolean,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSaveReport: () -> Unit,
    onCopyReport: () -> Unit,
    onShareReport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = LuxuryGold
                )
                Text(
                    text = if (isArabic) "تقرير التشخيص وفحص النظام الشامل" else "System Diagnostic & Audit Report",
                    color = SoftGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .background(Color(0xFF0F1218), RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                if (isRunning) {
                    Column(
                        modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = LuxuryGold)
                        Text(
                            text = if (isArabic) "جاري تشغيل وفحص كافة عناصر وخوادم النظام..." else "Analyzing all system servers, files and configs...",
                            color = TextSoftColor,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier.verticalScroll(scrollState)
                    ) {
                        Text(
                            text = reportText,
                            color = Color(0xFFE0E6ED),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSaveReport,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = ScreenBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isArabic) "حفظ كملف txt 💾" else "Save to txt 💾", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = onCopyReport,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BorderColor, contentColor = TextSoftColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isArabic) "نسخ النص 📋" else "Copy Code 📋", fontSize = 12.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onShareReport,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BorderColor, contentColor = TextSoftColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isArabic) "مشاركة التقرير 📤" else "Share Report 📤", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isArabic) "إغلاق" else "Close", fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isArabic) "إلغاء الفحص" else "Cancel", fontSize = 12.sp)
                    }
                }
            }
        },
        containerColor = ScreenBg,
        shape = RoundedCornerShape(16.dp)
    )
}
