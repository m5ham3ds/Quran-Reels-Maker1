package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.generator.VideoGenerator
import com.example.ui.ReelState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VideoGenerationService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var notificationManager: NotificationManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val channelId = "video_generation_channel"
    private val notificationId = 1001

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "QuranReel:VideoGenerationLock")
        wakeLock?.acquire()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_STICKY
        }

        val action = intent.action
        if (action == "com.example.action.PAUSE_RESUME") {
            togglePauseResumed()
            val language = kotlinx.coroutines.runBlocking {
                com.example.settings.SettingsManager(this@VideoGenerationService).language.first()
            }
            val isArabic = language == "ar"
            updateNotificationProgress(lastMessage, lastProgress, isArabic)
            return START_STICKY
        } else if (action == "com.example.action.CANCEL") {
            cancelGeneration()
            stopForeground(true)
            stopSelf()
            return START_STICKY
        }

        resetControlFlags()

        val surah = intent.getIntExtra("surah", 1)
        val startAyah = intent.getIntExtra("startAyah", 1)
        val endAyah = intent.getIntExtra("endAyah", 5)
        val reciterId = intent.getStringExtra("reciterId") ?: "ar.alafasy"
        val showTranslation = intent.getBooleanExtra("showTranslation", true)
        val pexelsApiKey = intent.getStringExtra("pexelsApiKey") ?: ""
        val videoQuality = intent.getStringExtra("videoQuality") ?: "Ultra"
        val isRetry = intent.getBooleanExtra("isRetry", false)
        val videoQuery = intent.getStringExtra("videoQuery")

        scope.launch {
            val settingsManager = com.example.settings.SettingsManager(this@VideoGenerationService)
            val language = settingsManager.language.first()
            val isArabic = language == "ar"

            // 1. Show immediate Foreground Service Notification
            startForegroundServiceState(startAyah, endAyah, isArabic)

            try {
                _serviceState.value = ReelState.Loading(
                    if (isRetry) {
                        if (isArabic) "جاري استئناف معالجة المقطع وإعادة المحاولة..." else "Resuming video generation..."
                    } else {
                        if (isArabic) "جاري بدء المعالجة وإنشاء القنوات السينمائية..." else "Initializing video filters..."
                    },
                    0f
                )
                val includeBasmalah = settingsManager.includeBasmalah.first()
                val videoGenerator = VideoGenerator()

                videoGenerator.generateReel(
                    context = this@VideoGenerationService,
                    surah = surah,
                    startAyah = startAyah,
                    endAyah = endAyah,
                    reciterId = reciterId,
                    showTranslation = showTranslation,
                    pexelsApiKey = pexelsApiKey,
                    videoQuality = videoQuality,
                    isRetry = isRetry,
                    includeBasmalah = includeBasmalah,
                    videoQuery = videoQuery,
                    onProgress = { msg, progress ->
                        _serviceState.value = ReelState.Loading(msg, progress)
                        updateNotificationProgress(msg, progress, isArabic)
                    },
                    onComplete = { uri ->
                        _serviceState.value = ReelState.Success(uri)
                        showCompleteNotification(uri, isArabic)
                        stopForeground(true)
                        stopSelf()
                    },
                    onError = { err ->
                        _serviceState.value = ReelState.Error(err)
                        showErrorNotification(err, isArabic)
                        stopForeground(true)
                        stopSelf()
                    }
                )
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: "Unknown error occurred"
                _serviceState.value = ReelState.Error(errMsg)
                showErrorNotification(errMsg, isArabic)
                stopForeground(true)
                stopSelf()
            }
        }

        return START_STICKY
    }

    private val completedChannelId = "video_completed_channel"

    private fun startForegroundServiceState(startAyah: Int, endAyah: Int, isArabic: Boolean) {
        val title = if (isArabic) "جاري تصميم مقطع ريلز القرآن..." else "Designing Quran Reel..."
        lastMessage = if (isArabic) "جاري معالجة الآيات من $startAyah إلى $endAyah" else "Processing verses $startAyah to $endAyah"
        lastProgress = 0f

        // Intent to open MainActivity when clicking notification
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            this, 10, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, VideoGenerationService::class.java).apply {
            action = "com.example.action.PAUSE_RESUME"
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 101, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, VideoGenerationService::class.java).apply {
            action = "com.example.action.CANCEL"
        }
        val cancelPendingIntent = PendingIntent.getService(
            this, 102, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseLabel = if (isPaused) {
            if (isArabic) "استئناف" else "Resume"
        } else {
            if (isArabic) "إيقاف مؤقت" else "Pause"
        }
        val pauseIcon = if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(lastMessage)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setColor(0xFFD29E57.toInt()) // Beautiful Luxury Gold accent
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(appPendingIntent)
            .setProgress(100, 0, false)
            .addAction(pauseIcon, pauseLabel, pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, if (isArabic) "إلغاء المونتاج" else "Cancel", cancelPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING)
        } else {
            startForeground(notificationId, notification)
        }
    }

    private fun updateNotificationProgress(message: String, progress: Float, isArabic: Boolean) {
        lastMessage = message
        lastProgress = progress

        val title = if (isArabic) "جاري تصميم المقطع السينمائي..." else "Reel render active..."
        
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            this, 10, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, VideoGenerationService::class.java).apply {
            action = "com.example.action.PAUSE_RESUME"
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 101, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, VideoGenerationService::class.java).apply {
            action = "com.example.action.CANCEL"
        }
        val cancelPendingIntent = PendingIntent.getService(
            this, 102, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseLabel = if (isPaused) {
            if (isArabic) "استئناف" else "Resume"
        } else {
            if (isArabic) "إيقاف مؤقت" else "Pause"
        }
        val pauseIcon = if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setColor(0xFFD29E57.toInt())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(appPendingIntent)
            .setProgress(100, (progress * 100).toInt(), false)
            .addAction(pauseIcon, pauseLabel, pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, if (isArabic) "إلغاء المونتاج" else "Cancel", cancelPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showCompleteNotification(uri: Uri, isArabic: Boolean) {
        val title = if (isArabic) "تم تصميم المقطع بنجاح! 🎉" else "Reel rendering complete! 🎉"
        val desc = if (isArabic) "اضغط لعرض المقطع ومشاركته في الأستوديو" else "Tap to view and share from your gallery"

        // Build intent to play video
        val playIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val playPendingIntent = PendingIntent.getActivity(
            this, 11, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build intent to share video directly from shade
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val sharePendingIntent = PendingIntent.getActivity(
            this, 13, Intent.createChooser(shareIntent, if (isArabic) "مشاركة المقطع" else "Share Reel"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, completedChannelId)
            .setContentTitle(title)
            .setContentText(desc)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setColor(0xFFD29E57.toInt())
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(playPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                android.R.drawable.ic_media_play,
                if (isArabic) "تشغيل الفيديو" else "Play Video",
                playPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_share,
                if (isArabic) "مشاركة" else "Share",
                sharePendingIntent
            )
            .build()

        notificationManager.notify(notificationId + 1, notification)
    }

    private fun showErrorNotification(error: String, isArabic: Boolean) {
        val title = if (isArabic) "فشل تصميم مقطع الفيديو" else "Reel design failed"

        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            this, 12, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, completedChannelId)
            .setContentTitle(title)
            .setContentText(error)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setColor(0xFFF44336.toInt()) // Red color accent for errors
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)
            .build()

        notificationManager.notify(notificationId + 2, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "جاري معالجة مقاطع الريلز",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "قناة إشعارات جاري معالجة فيديو ريلز القرآن الكريم"
                setShowBadge(false)
            }
            
            val completedChannel = NotificationChannel(
                completedChannelId,
                "تنبيهات اكتمال ريلز القرآن",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات واشعار اكتمال تصميم فيديو ريلز القرآن الكريم"
                enableLights(true)
                lightColor = 0xFFD29E57.toInt()
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(completedChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    companion object {
        private val _serviceState = MutableStateFlow<ReelState>(ReelState.Idle)
        val serviceState: StateFlow<ReelState> = _serviceState

        private val _isPausedState = MutableStateFlow(false)
        val isPausedState: StateFlow<Boolean> = _isPausedState

        @Volatile
        var lastMessage = ""
        @Volatile
        var lastProgress = 0f

        @Volatile
        var isPaused = false
        @Volatile
        var isCancelled = false
        val pauseLock = Object()

        fun togglePauseResumed() {
            synchronized(pauseLock) {
                isPaused = !isPaused
                _isPausedState.value = isPaused
                if (!isPaused) {
                    pauseLock.notifyAll()
                }
            }
        }

        fun cancelGeneration() {
            synchronized(pauseLock) {
                isCancelled = true
                isPaused = false
                _isPausedState.value = false
                pauseLock.notifyAll()
            }
            _serviceState.value = ReelState.Idle
        }

        fun resetControlFlags() {
            synchronized(pauseLock) {
                isPaused = false
                isCancelled = false
                _isPausedState.value = false
                pauseLock.notifyAll()
            }
        }

        fun clearState() {
            _serviceState.value = ReelState.Idle
        }
    }
}
