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
import com.example.data.ThemeMode

private val WishYaLightColorScheme = lightColorScheme(
    primary = WishPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = WishPurpleSoft,
    onPrimaryContainer = WishPurpleDark,

    secondary = WishPinkSecondary,
    onSecondary = Color.White,
    secondaryContainer = WishPinkSoft,
    onSecondaryContainer = WishPinkDark,

    tertiary = WishPurpleLight,
    onTertiary = Color.White,
    tertiaryContainer = WishLavenderSoft,
    onTertiaryContainer = WishPurpleDark,

    background = WishBgLight,
    onBackground = WishTextPrimaryLight,
    surface = WishSurfaceLight,
    onSurface = WishTextPrimaryLight,
    surfaceVariant = WishSurfaceVariantLight,
    onSurfaceVariant = WishTextSecondaryLight,

    outline = WishOutlineLight,
    outlineVariant = Color(0xFFEFE9F9)
)

private val WishYaDarkColorScheme = darkColorScheme(
    primary = WishLavenderAccent,
    onPrimary = WishBgDark,
    primaryContainer = WishPurpleDark,
    onPrimaryContainer = WishPurpleSoft,

    secondary = WishPinkLight,
    onSecondary = WishBgDark,
    secondaryContainer = WishPinkDark,
    onSecondaryContainer = WishPinkSoft,

    tertiary = WishPurpleLight,
    onTertiary = WishBgDark,
    tertiaryContainer = WishSurfaceVariantDark,
    onTertiaryContainer = WishLavenderSoft,

    background = WishBgDark,
    onBackground = WishTextPrimaryDark,
    surface = WishSurfaceDark,
    onSurface = WishTextPrimaryDark,
    surfaceVariant = WishSurfaceVariantDark,
    onSurfaceVariant = WishTextSecondaryDark,

    outline = WishOutlineDark,
    outlineVariant = Color(0xFF382E52)
)

@Composable
fun WishYaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Keep signature purple/pink palette consistent
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> WishYaDarkColorScheme
        else -> WishYaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
