package com.example.nammaride.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- DYNAMIC THEME SYSTEM ---
data class NammaColors(
    val background: Color, val text: Color, val input: Color, val border: Color, val subtext: Color,
    val primary: Color = Color(0xFF8C000C), val accent: Color = Color(0xFFFFEB3B)
)

val DarkThemeColors = NammaColors(background = Color(0xFF0D0D0D), text = Color(0xFFFFFFFF), input = Color(0xFF1A1A1A), border = Color(0xFF333333), subtext = Color(0xFFA0A0A0))
val LightThemeColors = NammaColors(background = Color(0xFFF3F4F6), text = Color(0xFF111827), input = Color(0xFFFFFFFF), border = Color(0xFFD1D5DB), subtext = Color(0xFF6B7280))
val LocalNammaColors = staticCompositionLocalOf { DarkThemeColors }

val SuccessGreen = Color(0xFF22C55E)
val MapBlue = Color(0xFF3B82F6)
val ErrorRed = Color(0xFFEF4444)