package com.example.generator

import android.content.Context
import android.os.Build
import android.os.Environment
import com.example.settings.SettingsManager
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SystemDiagnosticTracker {
    private val logList = mutableListOf<String>()
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .build()
            chain.proceed(request)
        }
        .build()

    @Synchronized
    fun addLog(tag: String, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val formatted = "[$timestamp] [$tag] $message"
        logList.add(formatted)
        android.util.Log.d("SystemDiagnostic", formatted)
    }

    @Synchronized
    fun getLogs(): List<String> = logList.toList()

    @Synchronized
    fun clearLogs() {
        logList.clear()
        addLog("SYSTEM", "Logs cleared. Beginning new diagnostic tracking session.")
    }

    suspend fun runFullSystemAudit(context: Context): String {
        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("      تقرير الفحص التشخيصي الشامل - QURAN REELS MAKER      \n")
        sb.append("====================================================\n\n")

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        sb.append("تاريخ الفحص (Audit Time): $timestamp\n\n")

        // 1. Device Information
        sb.append("----------------------------------------------------\n")
        sb.append("1. معلومات نظام التشغيل والجهاز (Device & OS Info)\n")
        sb.append("----------------------------------------------------\n")
        sb.append("الشركة المصنعة (Manufacturer): ${Build.MANUFACTURER}\n")
        sb.append("الموديل (Model): ${Build.MODEL}\n")
        sb.append("إصدار الأندرويد (Android Version): ${Build.VERSION.RELEASE}\n")
        sb.append("مستوى السيرفر (SDK Level): ${Build.VERSION.SDK_INT}\n")
        sb.append("لوحة المعالجة (CPU ABI): ${Build.SUPPORTED_ABIS.joinToString(", ")}\n")
        sb.append("الذاكرة المتوفرة الكلية بالنظام: ${getSystemRamInfo(context)}\n\n")

        // 2. Network Diagnostics
        sb.append("----------------------------------------------------\n")
        sb.append("2. اختبار الاتصال بالخوادم والتكامل الشبكي (Network Audit)\n")
        sb.append("----------------------------------------------------\n")
        val endpoints = listOf(
            "api.alquran.cloud" to "https://api.alquran.cloud/v1/edition?format=audio&language=ar",
            "cdn.islamic.network" to "https://cdn.islamic.network/quran/audio/64/ar.alafasy/1.mp3",
            "qalam249-whisperx.hf.space" to "https://qalam249-whisperx.hf.space",
            "api.pexels.com" to "https://api.pexels.com/videos/popular?per_page=1"
        )

        for ((host, url) in endpoints) {
            sb.append("- فحص الاتصال بـ $host ... ")
            try {
                val reqBuilder = Request.Builder().url(url)
                if (host == "api.pexels.com") {
                    val settingsManager = SettingsManager(context)
                    val pKey = settingsManager.pexelsApiKey.first()
                    if (pKey.isNotBlank()) {
                        reqBuilder.addHeader("Authorization", pKey)
                    }
                }
                val request = reqBuilder.head().build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 405 || response.code == 401) {
                        sb.append("ناجح (Successful) - رمز الاستجابة: ${response.code}\n")
                    } else {
                        sb.append("فشل جزئي (Warning) - رمز الاستجابة: ${response.code}\n")
                    }
                }
            } catch (e: Exception) {
                sb.append("غير متصل (Failed) - السبب: ${e.message ?: "اتصال مغلق أو مهلة"}\n")
            }
        }
        sb.append("\n")

        // 3. Permissions Diagnostics
        sb.append("----------------------------------------------------\n")
        sb.append("3. حالة أذونات التطبيق والوصول للذاكرة (Permissions Check)\n")
        sb.append("----------------------------------------------------\n")
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(android.Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
        }
        permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        for (p in permissions) {
            try {
                val status = androidx.core.content.ContextCompat.checkSelfPermission(context, p)
                val statusStr = if (status == android.content.pm.PackageManager.PERMISSION_GRANTED) "ممنوح (GRANTED)" else "مرفوض (DENIED) / لم يُطلب"
                sb.append("- إذن $p: $statusStr\n")
            } catch (e: Exception) {
                sb.append("- إذن $p: غير مدعوم أو غير محدد\n")
            }
        }
        sb.append("\n")

        // 4. File-System Directories & Cache Audit
        sb.append("----------------------------------------------------\n")
        sb.append("4. فحص المجلدات والملفات المحلية (Local Directory Storage Audit)\n")
        sb.append("----------------------------------------------------\n")
        val cacheDir = context.cacheDir
        val cacheFilesCount = cacheDir.listFiles()?.size ?: 0
        val totalCacheSizeStr = getFolderSizeLabel(cacheDir)
        sb.append("- مجلد الكاش الداخلي (Cache Directory): ${cacheDir.absolutePath}\n")
        sb.append("  عدد الملفات فيه: $cacheFilesCount ملف\n")
        sb.append("  حجم المجلد الكلي: $totalCacheSizeStr\n")

        val moviesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Quran Reels")
        sb.append("- مجلد حفظ المخرجات العام (Movies Directory): ${moviesDir.absolutePath}\n")
        sb.append("  هل المجلد موجود حالياً؟: ${if (moviesDir.exists()) "نعم (Yes)" else "لا (No)"}\n")
        if (moviesDir.exists()) {
            val videoCount = moviesDir.listFiles { _, name -> name.endsWith(".mp4") }?.size ?: 0
            sb.append("  عدد الفيديوهات المنتجة حالياً بالاستوديو: $videoCount مقطع\n")
        }

        val detailsDir = File(moviesDir, "Details")
        sb.append("- مجلد تفاصيل النشر (Details Text Storage): ${detailsDir.absolutePath}\n")
        sb.append("  هل المجلد موجود؟: ${if (detailsDir.exists()) "نعم" else "لا"}\n")
        if (detailsDir.exists()) {
            val detailsCount = detailsDir.listFiles()?.size ?: 0
            sb.append("  عدد تقارير النشر المحفوظة هناك: $detailsCount ملف\n")
        }
        sb.append("\n")

        // 5. System Execution Logs Summary
        sb.append("----------------------------------------------------\n")
        sb.append("5. تفاصيل خطوة بخطوة لسير آخر عملية مونتاج (Process Trace Logs)\n")
        sb.append("----------------------------------------------------\n")
        val logs = getLogs()
        if (logs.isEmpty()) {
            sb.append("لا توجد سجلات من عملية معالجة نشطة حالياً. يرجى البدء بإنشاء فيديو أولاً لمحاكاة الأخطاء بدقة.\n")
        } else {
            logs.forEach { logLine ->
                sb.append("$logLine\n")
            }
        }
        sb.append("\n")
        sb.append("====================================================\n")
        sb.append("                نهاية التقرير التشخيصي               \n")
        sb.append("====================================================\n")

        val finalReport = sb.toString()

        // 6. Save data to txt in context program path data under externalFilesDir
        var reportSavedPath = ""
        try {
            val targetFolder = context.getExternalFilesDir(null) ?: context.filesDir
            if (!targetFolder.exists()) {
                targetFolder.mkdirs()
            }
            val reportFile = File(targetFolder, "QuranReel_System_Diagnostic_Report.txt")
            reportFile.writeText(finalReport, Charsets.UTF_8)
            reportSavedPath = reportFile.absolutePath
            addLog("SYSTEM", "Diagnostic report saved successfully to: ${reportFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            addLog("ERROR", "Failed to save diagnostic file: ${e.message}")
        }

        return finalReport
    }

    fun saveReportToFilesAndGetPath(context: Context, reportContent: String): String {
        return try {
            val targetFolder = context.getExternalFilesDir(null) ?: context.filesDir
            if (!targetFolder.exists()) {
                targetFolder.mkdirs()
            }
            val reportFile = File(targetFolder, "QuranReel_System_Diagnostic_Report.txt")
            reportFile.writeText(reportContent, Charsets.UTF_8)
            reportFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: " + e.message
        }
    }

    private fun getSystemRamInfo(context: Context): String {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memInfo = android.app.ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val totalGb = memInfo.totalMem / (1024f * 1024f * 1024f)
            val availGb = memInfo.availMem / (1024f * 1024f * 1024f)
            String.format(Locale.US, "%.2f GB / %.2f GB", availGb, totalGb)
        } catch (e: Exception) {
            "N/A"
        }
    }

    private fun getFolderSizeLabel(f: File): String {
        val bytes = getFolderSize(f)
        if (bytes < 1024) return "$bytes Bytes"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.1f MB", mb)
    }

    private fun getFolderSize(f: File): Long {
        var size: Long = 0
        if (f.isDirectory) {
            val files = f.listFiles() ?: return 0
            for (file in files) {
                size += getFolderSize(file)
            }
        } else {
            size += f.length()
        }
        return size
    }
}
