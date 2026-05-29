package com.employeeapp.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand Colors ─────────────────────────────────────────────────────────────
val PrimaryBlue = Color(0xFF1E6FFF)
val PrimaryBlueDark = Color(0xFF4D8FFF)
val SecondaryIndigo = Color(0xFF6C63FF)
val TertiaryEmerald = Color(0xFF10B981)

val SurfaceLight = Color(0xFFF8FAFC)
val SurfaceDark = Color(0xFF0F172A)
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1E293B)

val ErrorRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
val SuccessGreen = Color(0xFF10B981)

val DepartmentColors = mapOf(
    "Engineering" to Color(0xFF3B82F6),
    "HR" to Color(0xFFEC4899),
    "Sales" to Color(0xFFF59E0B),
    "Finance" to Color(0xFF10B981),
    "Design" to Color(0xFF8B5CF6),
    "Ops" to Color(0xFFEF4444)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFF),
    onPrimaryContainer = Color(0xFF003085),
    secondary = SecondaryIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E7FF),
    tertiary = TertiaryEmerald,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Color(0xFF0F172A),
    surface = CardLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = Color(0xFF002B85),
    primaryContainer = Color(0xFF003BA8),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF9C94FF),
    onSecondary = Color(0xFF2B1EAF),
    secondaryContainer = Color(0xFF3D33C2),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF003728),
    background = SurfaceDark,
    onBackground = Color(0xFFF1F5F9),
    surface = CardDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

@Composable
fun EmployeeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
