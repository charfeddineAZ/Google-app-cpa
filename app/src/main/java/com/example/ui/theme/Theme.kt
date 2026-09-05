package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudioColors = darkColorScheme(
  primary = Purple,
  onPrimary = Ink,
  primaryContainer = PurpleSoft,
  onPrimaryContainer = Cream,
  secondary = Lime,
  onSecondary = Ink,
  secondaryContainer = LimeDark,
  onSecondaryContainer = Lime,
  tertiary = Orange,
  onTertiary = Ink,
  background = Ink,
  onBackground = Cream,
  surface = InkElevated,
  onSurface = Cream,
  surfaceVariant = InkSoft,
  onSurfaceVariant = Muted,
  outline = Border,
  error = Red,
  onError = Ink
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = StudioColors,
    typography = Typography,
    content = content
  )
}
