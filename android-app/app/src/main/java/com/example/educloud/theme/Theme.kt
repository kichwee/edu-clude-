package com.example.educloud.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EduBlue80,
    onPrimary = EduBlue40,
    primaryContainer = EduBlue40,
    secondary = EduGreen80,
    onSecondary = EduGreen40,
    secondaryContainer = EduGreen40,
    background = SurfaceDark,
    surface = SurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = EduCloudLeaf,
    onPrimary = EduCloudSurface,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Color(0xFF944B00),
    onSecondary = EduCloudSurface,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = EduCloudLake,
    onTertiary = EduCloudSurface,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = EduCloudPaper,
    onBackground = EduCloudInk,
    surface = EduCloudSurface,
    onSurface = EduCloudInk,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = EduCloudMutedInk,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = EduCloudError,
    onError = EduCloudSurface,
    errorContainer = EduCloudErrorContainer,
    onErrorContainer = OnErrorContainer,
)

@Composable
fun EduCloudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is supported on Android 12+ but we keep brand consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduCloudTypography,
        content = content
    )
}
