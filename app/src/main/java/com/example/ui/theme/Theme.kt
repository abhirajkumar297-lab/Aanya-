package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
  primary = RosePrimaryLight,
  onPrimary = OnRosePrimaryLight,
  primaryContainer = RoseContainerLight,
  onPrimaryContainer = OnRoseContainerLight,
  secondary = MarigoldSecondaryLight,
  onSecondary = OnMarigoldSecondaryLight,
  secondaryContainer = MarigoldContainerLight,
  onSecondaryContainer = OnMarigoldContainerLight,
  tertiary = PlumTertiaryLight,
  onTertiary = OnPlumTertiaryLight,
  tertiaryContainer = PlumContainerLight,
  onTertiaryContainer = OnPlumContainerLight,
  background = CreamBackgroundLight,
  onBackground = EspressoOnBackgroundLight,
  surface = CreamSurfaceLight,
  onSurface = EspressoOnSurfaceLight,
  surfaceVariant = PeachSurfaceVariantLight,
  onSurfaceVariant = CocoaOnSurfaceVariantLight,
  outline = SoftBorderLight,
  outlineVariant = SoftBorderLight,
  surfaceContainerLowest = SurfaceContainerLowestLight,
  surfaceContainerLow = SurfaceContainerLowLight,
  surfaceContainer = SurfaceContainerLight,
  surfaceContainerHigh = SurfaceContainerHighLight,
)

private val DarkColorScheme = darkColorScheme(
  primary = CoralPrimaryDark,
  onPrimary = OnCoralPrimaryDark,
  primaryContainer = CoralContainerDark,
  onPrimaryContainer = OnCoralContainerDark,
  secondary = AmberSecondaryDark,
  onSecondary = OnAmberSecondaryDark,
  secondaryContainer = AmberContainerDark,
  onSecondaryContainer = OnAmberContainerDark,
  tertiary = MauveTertiaryDark,
  onTertiary = OnMauveTertiaryDark,
  tertiaryContainer = MauveContainerDark,
  onTertiaryContainer = OnMauveContainerDark,
  background = PlumBackgroundDark,
  onBackground = WarmCreamOnBackgroundDark,
  surface = PlumSurfaceDark,
  onSurface = WarmCreamOnSurfaceDark,
  surfaceVariant = PlumSurfaceVariantDark,
  onSurfaceVariant = DustyRoseOnSurfaceVariantDark,
  outline = SoftBorderDark,
  outlineVariant = SoftBorderDark,
  surfaceContainerLowest = SurfaceContainerLowestDark,
  surfaceContainerLow = SurfaceContainerLowDark,
  surfaceContainer = SurfaceContainerDark,
  surfaceContainerHigh = SurfaceContainerHighDark,
)

val AanyaShapes = Shapes(
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun AanyaTheme(
  darkThemeOverride: Boolean? = null,
  content: @Composable () -> Unit,
) {
  val darkTheme = darkThemeOverride ?: isSystemInDarkTheme()
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AanyaTypography,
    shapes = AanyaShapes,
    content = content,
  )
}
