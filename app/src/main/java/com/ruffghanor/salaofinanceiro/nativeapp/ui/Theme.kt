package com.ruffghanor.salaofinanceiro.nativeapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFF0B1220)
    val Panel = Color(0xEE111827)
    val PanelAlt = Color(0xFF1F2937)
    val Line = Color(0x22334155)
    val Text = Color(0xFFE5E7EB)
    val Muted = Color(0xFF94A3B8)
    val Danger = Color(0xFFEF4444)
    val Success = Color(0xFF10B981)
    val SuccessText = Color(0xFFA7F3D0)
    val Track = Color(0xFF1E293B)
    val Primary = Color(0xFF6D28D9)
}

@Composable
fun SalonTheme(themeColorHex: String, content: @Composable () -> Unit) {
    val primary = parseColor(themeColorHex) ?: AppColors.Primary
    val scheme = darkColorScheme(
        primary = primary,
        secondary = primary,
        tertiary = Color(0xFF22D3EE),
        background = AppColors.Background,
        surface = AppColors.Panel,
        onPrimary = Color.White,
        onBackground = AppColors.Text,
        onSurface = AppColors.Text,
        error = AppColors.Danger,
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

private fun parseColor(hex: String): Color? = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrNull()
