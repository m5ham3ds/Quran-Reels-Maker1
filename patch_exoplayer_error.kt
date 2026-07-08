            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                com.example.generator.SystemDiagnosticTracker.addLog("PLAYER_ERROR", "ExoPlayer error: ${error.errorCode} - ${error.message}")
                android.util.Log.e("PopularClipsScreen", "ExoPlayer error: ${error.message}", error)
                isPreviewLoading = false
                playingClipId = null
                Toast.makeText(
                    context, 
                    if (isArabic) "تعذر تشغيل العينة: ${error.message}" else "Could not play audio: ${error.message}", 
                    Toast.LENGTH_LONG
                ).show()
            }
