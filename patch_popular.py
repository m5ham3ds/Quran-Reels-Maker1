import re
with open("app/src/main/java/com/example/ui/PopularClipsScreen.kt", "r") as f:
    content = f.read()

imports = """
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
"""
if "import android.Manifest" not in content:
    content = content.replace("import android.content.Context", imports + "import android.content.Context")


launcher_code = """
    var delayedGenerateAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> 
        delayedGenerateAction?.invoke()
        delayedGenerateAction = null
    }

"""
content = content.replace("    val backgroundKeywords by settingsManager.backgroundKeywords.collectAsState(initial = emptySet())", launcher_code + "    val backgroundKeywords by settingsManager.backgroundKeywords.collectAsState(initial = emptySet())")

# Now update the generate button logic
button_code_to_replace = """                                        // Stop any ongoing audio preview to prevent overlapping sounds
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
                                        Toast.makeText(context, if (isArabic) "بدء المونتاج لـ ${clip.reciter}..." else "Starting production...", Toast.LENGTH_SHORT).show()"""

new_button_code = """                                        // Stop any ongoing audio preview to prevent overlapping sounds
                                        previewPlayer.stop()
                                        playingClipId = null
                                        
                                        val onGenerateAction = {
                                            viewModel.generate(
                                                context = context,
                                                surah = clip.surah,
                                                startAyah = clip.ayahStart,
                                                endAyah = clip.ayahEnd,
                                                reciterId = "popular|" + clip.reciterId,
                                                videoQuery = clip.videoQuery
                                            )
                                            Toast.makeText(context, if (isArabic) "بدء المونتاج لـ ${clip.reciter}..." else "Starting production...", Toast.LENGTH_SHORT).show()
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

                                        if (permissionsNeeded.isNotEmpty()) {
                                            delayedGenerateAction = onGenerateAction
                                            permissionLauncher.launch(permissionsNeeded.toTypedArray())
                                        } else {
                                            onGenerateAction()
                                        }"""

content = content.replace(button_code_to_replace, new_button_code)

with open("app/src/main/java/com/example/ui/PopularClipsScreen.kt", "w") as f:
    f.write(content)

