package com.example.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val PEXELS_API_KEY = stringPreferencesKey("pexels_api_key")
        val PIXABAY_API_KEY = stringPreferencesKey("pixabay_api_key")
        val THEME_DARK_MODE = booleanPreferencesKey("theme_dark_mode")
        val SHOW_TRANSLATION = booleanPreferencesKey("show_translation")
        val LANGUAGE = stringPreferencesKey("language") // "ar" or "en"

        // Font formatting preferences
        val FONT_FAMILY = stringPreferencesKey("font_family") // "Amiri", "Cairo", "Default", "Monospace"
        val FONT_SIZE = intPreferencesKey("font_size") // in px, e.g., 50
        val TEXT_COLOR = stringPreferencesKey("text_color") // hex color, e.g., "#FFD54F"
        val TEXT_OPACITY = floatPreferencesKey("text_opacity") // 0.0f - 1.0f
        
        val SHOW_TEXT_BACKGROUND = booleanPreferencesKey("show_text_background")
        val TEXT_BG_COLOR = stringPreferencesKey("text_bg_color")
        val TEXT_BG_OPACITY = floatPreferencesKey("text_bg_opacity")
        val TEXT_BG_RADIUS = intPreferencesKey("text_bg_radius")
        
        val TEXT_POSITION = stringPreferencesKey("text_position") // "Top", "Center", "Bottom"
        val TEXT_ALIGN = stringPreferencesKey("text_align") // "Center", "Left", "Right"
        
        val TRANSLATION_FONT_SIZE = intPreferencesKey("translation_font_size")
        val TRANSLATION_COLOR = stringPreferencesKey("translation_color")
        val TRANSLATION_FONT_FAMILY = stringPreferencesKey("translation_font_family")

        // Download Video Quality
        val VIDEO_QUALITY = stringPreferencesKey("video_quality") // "Normal", "High", "Ultra"

        // Gemini & Social accounts keys
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val TIKTOK_LINKED = booleanPreferencesKey("tiktok_linked")
        val INSTAGRAM_LINKED = booleanPreferencesKey("instagram_linked")
        val FACEBOOK_LINKED = booleanPreferencesKey("facebook_linked")
        val YOUTUBE_LINKED = booleanPreferencesKey("youtube_linked")
        val TIKTOK_HANDLE = stringPreferencesKey("tiktok_handle")
        val INSTAGRAM_HANDLE = stringPreferencesKey("instagram_handle")
        val FACEBOOK_HANDLE = stringPreferencesKey("facebook_handle")
        val YOUTUBE_HANDLE = stringPreferencesKey("youtube_handle")
        val TIKTOK_AUTOPOST = booleanPreferencesKey("tiktok_autopost")
        val INSTAGRAM_AUTOPOST = booleanPreferencesKey("instagram_autopost")
        val FACEBOOK_AUTOPOST = booleanPreferencesKey("facebook_autopost")
        val YOUTUBE_AUTOPOST = booleanPreferencesKey("youtube_autopost")

        // Real API integration tokens and automation webhooks
        val TIKTOK_ACCESS_TOKEN = stringPreferencesKey("tiktok_access_token")
        val INSTAGRAM_ACCESS_TOKEN = stringPreferencesKey("instagram_access_token")
        val FACEBOOK_ACCESS_TOKEN = stringPreferencesKey("facebook_access_token")
        val YOUTUBE_ACCESS_TOKEN = stringPreferencesKey("youtube_access_token")
        val WEBHOOK_PUBLISH_URL = stringPreferencesKey("webhook_publish_url")

        val TIKTOK_CLIENT_KEY = stringPreferencesKey("tiktok_client_key")
        val TIKTOK_CLIENT_SECRET = stringPreferencesKey("tiktok_client_secret")
        val INSTAGRAM_CLIENT_ID = stringPreferencesKey("instagram_client_id")
        val INSTAGRAM_CLIENT_SECRET = stringPreferencesKey("instagram_client_secret")
        val FACEBOOK_CLIENT_ID = stringPreferencesKey("facebook_client_id")
        val FACEBOOK_CLIENT_SECRET = stringPreferencesKey("facebook_client_secret")
        val YOUTUBE_CLIENT_ID = stringPreferencesKey("youtube_client_id")
        val YOUTUBE_CLIENT_SECRET = stringPreferencesKey("youtube_client_secret")

        // Google Drive & Sheets preferences keys
        val GOOGLE_DRIVE_SHEETS_LINKED = booleanPreferencesKey("google_drive_sheets_linked")
        val GOOGLE_ACCOUNT_EMAIL = stringPreferencesKey("google_account_email")
        val GOOGLE_DRIVE_FOLDER_ID = stringPreferencesKey("google_drive_folder_id")
        val GOOGLE_SPREADSHEET_ID = stringPreferencesKey("google_spreadsheet_id")
        val GOOGLE_OAUTH_ACCESS_TOKEN = stringPreferencesKey("google_oauth_access_token")
        val GOOGLE_AUTO_SAVE_ENABLED = booleanPreferencesKey("google_auto_save_enabled")

        // HomeScreen selections persistence
        val SELECTED_SURAH_IDX = intPreferencesKey("selected_surah_idx")
        val START_AYAH_TEXT = stringPreferencesKey("start_ayah_text")
        val END_AYAH_TEXT = stringPreferencesKey("end_ayah_text")
        val SELECTED_RECITER_ID = stringPreferencesKey("selected_reciter_id")
        val INCLUDE_BASMALAH = booleanPreferencesKey("include_basmalah")
        val ACTIVE_GENERATION_RECITER_ID = stringPreferencesKey("active_generation_reciter_id")
        val BACKGROUND_KEYWORDS = stringSetPreferencesKey("background_keywords")
        val CUSTOM_CURATED_CLIPS = stringPreferencesKey("custom_curated_clips")
    }

    val pexelsApiKey: Flow<String> = context.dataStore.data.map { it[PEXELS_API_KEY] ?: "" }
    val pixabayApiKey: Flow<String> = context.dataStore.data.map { it[PIXABAY_API_KEY] ?: "" }
    val themeMode: Flow<Boolean> = context.dataStore.data.map { it[THEME_DARK_MODE] ?: true } // default dark mode for cinematic feel
    val showTranslation: Flow<Boolean> = context.dataStore.data.map { it[SHOW_TRANSLATION] ?: true }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "ar" }
    val videoQuality: Flow<String> = context.dataStore.data.map { it[VIDEO_QUALITY] ?: "Ultra" }

    // Font formatting flows
    val fontFamily: Flow<String> = context.dataStore.data.map { it[FONT_FAMILY] ?: "Amiri" }
    val fontSize: Flow<Int> = context.dataStore.data.map { it[FONT_SIZE] ?: 50 }
    val textColor: Flow<String> = context.dataStore.data.map { it[TEXT_COLOR] ?: "#FFD54F" } // Default nice gold
    val textOpacity: Flow<Float> = context.dataStore.data.map { it[TEXT_OPACITY] ?: 1.0f }
    
    val showTextBackground: Flow<Boolean> = context.dataStore.data.map { it[SHOW_TEXT_BACKGROUND] ?: false }
    val textBgColor: Flow<String> = context.dataStore.data.map { it[TEXT_BG_COLOR] ?: "#000000" }
    val textBgOpacity: Flow<Float> = context.dataStore.data.map { it[TEXT_BG_OPACITY] ?: 0.6f }
    val textBgRadius: Flow<Int> = context.dataStore.data.map { it[TEXT_BG_RADIUS] ?: 16 }
    
    val textPosition: Flow<String> = context.dataStore.data.map { it[TEXT_POSITION] ?: "Center" }
    val textAlign: Flow<String> = context.dataStore.data.map { it[TEXT_ALIGN] ?: "Center" }
    
    val translationFontSize: Flow<Int> = context.dataStore.data.map { it[TRANSLATION_FONT_SIZE] ?: 25 }
    val translationColor: Flow<String> = context.dataStore.data.map { it[TRANSLATION_COLOR] ?: "#E0E0E0" }
    val translationFontFamily: Flow<String> = context.dataStore.data.map { it[TRANSLATION_FONT_FAMILY] ?: "Default" }

    // Gemini & Social accounts flows
    val geminiApiKey: Flow<String> = context.dataStore.data.map { it[GEMINI_API_KEY] ?: "" }
    val geminiModel: Flow<String> = context.dataStore.data.map { it[GEMINI_MODEL] ?: "gemini-1.5-flash" }
    val tiktokLinked: Flow<Boolean> = context.dataStore.data.map { it[TIKTOK_LINKED] ?: false }
    val instagramLinked: Flow<Boolean> = context.dataStore.data.map { it[INSTAGRAM_LINKED] ?: false }
    val facebookLinked: Flow<Boolean> = context.dataStore.data.map { it[FACEBOOK_LINKED] ?: false }
    val youtubeLinked: Flow<Boolean> = context.dataStore.data.map { it[YOUTUBE_LINKED] ?: false }
    val tiktokHandle: Flow<String> = context.dataStore.data.map { it[TIKTOK_HANDLE] ?: "" }
    val instagramHandle: Flow<String> = context.dataStore.data.map { it[INSTAGRAM_HANDLE] ?: "" }
    val facebookHandle: Flow<String> = context.dataStore.data.map { it[FACEBOOK_HANDLE] ?: "" }
    val youtubeHandle: Flow<String> = context.dataStore.data.map { it[YOUTUBE_HANDLE] ?: "" }
    val tiktokAutopost: Flow<Boolean> = context.dataStore.data.map { it[TIKTOK_AUTOPOST] ?: true }
    val instagramAutopost: Flow<Boolean> = context.dataStore.data.map { it[INSTAGRAM_AUTOPOST] ?: true }
    val facebookAutopost: Flow<Boolean> = context.dataStore.data.map { it[FACEBOOK_AUTOPOST] ?: true }
    val youtubeAutopost: Flow<Boolean> = context.dataStore.data.map { it[YOUTUBE_AUTOPOST] ?: true }

    // Real API integration flows
    val tiktokAccessToken: Flow<String> = context.dataStore.data.map { it[TIKTOK_ACCESS_TOKEN] ?: "" }
    val instagramAccessToken: Flow<String> = context.dataStore.data.map { it[INSTAGRAM_ACCESS_TOKEN] ?: "" }
    val facebookAccessToken: Flow<String> = context.dataStore.data.map { it[FACEBOOK_ACCESS_TOKEN] ?: "" }
    val youtubeAccessToken: Flow<String> = context.dataStore.data.map { it[YOUTUBE_ACCESS_TOKEN] ?: "" }
    val webhookPublishUrl: Flow<String> = context.dataStore.data.map { it[WEBHOOK_PUBLISH_URL] ?: "" }

    val tiktokClientKey: Flow<String> = context.dataStore.data.map { it[TIKTOK_CLIENT_KEY] ?: "" }
    val tiktokClientSecret: Flow<String> = context.dataStore.data.map { it[TIKTOK_CLIENT_SECRET] ?: "" }
    val instagramClientId: Flow<String> = context.dataStore.data.map { it[INSTAGRAM_CLIENT_ID] ?: "" }
    val instagramClientSecret: Flow<String> = context.dataStore.data.map { it[INSTAGRAM_CLIENT_SECRET] ?: "" }
    val facebookClientId: Flow<String> = context.dataStore.data.map { it[FACEBOOK_CLIENT_ID] ?: "" }
    val facebookClientSecret: Flow<String> = context.dataStore.data.map { it[FACEBOOK_CLIENT_SECRET] ?: "" }
    val youtubeClientId: Flow<String> = context.dataStore.data.map { it[YOUTUBE_CLIENT_ID] ?: "" }
    val youtubeClientSecret: Flow<String> = context.dataStore.data.map { it[YOUTUBE_CLIENT_SECRET] ?: "" }

    // Google Drive & Sheets flow accessors
    val googleDriveSheetsLinked: Flow<Boolean> = context.dataStore.data.map { it[GOOGLE_DRIVE_SHEETS_LINKED] ?: false }
    val googleAccountEmail: Flow<String> = context.dataStore.data.map { it[GOOGLE_ACCOUNT_EMAIL] ?: "" }
    val googleDriveFolderId: Flow<String> = context.dataStore.data.map { it[GOOGLE_DRIVE_FOLDER_ID] ?: "" }
    val googleSpreadsheetId: Flow<String> = context.dataStore.data.map { it[GOOGLE_SPREADSHEET_ID] ?: "" }
    val googleOauthAccessToken: Flow<String> = context.dataStore.data.map { it[GOOGLE_OAUTH_ACCESS_TOKEN] ?: "" }
    val googleAutoSaveEnabled: Flow<Boolean> = context.dataStore.data.map { it[GOOGLE_AUTO_SAVE_ENABLED] ?: true }

    // HomeScreen selections flows
    val selectedSurahIdx: Flow<Int> = context.dataStore.data.map { it[SELECTED_SURAH_IDX] ?: 0 }
    val startAyahText: Flow<String> = context.dataStore.data.map { it[START_AYAH_TEXT] ?: "1" }
    val endAyahText: Flow<String> = context.dataStore.data.map { it[END_AYAH_TEXT] ?: "" }
    val selectedReciterId: Flow<String> = context.dataStore.data.map { it[SELECTED_RECITER_ID] ?: "ar.alafasy" }
    val includeBasmalah: Flow<Boolean> = context.dataStore.data.map { it[INCLUDE_BASMALAH] ?: true }
    val activeGenerationReciterId: Flow<String> = context.dataStore.data.map { it[ACTIVE_GENERATION_RECITER_ID] ?: "ar.alafasy" }
    val backgroundKeywords: Flow<Set<String>> = context.dataStore.data.map { it[BACKGROUND_KEYWORDS] ?: emptySet() }

    suspend fun savePexelsKey(key: String) {
        context.dataStore.edit { it[PEXELS_API_KEY] = key }
    }

    suspend fun savePixabayKey(key: String) {
        context.dataStore.edit { it[PIXABAY_API_KEY] = key }
    }

    suspend fun setThemeMode(isDark: Boolean) {
        context.dataStore.edit { it[THEME_DARK_MODE] = isDark }
    }

    suspend fun setShowTranslation(show: Boolean) {
        context.dataStore.edit { it[SHOW_TRANSLATION] = show }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setVideoQuality(value: String) {
        context.dataStore.edit { it[VIDEO_QUALITY] = value }
    }

    // Font formatting setters
    suspend fun setFontFamily(value: String) {
        context.dataStore.edit { it[FONT_FAMILY] = value }
    }

    suspend fun setFontSize(value: Int) {
        context.dataStore.edit { it[FONT_SIZE] = value }
    }

    suspend fun setTextColor(value: String) {
        context.dataStore.edit { it[TEXT_COLOR] = value }
    }

    suspend fun setTextOpacity(value: Float) {
        context.dataStore.edit { it[TEXT_OPACITY] = value }
    }

    suspend fun setShowTextBackground(value: Boolean) {
        context.dataStore.edit { it[SHOW_TEXT_BACKGROUND] = value }
    }

    suspend fun setTextBgColor(value: String) {
        context.dataStore.edit { it[TEXT_BG_COLOR] = value }
    }

    suspend fun setTextBgOpacity(value: Float) {
        context.dataStore.edit { it[TEXT_BG_OPACITY] = value }
    }

    suspend fun setTextBgRadius(value: Int) {
        context.dataStore.edit { it[TEXT_BG_RADIUS] = value }
    }

    suspend fun setTextPosition(value: String) {
        context.dataStore.edit { it[TEXT_POSITION] = value }
    }

    suspend fun setTextAlign(value: String) {
        context.dataStore.edit { it[TEXT_ALIGN] = value }
    }

    suspend fun setTranslationFontSize(value: Int) {
        context.dataStore.edit { it[TRANSLATION_FONT_SIZE] = value }
    }

    suspend fun setTranslationColor(value: String) {
        context.dataStore.edit { it[TRANSLATION_COLOR] = value }
    }

    suspend fun setTranslationFontFamily(value: String) {
        context.dataStore.edit { it[TRANSLATION_FONT_FAMILY] = value }
    }

    // Gemini & Social accounts setters
    suspend fun saveGeminiKey(key: String) {
        context.dataStore.edit { it[GEMINI_API_KEY] = key }
    }

    suspend fun saveGeminiModel(model: String) {
        context.dataStore.edit { it[GEMINI_MODEL] = model }
    }

    suspend fun setTiktokLinked(value: Boolean) {
        context.dataStore.edit { it[TIKTOK_LINKED] = value }
    }

    suspend fun setInstagramLinked(value: Boolean) {
        context.dataStore.edit { it[INSTAGRAM_LINKED] = value }
    }

    suspend fun setFacebookLinked(value: Boolean) {
        context.dataStore.edit { it[FACEBOOK_LINKED] = value }
    }

    suspend fun setYoutubeLinked(value: Boolean) {
        context.dataStore.edit { it[YOUTUBE_LINKED] = value }
    }

    suspend fun setTiktokHandle(value: String) {
        context.dataStore.edit { it[TIKTOK_HANDLE] = value }
    }

    suspend fun setInstagramHandle(value: String) {
        context.dataStore.edit { it[INSTAGRAM_HANDLE] = value }
    }

    suspend fun setFacebookHandle(value: String) {
        context.dataStore.edit { it[FACEBOOK_HANDLE] = value }
    }

    suspend fun setYoutubeHandle(value: String) {
        context.dataStore.edit { it[YOUTUBE_HANDLE] = value }
    }

    suspend fun setTiktokAutopost(value: Boolean) {
        context.dataStore.edit { it[TIKTOK_AUTOPOST] = value }
    }

    suspend fun setInstagramAutopost(value: Boolean) {
        context.dataStore.edit { it[INSTAGRAM_AUTOPOST] = value }
    }

    suspend fun setFacebookAutopost(value: Boolean) {
        context.dataStore.edit { it[FACEBOOK_AUTOPOST] = value }
    }

    suspend fun setYoutubeAutopost(value: Boolean) {
        context.dataStore.edit { it[YOUTUBE_AUTOPOST] = value }
    }

    suspend fun setTiktokAccessToken(value: String) {
        context.dataStore.edit { it[TIKTOK_ACCESS_TOKEN] = value }
    }

    suspend fun setInstagramAccessToken(value: String) {
        context.dataStore.edit { it[INSTAGRAM_ACCESS_TOKEN] = value }
    }

    suspend fun setFacebookAccessToken(value: String) {
        context.dataStore.edit { it[FACEBOOK_ACCESS_TOKEN] = value }
    }

    suspend fun setYoutubeAccessToken(value: String) {
        context.dataStore.edit { it[YOUTUBE_ACCESS_TOKEN] = value }
    }

    suspend fun setWebhookPublishUrl(value: String) {
        context.dataStore.edit { it[WEBHOOK_PUBLISH_URL] = value }
    }

    suspend fun setTiktokClientKey(value: String) {
        context.dataStore.edit { it[TIKTOK_CLIENT_KEY] = value }
    }

    suspend fun setTiktokClientSecret(value: String) {
        context.dataStore.edit { it[TIKTOK_CLIENT_SECRET] = value }
    }

    suspend fun setInstagramClientId(value: String) {
        context.dataStore.edit { it[INSTAGRAM_CLIENT_ID] = value }
    }

    suspend fun setInstagramClientSecret(value: String) {
        context.dataStore.edit { it[INSTAGRAM_CLIENT_SECRET] = value }
    }

    suspend fun setFacebookClientId(value: String) {
        context.dataStore.edit { it[FACEBOOK_CLIENT_ID] = value }
    }

    suspend fun setFacebookClientSecret(value: String) {
        context.dataStore.edit { it[FACEBOOK_CLIENT_SECRET] = value }
    }

    suspend fun setYoutubeClientId(value: String) {
        context.dataStore.edit { it[YOUTUBE_CLIENT_ID] = value }
    }

    suspend fun setYoutubeClientSecret(value: String) {
        context.dataStore.edit { it[YOUTUBE_CLIENT_SECRET] = value }
    }

    // Google Drive & Sheets setters
    suspend fun setGoogleDriveSheetsLinked(value: Boolean) {
        context.dataStore.edit { it[GOOGLE_DRIVE_SHEETS_LINKED] = value }
    }

    suspend fun setGoogleAccountEmail(value: String) {
        context.dataStore.edit { it[GOOGLE_ACCOUNT_EMAIL] = value }
    }

    suspend fun setGoogleDriveFolderId(value: String) {
        context.dataStore.edit { it[GOOGLE_DRIVE_FOLDER_ID] = value }
    }

    suspend fun setGoogleSpreadsheetId(value: String) {
        context.dataStore.edit { it[GOOGLE_SPREADSHEET_ID] = value }
    }

    suspend fun setGoogleOauthAccessToken(value: String) {
        context.dataStore.edit { it[GOOGLE_OAUTH_ACCESS_TOKEN] = value }
    }

    suspend fun setGoogleAutoSaveEnabled(value: Boolean) {
        context.dataStore.edit { it[GOOGLE_AUTO_SAVE_ENABLED] = value }
    }

    // HomeScreen selections setters
    suspend fun setSelectedSurahIdx(value: Int) {
        context.dataStore.edit { it[SELECTED_SURAH_IDX] = value }
    }

    suspend fun setStartAyahText(value: String) {
        context.dataStore.edit { it[START_AYAH_TEXT] = value }
    }

    suspend fun setEndAyahText(value: String) {
        context.dataStore.edit { it[END_AYAH_TEXT] = value }
    }

    suspend fun setSelectedReciterId(value: String) {
        context.dataStore.edit { it[SELECTED_RECITER_ID] = value }
    }

    suspend fun setIncludeBasmalah(value: Boolean) {
        context.dataStore.edit { it[INCLUDE_BASMALAH] = value }
    }

    suspend fun setActiveGenerationReciterId(value: String) {
        context.dataStore.edit { it[ACTIVE_GENERATION_RECITER_ID] = value }
    }

    suspend fun setBackgroundKeywords(value: Set<String>) {
        context.dataStore.edit { it[BACKGROUND_KEYWORDS] = value }
    }

    private val sharedPrefs = context.getSharedPreferences("quran_reels_custom_clips", Context.MODE_PRIVATE)

    fun getCustomCuratedClipsSync(): String {
        return sharedPrefs.getString("custom_curated_clips", "[]") ?: "[]"
    }

    fun saveCustomCuratedClipsSync(clipsJson: String) {
        sharedPrefs.edit().putString("custom_curated_clips", clipsJson).commit()
    }

}
