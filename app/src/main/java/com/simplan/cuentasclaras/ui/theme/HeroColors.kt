package com.simplan.cuentasclaras.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class HeroColors(
    val start: Color,
    val mid: Color,
    val end: Color
)

val HeroDark = HeroColors(
    start = Color(0xFF0B1B2A),
    mid = Color(0xFF123B43),
    end = Color(0xFF0F3A40)
)

val HeroLight = HeroColors(
    start = Color(0xFF0B745C),
    mid = Color(0xFF0F9471),
    end = Color(0xFF23A078)
)
val localHeroColors = staticCompositionLocalOf { HeroDark }
