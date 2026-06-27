package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.SettingsManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val settingsManager = remember { SettingsManager(context) }
    val scope = rememberCoroutineScope()

    val pexelsKey by settingsManager.pexelsApiKey.collectAsState(initial = "")
    val pixabayKey by settingsManager.pixabayApiKey.collectAsState(initial = "")
    val geminiKey by settingsManager.geminiApiKey.collectAsState(initial = "")
    val geminiModel by settingsManager.geminiModel.collectAsState(initial = "gemini-3.5-flash")
    val isDark by settingsManager.themeMode.collectAsState(initial = true)
    val showTrans by settingsManager.showTranslation.collectAsState(initial = true)
    val language by settingsManager.language.collectAsState(initial = "ar")
    val videoQuality by settingsManager.videoQuality.collectAsState(initial = "Ultra")
    val backgroundKeywords by settingsManager.backgroundKeywords.collectAsState(initial = emptySet())

    val isArabic = language == "ar"
    
    var showInlineDiagnostics by remember { mutableStateOf(false) }
    var logsList by remember { mutableStateOf(emptyList<String>()) }
    
    LaunchedEffect(showInlineDiagnostics) {
        if (showInlineDiagnostics) {
            while(true) {
                logsList = com.example.generator.SystemDiagnosticTracker.getLogs()
                kotlinx.coroutines.delay(500)
            }
        }
    }

    // App Colors matching MainActivity
    val ScreenBg = Color(0xFF0F0F12)
    val CardBg = Color(0xFF18181D)
    val LuxuryGold = Color(0xFFD29E57)
    val TextMutedColor = Color(0xFF9E9EA5)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Appearance and Language Card
                Text(
                    text = if (isArabic) "المظهر واللغة" else "Appearance & Language",
                    color = LuxuryGold, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardBg, 
                    border = BorderStroke(1.dp, Color(0x15FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Theme switches
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LuxuryGold.copy(alpha=0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = LuxuryGold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "الوضع الداكن" else "Dark Theme",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isArabic) "مريح للعين وخيار مثالي للتطوير" else "Comfortable dark palette layout",
                                    color = TextMutedColor,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = isDark,
                                onCheckedChange = { scope.launch { settingsManager.setThemeMode(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LuxuryGold,
                                    checkedTrackColor = LuxuryGold.copy(alpha=0.4f)
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0x15FFFFFF))

                        // Translation switches
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LuxuryGold.copy(alpha=0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = LuxuryGold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "ترجمة المعاني" else "Interpretive Translation",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isArabic) "عرض الترجمة الأجنبية تحت الآية" else "Append translated verses to screen",
                                    color = TextMutedColor,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = showTrans,
                                onCheckedChange = { scope.launch { settingsManager.setShowTranslation(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LuxuryGold,
                                    checkedTrackColor = LuxuryGold.copy(alpha=0.4f)
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0x15FFFFFF))

                        // Language picker Custom Dropdown
                        Column {
                            Text(
                                text = if (isArabic) "لغة واجهة التطبيق" else "Terminal Language",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            var langExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                onClick = { langExpanded = true },
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
                                        text = if (language == "ar") "العربية" else "English",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = Color(0xFFCFD8DC)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = langExpanded,
                                onDismissRequest = { langExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(CardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("اللغة العربية", color = Color.White, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        scope.launch { settingsManager.setLanguage("ar") }
                                        langExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("English Language", color = Color.White, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        scope.launch { settingsManager.setLanguage("en") }
                                        langExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0x15FFFFFF))

                    // Video Quality Dropdown
                    Column {
                        Text(
                            text = if (isArabic) "جودة المشاهد السينمائية" else "Video Library Quality",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        var qualityExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                onClick = { qualityExpanded = true },
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
                                        text = when(videoQuality) {
                                            "Normal" -> if (isArabic) "عادية" else "Normal"
                                            "High" -> if (isArabic) "عالية" else "High"
                                            else -> if (isArabic) "عالية جدآ" else "Ultra"
                                        },
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Info, // Generic icon
                                        contentDescription = null,
                                        tint = Color(0xFFCFD8DC)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = qualityExpanded,
                                onDismissRequest = { qualityExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(CardBg)
                            ) {
                                val options = listOf("Normal", "High", "Ultra")
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                when(option) {
                                                    "Normal" -> if (isArabic) "عادية" else "Normal"
                                                    "High" -> if (isArabic) "عالية" else "High"
                                                    else -> if (isArabic) "عالية جدآ" else "Ultra"
                                                },
                                                color = Color.White, 
                                                fontWeight = FontWeight.Bold
                                            ) 
                                        },
                                        onClick = {
                                            scope.launch { settingsManager.setVideoQuality(option) }
                                            qualityExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 1.5: Background Search Keywords
            Text(
                text = if (isArabic) "كلمات البحث للخلفيات" else "Background Search Keywords",
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardBg,
                border = BorderStroke(1.dp, Color(0x15FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                var newKeyword by remember { mutableStateOf("") }
                var isGenerating by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isArabic) "سيتم استخدام هذه الكلمات تلقائياً للبحث عن مناظر طبيعية أو إسلامية أو سينمائية من Pexels أو Pixabay لاستخدامها كخلفية لآيات القرآن." else "These keywords are automatically used to fetch matching cinematic or islamic backgrounds for video backgrounds.",
                        color = TextMutedColor,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    // Tags display
                    if (backgroundKeywords.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            backgroundKeywords.forEach { keyword ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0x22FFFFFF),
                                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = keyword, color = Color.White, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF5350),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    val newSet = backgroundKeywords.toMutableSet()
                                                    newSet.remove(keyword)
                                                    scope.launch { settingsManager.setBackgroundKeywords(newSet) }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add input row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newKeyword,
                            onValueChange = { newKeyword = it },
                            placeholder = { Text(if (isArabic) "كلمة جديدة..." else "New keyword...", color = Color(0x61FFFFFF), fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedContainerColor = Color(0x0FFFFFFF),
                                unfocusedContainerColor = Color(0x05FFFFFF)
                            ),
                            modifier = Modifier.weight(1f).height(50.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newKeyword.isNotBlank()) {
                                    val newSet = backgroundKeywords.toMutableSet()
                                    newKeyword.split(",").forEach {
                                        val trimmed = it.trim()
                                        if (trimmed.isNotBlank()) newSet.add(trimmed)
                                    }
                                    scope.launch { settingsManager.setBackgroundKeywords(newSet) }
                                    newKeyword = ""
                                }
                            },
                            modifier = Modifier.background(LuxuryGold, RoundedCornerShape(12.dp)).size(50.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                        }
                    }

                    // Bottom Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { scope.launch { settingsManager.setBackgroundKeywords(emptySet()) } },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isArabic) "حذف الكل" else "Clear All")
                        }

                        Button(
                            enabled = !isGenerating,
                            onClick = {
                                if (isGenerating) return@Button
                                val currentGeminiKey = if (geminiKey.isNotBlank()) geminiKey else com.example.BuildConfig.GEMINI_API_KEY
                                if (currentGeminiKey.isBlank() || currentGeminiKey == "MY_GEMINI_API_KEY") return@Button
                                isGenerating = true
                                showInlineDiagnostics = true
                                com.example.generator.SystemDiagnosticTracker.addLog("AUTOFILL", "بدء عملية الملئ التلقائي للكلمات المرجعية...")
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val client = OkHttpClient.Builder()
                                            // Bypass WAF error
                                            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                                            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                                            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                                            .addInterceptor { chain ->
                                                val original = chain.request()
                                                val requestBuilder = original.newBuilder()
                                                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                                    .header("x-goog-api-key", currentGeminiKey.trim())
                                                    .method(original.method, original.body)
                                                chain.proceed(requestBuilder.build())
                                            }
                                            .build()
                                        val url = "https://generativelanguage.googleapis.com/v1beta/models/${geminiModel.trim()}:generateContent?key=${currentGeminiKey.trim()}"
                                        com.example.generator.SystemDiagnosticTracker.addLog("AUTOFILL", "الرابط المطلوب: $url")
                                        val jsonReq = JSONObject().apply {
                                            put("contents", org.json.JSONArray().apply {
                                                put(JSONObject().apply {
                                                    put("parts", org.json.JSONArray().apply {
                                                        put(JSONObject().apply {
                                                            put("text", "Generate 10 English keywords or short phrases optimized for Pexels/Pixabay to find aesthetic cinematic, nature, atmospheric, and Islamic-themed background videos (e.g. 'islamic aesthetics kaaba mecca', 'dark cinematic aesthetic landscape', 'stormy rain window', 'stars night sky'). Return ONLY a comma-separated list of strings.")
                                                        })
                                                    })
                                                })
                                            })
                                        }
                                        val request = Request.Builder()
                                            .url(url)
                                            .post(jsonReq.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                                            .build()
                                        var attempt = 0
                                        var success = false
                                        while (attempt < 3 && !success) {
                                            com.example.generator.SystemDiagnosticTracker.addLog("AUTOFILL", "بدء المحاولة رقم ${attempt + 1}")
                                            val response = client.newCall(request).execute()
                                            if (response.isSuccessful) {
                                                val body = response.body?.string() ?: ""
                                                val root = JSONObject(body)
                                                val textStr = root.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                                                val newSet = backgroundKeywords.toMutableSet()
                                                textStr.split(",").forEach {
                                                    val trimmed = it.trim().removeSurrounding("\"").removeSurrounding("'").removeSurrounding("\n")
                                                    if (trimmed.isNotBlank()) newSet.add(trimmed)
                                                }
                                                withContext(Dispatchers.Main) {
                                                    settingsManager.setBackgroundKeywords(newSet)
                                                }
                                                com.example.generator.SystemDiagnosticTracker.addLog("AUTOFILL", "نجاح! تم جلب الكلمات وتحديث القائمة. النص المسترجع: $textStr")
                                                success = true
                                            } else if (response.code == 429) {
                                                com.example.generator.SystemDiagnosticTracker.addLog("ERROR", "تم استنفاد الحد الأقصى (429 Too Many Requests). المحاولة $attempt...")
                                                attempt++
                                                if (attempt >= 3) {
                                                    withContext(Dispatchers.Main) {
                                                        android.widget.Toast.makeText(context, if (isArabic) "لقد استنفذت الحد المسموح (خطأ 429). أضف مفتاح API الخاص بك." else "Rate limit reached (429). Enter your own Gemini API Key.", android.widget.Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                                kotlinx.coroutines.delay(2000L * attempt)
                                            } else {
                                                val errorBody = response.body?.string() ?: ""
                                                com.example.generator.SystemDiagnosticTracker.addLog("ERROR", "فشل الاتصال: رمز الاستجابة ${response.code}، التفاصيل: $errorBody\nالرابط كان: $url")
                                                withContext(Dispatchers.Main) {
                                                    android.widget.Toast.makeText(context, if (isArabic) "فشل الاتصال: ${response.code}\n$errorBody" else "Connection failed: ${response.code}\n$errorBody", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                                break
                                            }
                                        }
                                    } catch (e: Exception) {
                                        com.example.generator.SystemDiagnosticTracker.addLog("ERROR", "استثناء غير متوقع: ${e.message}")
                                        e.printStackTrace()
                                    } finally {
                                        withContext(Dispatchers.Main) { isGenerating = false }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x334CAF50), contentColor = Color(0xFF81C784)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color(0xFF81C784), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(if (isArabic) "توليد بالذكاء الاصطناعي" else "AI Auto Fill")
                        }
                    }
                }
            }

            // Section 2: API Keys and Video Backdrop config
            Text(
                text = if (isArabic) "مصادر ومفاتيح الـ API" else "Integration & API Configuration",
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardBg,
                border = BorderStroke(1.dp, Color(0x15FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFFF176),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isArabic) 
                                    "قم بإدخال المفتاح أدناه لتفعيل ميزة تنزيل خلفيات سينمائية طبيعية ومتحركة تلقائياً وبشكل مجاني تماماً!" 
                                    else "Provide API keys below to download gorgeous cinematic background textures automatically and for free!",
                                color = Color(0xFFECEFF1),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Pexels Text Field & instructions
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xBCFFFFFF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pexels Key", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Text(
                                    text = if (isArabic) "احصل على مفتاح Pexels" else "Get free Pexels key",
                                    color = LuxuryGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { uriHandler.openUri("https://www.pexels.com/api/") }
                                        .padding(4.dp)
                                )
                            }
                            OutlinedTextField(
                                value = pexelsKey,
                                onValueChange = { scope.launch { settingsManager.savePexelsKey(it) } },
                                placeholder = { Text(if (isArabic) "أدخل مفتاح Pexels هنا..." else "Paste your Pexels token...", color = Color(0x61FFFFFF)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedContainerColor = Color(0x0FFFFFFF),
                                    unfocusedContainerColor = Color(0x05FFFFFF)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Pixabay Text Field & instructions
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xBCFFFFFF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pixabay Key", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Text(
                                    text = if (isArabic) "احصل على مفتاح Pixabay" else "Get free Pixabay key",
                                    color = LuxuryGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { uriHandler.openUri("https://pixabay.com/api/docs/") }
                                        .padding(4.dp)
                                )
                            }
                            OutlinedTextField(
                                value = pixabayKey,
                                onValueChange = { scope.launch { settingsManager.savePixabayKey(it) } },
                                placeholder = { Text(if (isArabic) "أدخل مفتاح Pixabay هنا..." else "Paste your Pixabay token...", color = Color(0x61FFFFFF)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedContainerColor = Color(0x0FFFFFFF),
                                    unfocusedContainerColor = Color(0x05FFFFFF)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Gemini Text Field & instructions
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xBCFFFFFF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gemini AI Key", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Text(
                                    text = if (isArabic) "احصل على مفتاح Gemini مجاناً" else "Get free Gemini key",
                                    color = LuxuryGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { uriHandler.openUri("https://aistudio.google.com/") }
                                        .padding(4.dp)
                                )
                            }
                            Text(
                                text = if (isArabic) "مطلوب لتوليد العناوين والوصف والهاشتاجات الذكية لكل منصة بشكل احترافي" else "Required to generate smart platform-specific titles, descriptions, and tags automatically",
                                color = TextMutedColor,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            OutlinedTextField(
                                value = geminiKey,
                                onValueChange = { scope.launch { settingsManager.saveGeminiKey(it) } },
                                placeholder = { Text(if (isArabic) "أدخل مفتاح Gemini هنا لروبوت الذكاء الاصطناعي..." else "Paste your Gemini API key...", color = Color(0x61FFFFFF)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedContainerColor = Color(0x0FFFFFFF),
                                    unfocusedContainerColor = Color(0x05FFFFFF)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isArabic) "نموذج الذكاء الاصطناعي (Gemini Model)" else "Gemini AI Model",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            val models = listOf(
                                "gemini-3.5-flash",
                                "gemini-3.1-pro-preview",
                                "gemini-3.1-flash-lite-preview",
                                "gemini-2.5-flash"
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                models.forEach { model ->
                                    FilterChip(
                                        selected = geminiModel == model,
                                        onClick = { scope.launch { settingsManager.saveGeminiModel(model) } },
                                        label = { Text(model, color = if (geminiModel == model) Color.Black else Color.White) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LuxuryGold,
                                            containerColor = Color(0x33FFFFFF)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Inline Diagnostic Tracker
                if (showInlineDiagnostics && logsList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black,
                        border = BorderStroke(1.dp, Color(0x334CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (isArabic) "الفحص التشخيصي الشامل (مباشر)" else "Live System Diagnostics",
                                        color = Color(0xFF81C784),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = {
                                    val logText = logsList.joinToString("\n")
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(logText))
                                    android.widget.Toast.makeText(context, if (isArabic) "تم نسخ السجلات" else "Logs copied", android.widget.Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Logs",
                                        tint = Color(0xFF81C784),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            logsList.forEach { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("ERROR")) Color(0xFFEF5350) else Color(0xFFA5D6A7),
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Autosaver status block
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0x73FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "يتم حفظ التعديلات تلقائياً" else "All settings autosave instantly",
                        color = Color(0x73FFFFFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
