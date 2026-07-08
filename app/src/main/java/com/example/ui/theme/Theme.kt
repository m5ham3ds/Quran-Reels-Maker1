package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD29E57),
    background = Color(0xFF0F0F12),
    surface = Color(0xFF18181D),
    surfaceVariant = Color(0xFF282830),
    onBackground = Color(0xFFE0E0E6),
    onSurfaceVariant = Color(0xFF9E9EA5)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFFD29E57),
    background = Color(0xFFF5F5F7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0E0E0),
    onBackground = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF757575)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Disable dynamic colors so our custom colors are always applied
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
