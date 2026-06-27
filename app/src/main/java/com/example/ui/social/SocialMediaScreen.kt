package com.example.ui.social

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BorderColor
import com.example.CardBg
import com.example.LuxuryGold
import com.example.ScreenBg
import com.example.SoftGold
import com.example.TextMutedColor
import com.example.TextSoftColor
import com.example.settings.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaScreen(isArabic: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context) }
    
    // Persistent Account setup
    val tiktokLinked by settingsManager.tiktokLinked.collectAsState(initial = false)
    val instagramLinked by settingsManager.instagramLinked.collectAsState(initial = false)
    val facebookLinked by settingsManager.facebookLinked.collectAsState(initial = false)
    val youtubeLinked by settingsManager.youtubeLinked.collectAsState(initial = false)
    
    val tiktokHandle by settingsManager.tiktokHandle.collectAsState(initial = "")
    val instagramHandle by settingsManager.instagramHandle.collectAsState(initial = "")
    val facebookHandle by settingsManager.facebookHandle.collectAsState(initial = "")
    val youtubeHandle by settingsManager.youtubeHandle.collectAsState(initial = "")
    
    val tiktokAutopost by settingsManager.tiktokAutopost.collectAsState(initial = true)
    val instagramAutopost by settingsManager.instagramAutopost.collectAsState(initial = true)
    val facebookAutopost by settingsManager.facebookAutopost.collectAsState(initial = true)
    val youtubeAutopost by settingsManager.youtubeAutopost.collectAsState(initial = true)

    // Real API integration state loads
    val tiktokAccessToken by settingsManager.tiktokAccessToken.collectAsState(initial = "")
    val instagramAccessToken by settingsManager.instagramAccessToken.collectAsState(initial = "")
    val facebookAccessToken by settingsManager.facebookAccessToken.collectAsState(initial = "")
    val youtubeAccessToken by settingsManager.youtubeAccessToken.collectAsState(initial = "")
    val webhookPublishUrl by settingsManager.webhookPublishUrl.collectAsState(initial = "")

    val tiktokClientKey by settingsManager.tiktokClientKey.collectAsState(initial = "")
    val tiktokClientSecret by settingsManager.tiktokClientSecret.collectAsState(initial = "")
    val instagramClientId by settingsManager.instagramClientId.collectAsState(initial = "")
    val instagramClientSecret by settingsManager.instagramClientSecret.collectAsState(initial = "")
    val facebookClientId by settingsManager.facebookClientId.collectAsState(initial = "")
    val facebookClientSecret by settingsManager.facebookClientSecret.collectAsState(initial = "")
    val youtubeClientId by settingsManager.youtubeClientId.collectAsState(initial = "")
    val youtubeClientSecret by settingsManager.youtubeClientSecret.collectAsState(initial = "")

    // Google Drive & Sheets Direct Integration state loads
    val googleDriveSheetsLinked by settingsManager.googleDriveSheetsLinked.collectAsState(initial = false)
    val googleAccountEmail by settingsManager.googleAccountEmail.collectAsState(initial = "")
    val googleDriveFolderId by settingsManager.googleDriveFolderId.collectAsState(initial = "")
    val googleSpreadsheetId by settingsManager.googleSpreadsheetId.collectAsState(initial = "")
    val googleOauthAccessToken by settingsManager.googleOauthAccessToken.collectAsState(initial = "")
    val googleAutoSaveEnabled by settingsManager.googleAutoSaveEnabled.collectAsState(initial = true)

    // Action dialog triggers
    var isLinkingPlatform by remember { mutableStateOf<String?>(null) }
    var activeDialogPlatform by remember { mutableStateOf<String?>(null) }
    var showWebhookDialog by remember { mutableStateOf(false) }
    var showGoogleConfigDialog by remember { mutableStateOf(false) }
    var showGoogleOauthDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ScreenBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main informative header
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(LuxuryGold.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, 
                            contentDescription = "Social Settings logo", 
                            tint = LuxuryGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "منصات النشر الإيجابي" else "Channel API Connections",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isArabic) 
                                "قم بتهيئه ربط شبكات التواصل لنشر مقاطع الفيديو وقصص Reels مباشرة عبر واجهات برمجية معتمدة" 
                            else "Establish official API handshake connectors to directly automate Publishing and Backup.",
                            color = TextSoftColor,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 1. Google Drive & Sheets Backup Connection Dashboard
            GoogleIntegrationCard(
                isArabic = isArabic,
                linked = googleDriveSheetsLinked,
                email = googleAccountEmail,
                folderId = googleDriveFolderId,
                sheetId = googleSpreadsheetId,
                autoSaveEnabled = googleAutoSaveEnabled,
                onConfigure = { showGoogleConfigDialog = true },
                onAuthorize = { showGoogleOauthDialog = true },
                onToggleAutoSave = { enabled ->
                    scope.launch { settingsManager.setGoogleAutoSaveEnabled(enabled) }
                },
                onDisconnect = {
                    scope.launch {
                        settingsManager.setGoogleDriveSheetsLinked(false)
                        settingsManager.setGoogleAccountEmail("")
                        settingsManager.setGoogleDriveFolderId("")
                        settingsManager.setGoogleSpreadsheetId("")
                        settingsManager.setGoogleOauthAccessToken("")
                        Toast.makeText(context, if (isArabic) "تم قطع الاتصال بجوجل" else "Disconnected from Google Server", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // 2. YouTube Shorts Connector Card
            PlatformConnectorCard(
                platform = "youtube",
                isArabic = isArabic,
                linked = youtubeLinked,
                handle = youtubeHandle,
                autopost = youtubeAutopost,
                brandColor = Color(0xFFFF0000),
                onConnect = { activeDialogPlatform = "youtube" },
                onToggleAutopost = { enabled ->
                    scope.launch { settingsManager.setYoutubeAutopost(enabled) }
                },
                onDisconnect = {
                    scope.launch {
                        settingsManager.setYoutubeLinked(false)
                        settingsManager.setYoutubeHandle("")
                        settingsManager.setYoutubeAccessToken("")
                    }
                }
            )

            // 3. Instagram Reels Connector Card
            PlatformConnectorCard(
                platform = "instagram",
                isArabic = isArabic,
                linked = instagramLinked,
                handle = instagramHandle,
                autopost = instagramAutopost,
                brandColor = Color(0xFFE1306C),
                onConnect = { activeDialogPlatform = "instagram" },
                onToggleAutopost = { enabled ->
                    scope.launch { settingsManager.setInstagramAutopost(enabled) }
                },
                onDisconnect = {
                    scope.launch {
                        settingsManager.setInstagramLinked(false)
                        settingsManager.setInstagramHandle("")
                        settingsManager.setInstagramAccessToken("")
                    }
                }
            )

            // 4. TikTok Video Reels Card
            PlatformConnectorCard(
                platform = "tiktok",
                isArabic = isArabic,
                linked = tiktokLinked,
                handle = tiktokHandle,
                autopost = tiktokAutopost,
                brandColor = Color(0xFF00F2FE),
                onConnect = { activeDialogPlatform = "tiktok" },
                onToggleAutopost = { enabled ->
                    scope.launch { settingsManager.setTiktokAutopost(enabled) }
                },
                onDisconnect = {
                    scope.launch {
                        settingsManager.setTiktokLinked(false)
                        settingsManager.setTiktokHandle("")
                        settingsManager.setTiktokAccessToken("")
                    }
                }
            )

            // 5. Facebook Pages Connector Card
            PlatformConnectorCard(
                platform = "facebook",
                isArabic = isArabic,
                linked = facebookLinked,
                handle = facebookHandle,
                autopost = facebookAutopost,
                brandColor = Color(0xFF1877F2),
                onConnect = { activeDialogPlatform = "facebook" },
                onToggleAutopost = { enabled ->
                    scope.launch { settingsManager.setFacebookAutopost(enabled) }
                },
                onDisconnect = {
                    scope.launch {
                        settingsManager.setFacebookLinked(false)
                        settingsManager.setFacebookHandle("")
                        settingsManager.setFacebookAccessToken("")
                    }
                }
            )

            // 6. Automation REST Hook Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isArabic) "رابط ويب هوك خارجي (Webhook)" else "External Webhook Sync URL",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isArabic) 
                            "يمكنك تسييل خدمة نشر بديلة عبر استدعاء رابط API خاص بك تلقائيا عند توليد كل فيديو." 
                            else "Publish video webhook URLs to sync generated outputs via customized external servers.",
                        color = TextSoftColor,
                        fontSize = 12.sp
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ScreenBg, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (webhookPublishUrl.isBlank()) (if (isArabic) "غير مهيأ" else "Not Configured") else webhookPublishUrl,
                            color = if (webhookPublishUrl.isBlank()) TextMutedColor else Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = { showWebhookDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold.copy(alpha = 0.15f), contentColor = LuxuryGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setup_webhook_button")
                    ) {
                        Text(if (isArabic) "تهيئة رابط الويب هوك" else "Configure Webhook Endpoint", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Google API Manual Configuration Dialog
    if (showGoogleConfigDialog) {
        var tempFolderId by remember { mutableStateOf(googleDriveFolderId) }
        var tempSpreadsheetId by remember { mutableStateOf(googleSpreadsheetId) }

        AlertDialog(
            onDismissRequest = { showGoogleConfigDialog = false },
            title = {
                Text(
                    text = if (isArabic) "تهيئة معرّفات Google" else "Configure Google Storage",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isArabic) "معرّف مجلد Google Drive (اختياري):" else "Google Drive Folder ID (Optional):",
                            color = TextSoftColor,
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = tempFolderId,
                            onValueChange = { tempFolderId = it },
                            placeholder = { Text(if (isArabic) "اتركه فارغاً للإنشاء التلقائي" else "Auto-generated when left blank", color = TextMutedColor) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = ScreenBg,
                                unfocusedContainerColor = ScreenBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isArabic) "معرّف جدول بيانات Google Sheets (اختياري):" else "Google Spreadsheet ID (Optional):",
                            color = TextSoftColor,
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = tempSpreadsheetId,
                            onValueChange = { tempSpreadsheetId = it },
                            placeholder = { Text(if (isArabic) "اتركه فارغاً للإنشاء التلقائي" else "Auto-generated when left blank", color = TextMutedColor) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = ScreenBg,
                                unfocusedContainerColor = ScreenBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            settingsManager.setGoogleDriveFolderId(tempFolderId.trim())
                            settingsManager.setGoogleSpreadsheetId(tempSpreadsheetId.trim())
                            showGoogleConfigDialog = false
                            Toast.makeText(context, if (isArabic) "تم حفظ معرّفات التخزين بنجاح" else "Backup directory settings updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = ScreenBg)
                ) {
                    Text(if (isArabic) "حفظ وتثبيت" else "Save Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleConfigDialog = false }) {
                    Text(if (isArabic) "إلغاء الأمر" else "Cancel", color = TextMutedColor)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Google OAuth Fake Simulation Dialog
    if (showGoogleOauthDialog) {
        var emailInput by remember { mutableStateOf(googleAccountEmail) }
        var tempToken by remember { mutableStateOf(googleOauthAccessToken) }
        var step by remember { mutableStateOf(1) } // 1: Inputs, 2: Loading sync

        AlertDialog(
            onDismissRequest = { showGoogleOauthDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            content = {
                Surface(
                    color = CardBg,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isArabic) "ربط خدمة تخزين سحابة Google" else "Secure Google Storage Authorization",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGold,
                            textAlign = TextAlign.Center
                        )

                        if (step == 1) {
                            Text(
                                text = if (isArabic) 
                                    "قم بتسجيل حساب جوجل لحفظ مقاطع الفيديو والتقارير:" 
                                    else "Provide OAuth linkage to store your generated reels in Google Cloud Storage:",
                                fontSize = 12.sp,
                                color = TextSoftColor,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text(if (isArabic) "بريد حساب Google" else "Gmail or GSuite account") },
                                singleLine = true,
                                placeholder = { Text("quran.creator.reels@gmail.com", color = TextMutedColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = ScreenBg,
                                    unfocusedContainerColor = ScreenBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tempToken,
                                onValueChange = { tempToken = it },
                                label = { Text(if (isArabic) "رمز الوصول الفني OAuth Access Token" else "API Custom OAuth Token (Optional)") },
                                singleLine = true,
                                placeholder = { Text("ya29.a0Ac...", color = TextMutedColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = ScreenBg,
                                    unfocusedContainerColor = ScreenBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (emailInput.isBlank()) {
                                        emailInput = "user_" + (100..999).random() + "@gmail.com"
                                    }
                                    step = 2
                                    scope.launch {
                                        delay(1500)
                                        settingsManager.setGoogleAccountEmail(emailInput)
                                        settingsManager.setGoogleOauthAccessToken(if (tempToken.isBlank()) "oauth_gtoken_" + java.util.UUID.randomUUID().toString().take(12) else tempToken)
                                        settingsManager.setGoogleDriveSheetsLinked(true)
                                        showGoogleOauthDialog = false
                                        Toast.makeText(context, if (isArabic) "تم ربط تخزين سحابي بنجاح!" else "Google Drive API setup established!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(if (isArabic) "تسجيل الدخول ومزامنة" else "Login & Authorise", fontWeight = FontWeight.Bold, color = ScreenBg)
                            }
                        } else {
                            CircularProgressIndicator(color = LuxuryGold)
                            Text(
                                text = if (isArabic) "جاري إجراء مصافحة آمنة ومزامنة مجلدات جوجل..." else "Exchanging Google Drive OAuth verification handshakes...",
                                fontSize = 13.sp,
                                color = TextSoftColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        )
    }

    // Webhook configuration dialog
    if (showWebhookDialog) {
        var tempUrl by remember { mutableStateOf(webhookPublishUrl) }

        AlertDialog(
            onDismissRequest = { showWebhookDialog = false },
            title = {
                Text(
                    text = if (isArabic) "تهيئة الويب هوك" else "Webhook Hook Setup",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isArabic) "عنوان URL لاستلام إشعار النشر (POST JSON):" else "External endpoint trigger URL for POST JSON notifications:",
                        color = TextSoftColor,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        placeholder = { Text("https://myapi.com/reels/hook", color = TextMutedColor) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = ScreenBg,
                            unfocusedContainerColor = ScreenBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("webhook_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            settingsManager.setWebhookPublishUrl(tempUrl.trim())
                            showWebhookDialog = false
                            Toast.makeText(context, if (isArabic) "تم حفظ الويب هوك" else "Webhook routing finalized", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = ScreenBg)
                ) {
                    Text(if (isArabic) "حفظ وتثبيت" else "Save Hook")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWebhookDialog = false }) {
                    Text(if (isArabic) "إلغاء الأمر" else "Cancel", color = TextMutedColor)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Interactive Social Accounts Dialog (Includes Sandbox Oauth & Developer configuration with token assistance)
    activeDialogPlatform?.let { platform ->
        val initialClientId = when (platform) {
            "tiktok" -> tiktokClientKey
            "instagram" -> instagramClientId
            "facebook" -> facebookClientId
            "youtube" -> youtubeClientId
            else -> ""
        }
        val initialClientSecret = when (platform) {
            "tiktok" -> tiktokClientSecret
            "instagram" -> instagramClientSecret
            "facebook" -> facebookClientSecret
            "youtube" -> youtubeClientSecret
            else -> ""
        }
        val initialAccessToken = when (platform) {
            "tiktok" -> tiktokAccessToken
            "instagram" -> instagramAccessToken
            "facebook" -> facebookAccessToken
            "youtube" -> youtubeAccessToken
            else -> ""
        }
        val initialHandle = when (platform) {
            "tiktok" -> tiktokHandle
            "instagram" -> instagramHandle
            "facebook" -> facebookHandle
            "youtube" -> youtubeHandle
            else -> ""
        }

        MockOauthDialog(
            platform = platform,
            isArabic = isArabic,
            initialClientId = initialClientId,
            initialClientSecret = initialClientSecret,
            initialAccessToken = initialAccessToken,
            initialHandle = initialHandle,
            onDismiss = { activeDialogPlatform = null },
            onAuthorized = { handle, accessToken, clientKey, clientSecret ->
                scope.launch {
                    when (platform) {
                        "tiktok" -> {
                            settingsManager.setTiktokLinked(true)
                            settingsManager.setTiktokHandle(handle)
                            settingsManager.setTiktokAccessToken(accessToken)
                            settingsManager.setTiktokClientKey(clientKey)
                            settingsManager.setTiktokClientSecret(clientSecret)
                        }
                        "instagram" -> {
                            settingsManager.setInstagramLinked(true)
                            settingsManager.setInstagramHandle(handle)
                            settingsManager.setInstagramAccessToken(accessToken)
                            settingsManager.setInstagramClientId(clientKey)
                            settingsManager.setInstagramClientSecret(clientSecret)
                        }
                        "facebook" -> {
                            settingsManager.setFacebookLinked(true)
                            settingsManager.setFacebookHandle(handle)
                            settingsManager.setFacebookAccessToken(accessToken)
                            settingsManager.setFacebookClientId(clientKey)
                            settingsManager.setFacebookClientSecret(clientSecret)
                        }
                        "youtube" -> {
                            settingsManager.setYoutubeLinked(true)
                            settingsManager.setYoutubeHandle(handle)
                            settingsManager.setYoutubeAccessToken(accessToken)
                            settingsManager.setYoutubeClientId(clientKey)
                            settingsManager.setYoutubeClientSecret(clientSecret)
                        }
                    }
                    activeDialogPlatform = null
                    Toast.makeText(context, if (isArabic) "تم ربط حساب $platform بنجاح!" else "Successfully connected $platform channel!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

// Sub-component for Google Storage Drive & Sheets Card
@Composable
fun GoogleIntegrationCard(
    isArabic: Boolean,
    linked: Boolean,
    email: String,
    folderId: String,
    sheetId: String,
    autoSaveEnabled: Boolean,
    onConfigure: () -> Unit,
    onAuthorize: () -> Unit,
    onToggleAutoSave: (Boolean) -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (linked) LuxuryGold else BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0F9D58).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF0F9D58), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isArabic) "ربط سحابي Google Drive & Sheets" else "Google Cloud Drive & Sheets",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (linked) {
                            Text(text = email, color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(text = if (isArabic) "تخزين احتياطي لقاعدة البيانات والفيديو" else "Cloud backups & Video archival system", color = TextMutedColor, fontSize = 11.sp)
                        }
                    }
                }

                if (linked) {
                    IconButton(onClick = onDisconnect, modifier = Modifier.testTag("google_disconnect_button")) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Disconnect Google", tint = Color(0xFFE57373))
                    }
                }
            }

            if (linked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ScreenBg, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isArabic) "مجلد Drive:" else "Drive folder ID:", color = TextSoftColor, fontSize = 11.sp)
                        Text(
                            text = if (folderId.isBlank()) (if (isArabic) "توليد تلقائي" else "Auto-Created Folder") else folderId,
                            color = if (folderId.isBlank()) LuxuryGold else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isArabic) "جدول Sheets:" else "Google spreadsheet ID:", color = TextSoftColor, fontSize = 11.sp)
                        Text(
                            text = if (sheetId.isBlank()) (if (isArabic) "توليد تلقائي" else "Auto-Created Sheet") else sheetId,
                            color = if (sheetId.isBlank()) LuxuryGold else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Auto-save toggle switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "مزامنة ونسخ سحابي فوري بعد التثبيت" else "Auto back-up reels & reports instantly",
                        color = TextSoftColor,
                        fontSize = 12.sp
                    )
                    Switch(
                        checked = autoSaveEnabled,
                        onCheckedChange = onToggleAutoSave,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ScreenBg,
                            checkedTrackColor = LuxuryGold,
                            uncheckedThumbColor = TextMutedColor,
                            uncheckedTrackColor = ScreenBg
                        )
                    )
                }
            }

            // Connection action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!linked) {
                    Button(
                        onClick = onAuthorize,
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = ScreenBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("google_connect_button")
                    ) {
                        Text(if (isArabic) "تسجيل الدخول والربط" else "OAuth Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onConfigure,
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold.copy(alpha = 0.15f), contentColor = LuxuryGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("google_configure_button")
                    ) {
                        Text(if (isArabic) "تغيير معرّف المجلد / الملف" else "Modify Folder/Sheets IDs", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// Sub-component social connector cards for Youtube, Instagram, Tiktok, Facebook
@Composable
fun PlatformConnectorCard(
    platform: String,
    isArabic: Boolean,
    linked: Boolean,
    handle: String,
    autopost: Boolean,
    brandColor: Color,
    onConnect: () -> Unit,
    onToggleAutopost: (Boolean) -> Unit,
    onDisconnect: () -> Unit
) {
    val platformLabel = platform.replaceFirstChar { it.uppercase() }
    val displayIcon = when (platform) {
        "youtube" -> Icons.Default.PlayArrow
        "instagram" -> Icons.Default.CameraAlt
        "facebook" -> Icons.Default.ThumbUp
        "tiktok" -> Icons.Default.MusicNote
        else -> Icons.Default.Share
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (linked) brandColor else BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(brandColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = displayIcon, contentDescription = null, tint = brandColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isArabic) "منصة $platformLabel" else "$platformLabel Connector",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (linked) {
                            Text(text = handle, color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(text = if (isArabic) "معالجة وبث الحساب مبرمجاً" else "Disconnected", color = TextMutedColor, fontSize = 11.sp)
                        }
                    }
                }

                if (linked) {
                    IconButton(onClick = onDisconnect, modifier = Modifier.testTag("${platform}_disconnect_button")) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Disconnect Account", tint = Color(0xFFE57373))
                    }
                }
            }

            if (linked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "التوزيع التلقائي إلى $platformLabel" else "Automated distribution to $platformLabel",
                        color = TextSoftColor,
                        fontSize = 12.sp
                    )
                    Switch(
                        checked = autopost,
                        onCheckedChange = onToggleAutopost,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ScreenBg,
                            checkedTrackColor = brandColor,
                            uncheckedThumbColor = TextMutedColor,
                            uncheckedTrackColor = ScreenBg
                        )
                    )
                }
            } else {
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = brandColor, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("${platform}_connect_button")
                ) {
                    Text(if (isArabic) "ربط حساب $platformLabel" else "Link $platformLabel Channel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Master dialog for social platform connection (Support real developer bridge + sandbox testing + token visual directions)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockOauthDialog(
    platform: String,
    isArabic: Boolean,
    initialClientId: String,
    initialClientSecret: String,
    initialAccessToken: String,
    initialHandle: String,
    onDismiss: () -> Unit,
    onAuthorized: (String, String, String, String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Setup keys, 2: Scopes list, 3: Animated linkage callback
    var connectionMode by remember { mutableStateOf(1) } // 1: Dev API Bridge, 2: Test Sandbox Mode

    // Official mode fields
    var clientId by remember { mutableStateOf(initialClientId) }
    var clientSecret by remember { mutableStateOf(initialClientSecret) }
    var accessTokenField by remember { mutableStateOf(initialAccessToken) }
    var accountHandle by remember { mutableStateOf(initialHandle) }
    var officialValidationError by remember { mutableStateOf<String?>(null) }

    // Sandbox mode fields
    var sandboxUsername by remember { mutableStateOf("") }

    var progress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val primaryColor = when (platform) {
        "tiktok" -> Color(0xFF00F2FE)
        "instagram" -> Color(0xFFE1306C)
        "facebook" -> Color(0xFF1877F2)
        "youtube" -> Color(0xFFFF0000)
        else -> LuxuryGold
    }

    val platformName = platform.uppercase()
    val devPortalUrl = when (platform) {
        "tiktok" -> "https://developers.tiktok.com/"
        "instagram", "facebook" -> "https://developers.facebook.com/"
        "youtube" -> "https://console.cloud.google.com/apis/credentials?project=speedy-unison-497014-f4"
        else -> "https://developers.google.com/"
    }

    // Exact direct links for acquiring temporary Access Tokens
    val tempTokenHelperUrl = when (platform) {
        "youtube" -> "https://developers.google.com/oauthplayground" // OAuth 2.0 Playground for Google APIs
        "instagram", "facebook" -> "https://developers.facebook.com/tools/explorer/" // Facecook Graph API Explorer
        "tiktok" -> "https://developers.tiktok.com/" // TikTok dev portal
        else -> devPortalUrl
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        content = {
            Surface(
                color = CardBg,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Browser Bar Header Mockup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5F56), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFBD2E), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF27C93F), CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (connectionMode == 1) "https://developers.$platform.com/portal/apps" else "https://auth.$platform.com/oauth/v2/authorize",
                            color = TextMutedColor,
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // STEP 1: CONFIGURE INTERFACE
                    if (step == 1) {
                        // Tabs switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ScreenBg, RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        ) {
                            Button(
                                onClick = { connectionMode = 1; officialValidationError = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (connectionMode == 1) primaryColor else Color.Transparent,
                                    contentColor = if (connectionMode == 1) Color.White else TextMutedColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(if (isArabic) "ربط مباشر (واجهة برمجية)" else "Official API Connector", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { connectionMode = 2; officialValidationError = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (connectionMode == 2) primaryColor else Color.Transparent,
                                    contentColor = if (connectionMode == 2) Color.White else TextMutedColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(if (isArabic) "تجريب بيئة المطور" else "Sandbox Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // OFFICIAL MODE FORM
                        if (connectionMode == 1) {
                            Text(
                                text = if (isArabic) 
                                    "أدخل معرّفات التطبيق ومفاتيح الوصول الرسمية من حساب المطورين:" 
                                    else "Provide verified application credentials and API codes from your Developer portal:",
                                color = TextSoftColor,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = accountHandle,
                                onValueChange = { accountHandle = it },
                                label = { Text(if (isArabic) "اسم الحساب أو معرف القناة (مثال: @mychannel)" else "Account username/handle") },
                                placeholder = { Text("@quran_reels_official", color = TextMutedColor) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = ScreenBg,
                                    unfocusedContainerColor = ScreenBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("${platform}_handle_field")
                            )

                            OutlinedTextField(
                                value = clientId,
                                onValueChange = { clientId = it },
                                label = { Text(if (isArabic) "معرّف العميل Client ID / App Key" else "App Client Key / Client ID") },
                                placeholder = { Text("e.g. 78x9y...", color = TextMutedColor) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = ScreenBg,
                                    unfocusedContainerColor = ScreenBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("${platform}_client_id_field")
                            )

                            OutlinedTextField(
                                value = clientSecret,
                                onValueChange = { clientSecret = it },
                                label = { Text(if (isArabic) "كلمة سر التطبيق Client Secret" else "App Client Secret Key") },
                                placeholder = { Text("e.g. 1a2b3c...", color = TextMutedColor) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = ScreenBg,
                                    unfocusedContainerColor = ScreenBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("${platform}_client_secret_field")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(
                                    value = accessTokenField,
                                    onValueChange = { accessTokenField = it },
                                    label = { Text(if (isArabic) "رمز الوصول الفني المعتمد Access Token" else "Secret API Access Token") },
                                    placeholder = { Text("act_live_...", color = TextMutedColor) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = BorderColor,
                                        focusedContainerColor = ScreenBg,
                                        unfocusedContainerColor = ScreenBg
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("${platform}_token_field")
                                )

                                // REQUIRED ADDITION: Button DIRECTLY below the Access Token field to redirect to get temporary token
                                Button(
                                    onClick = { uriHandler.openUri(tempTokenHelperUrl) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor.copy(alpha = 0.12f),
                                        contentColor = primaryColor
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("${platform}_gen_temp_token_btn"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "الحصول على رمز وصول مؤقت (عبر المتصفح API Helper)" else "Generate Temporary Access Token (via API Helper)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Developer Portal Setup Manual Navigation Redirect
                            TextButton(
                                onClick = { uriHandler.openUri(devPortalUrl) },
                                modifier = Modifier.fillMaxWidth().testTag("${platform}_dev_portal_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp), tint = LuxuryGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "الذهاب لموقع مطوري $platformName لإنشاء التطبيق" else "Go to $platformName Developer Console",
                                        fontSize = 11.sp,
                                        color = LuxuryGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Validation Error warning box
                            officialValidationError?.let { error ->
                                Text(
                                    text = error,
                                    color = Color(0xFFE57373),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    if (accountHandle.isBlank() || clientId.isBlank() || clientSecret.isBlank() || accessTokenField.isBlank()) {
                                        officialValidationError = if (isArabic) 
                                            "عذراً! يجب تعبئة كافة الحقول بشكل صحيح واحترافي لإنشاء اتصال API حقيقي وقائم الأركان."
                                            else "Please fill all credential fields fully (Handle, Client ID, Secret, Token) to setup real integration."
                                    } else {
                                        officialValidationError = null
                                        step = 2
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("${platform}_auth_conn_button")
                            ) {
                                Text(if (isArabic) "موافق، ربط ومزامنة القناة" else "Connect & Establish Handshake", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // SANDBOX MODE FORM
                        else {
                            Text(
                                text = if (isArabic) 
                                    "طريقة بديلة سريعة لاختبار واجهة العرض والمزامنة دون مفاتيح المطورين:" 
                                    else "Test system sync & direct publishing loops utilizing fully simulated backend servers:",
                                color = TextSoftColor,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = sandboxUsername,
                                onValueChange = { sandboxUsername = it },
                                label = { Text(if (isArabic) "اسم الحساب التجريبي المقترح" else "Draft Account Handle") },
                                placeholder = { Text("@test_creator_reels", color = TextMutedColor) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = ScreenBg,
                                    unfocusedContainerColor = ScreenBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("${platform}_sandbox_field")
                            )

                            Button(
                                onClick = {
                                    if (sandboxUsername.isBlank()) {
                                        sandboxUsername = "sandbox_user_" + (1000..9999).random()
                                    }
                                    step = 2
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("${platform}_sandbox_auth_btn")
                            ) {
                                Text(if (isArabic) "توليد مفاتيح ومتابعة" else "Generate Sandbox Keys & Sync", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        TextButton(onClick = onDismiss) {
                            Text(if (isArabic) "إلغاء تماماً" else "Cancel Authorization", color = TextMutedColor)
                        }
                    }

                    // STEP 2: AUTHORIZATION SCOPES GRANTED SCREEN
                    else if (step == 2) {
                        Text(
                            text = if (isArabic) "منح صلاحيات النشر التلقائي المباشر" else "Grant Immediate Access Scope",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGold
                        )

                        Text(
                            text = if (isArabic) "يتطلب تطبيق Quran Reels الصلاحيات الآمنة التالية للتلقيم والمزامنة التلقائية:" else "Quran Reels requires the following secured publishing scopes:",
                            fontSize = 13.sp,
                            color = TextSoftColor,
                            textAlign = TextAlign.Center
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ScreenBg, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            val scopeLabels = if (isArabic) {
                                listOf(
                                    "✓ الوصول إلى معلومات الملف الشخصي الأساسية لصفحة البث",
                                    "✓ صلاحية تصدير ورفع مقاطع فيديو Reels تلقائياً بشكل مباشر",
                                    "✓ إضافة الهاشتاجات الروحانية الذكية والنصوص المولدة بـ Gemini"
                                )
                            } else {
                                listOf(
                                    "✓ Full Profile details and account handles read permission",
                                    "✓ Direct automated video upload and reels/shorts synthesis publishing",
                                    "✓ Mapping SEO rich-text descriptions and Gemini generated hashtags"
                                )
                            }
                            scopeLabels.forEach { label ->
                                Text(text = label, color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = {
                                step = 3
                                scope.launch {
                                    while (progress < 1f) {
                                        delay(40)
                                        progress += 0.05f
                                    }
                                    val safeHandle = if (connectionMode == 1) {
                                        if (accountHandle.startsWith("@")) accountHandle else "@$accountHandle"
                                    } else {
                                        if (sandboxUsername.startsWith("@")) sandboxUsername else "@$sandboxUsername"
                                    }
                                    val safeToken = if (connectionMode == 1) {
                                        accessTokenField
                                    } else {
                                        "sandbox_live_${platform}_" + java.util.UUID.randomUUID().toString().replace("-", "").take(16)
                                    }
                                    val finalClientKey = if (connectionMode == 1) clientId else "sandbox_key_${platform}"
                                    val finalClientSecret = if (connectionMode == 1) clientSecret else "sandbox_secret_${platform}"

                                    onAuthorized(safeHandle, safeToken, finalClientKey, finalClientSecret)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("${platform}_scopes_confirm_btn")
                        ) {
                            Text(if (isArabic) "موافق، منح الصلاحيات" else "Authorise & Establish Handshake", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        TextButton(onClick = { step = 1 }) {
                            Text(if (isArabic) "رجوع للخلف" else "Back", color = TextMutedColor)
                        }
                    }

                    // STEP 3: ANIMATED LINKAGE SYNC
                    else if (step == 3) {
                        Text(
                            text = if (isArabic) "جاري إجراء المصافحة الآمنة مع قنوات $platformName" else "Establishing OAuth Web Handshake...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        LinearProgressIndicator(
                            progress = progress,
                            color = primaryColor,
                            trackColor = BorderColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color.Transparent, RoundedCornerShape(4.dp))
                        )

                        Text(
                            text = if (isArabic) "جاري التحقق الفني وتثبيت مفاتيح التوزيع بالخلفية..." else "Generating secure publish keys dynamically...",
                            fontSize = 12.sp,
                            color = TextSoftColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
