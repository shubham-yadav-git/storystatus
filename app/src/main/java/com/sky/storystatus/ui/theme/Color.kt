package com.sky.storystatus.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * StatusWala Design System — Color Palette
 *
 * A warm, editorial palette centered around a deep indigo accent,
 * with warm off-white surfaces and rich contrast for visual hierarchy.
 */
object StatusWalaColors {
    // Brand primary — deep violet/indigo
    val Primary = Color(0xFF5C3FD6)
    val PrimaryLight = Color(0xFF7C5FFF)
    val PrimaryDark = Color(0xFF3A1FA8)
    val PrimaryContainer = Color(0xFFEDE7FF)
    val OnPrimaryContainer = Color(0xFF1D0066)

    // Brand secondary — warm amber accent
    val Secondary = Color(0xFFE8913A)
    val SecondaryLight = Color(0xFFFFAB5E)
    val SecondaryContainer = Color(0xFFFFF0DE)
    val OnSecondaryContainer = Color(0xFF3D2300)

    // Tertiary — soft rose for emotional content
    val Tertiary = Color(0xFFD65C8F)
    val TertiaryContainer = Color(0xFFFFD9E7)
    val OnTertiaryContainer = Color(0xFF3E0021)

    // Surfaces — warm-toned, not cold white
    val SurfaceLight = Color(0xFFFCF8F4)        // Warm off-white
    val SurfaceVariantLight = Color(0xFFF3EDE6)  // Slightly darker cream
    val BackgroundLight = Color(0xFFFFFBF7)      // Very warm background
    val CardLight = Color(0xFFFFFFFF)
    val OnSurfaceLight = Color(0xFF1C1917)       // Deep warm gray (near-black)
    val OnSurfaceVariantLight = Color(0xFF79716B) // Muted warm gray

    // Dark mode — rich deep surfaces, not pure black
    val SurfaceDark = Color(0xFF171215)
    val SurfaceVariantDark = Color(0xFF2D2226)
    val BackgroundDark = Color(0xFF0F0B0E)
    val CardDark = Color(0xFF211A1E)
    val OnSurfaceDark = Color(0xFFEDE0E4)
    val OnSurfaceVariantDark = Color(0xFFA8979D)

    // Semantic colors
    val Success = Color(0xFF2BB673)
    val SuccessContainer = Color(0xFFCCF5DC)
    val Error = Color(0xFFE53935)
    val ErrorContainer = Color(0xFFFFDAD6)
    val Warning = Color(0xFFF9A825)
    val Info = Color(0xFF2196F3)

    // Category accent colors
    val Attitude = Color(0xFFE53935)
    val Shayari = Color(0xFF8E24AA)
    val Love = Color(0xFFEC407A)
    val Motivation = Color(0xFFFB8C00)
    val Friendship = Color(0xFF43A047)
    val Sad = Color(0xFF5C6BC0)
    val Birthday = Color(0xFFAB47BC)
    val Festivals = Color(0xFFFF7043)

    // Gradient pairs for category badges
    val AttitudeGradient = listOf(Color(0xFFE53935), Color(0xFFFF7043))
    val ShayariGradient = listOf(Color(0xFF8E24AA), Color(0xFFCE93D8))
    val LoveGradient = listOf(Color(0xFFEC407A), Color(0xFFF48FB1))
    val MotivationGradient = listOf(Color(0xFFFB8C00), Color(0xFFFFCA28))
    val FriendshipGradient = listOf(Color(0xFF43A047), Color(0xFF81C784))
    val SadGradient = listOf(Color(0xFF5C6BC0), Color(0xFF9FA8DA))
    val BirthdayGradient = listOf(Color(0xFFAB47BC), Color(0xFFE1BEE7))
    val FestivalsGradient = listOf(Color(0xFFFF7043), Color(0xFFFFAB91))

    // Header gradient
    val HeaderGradientStart = Color(0xFF5C3FD6)
    val HeaderGradientEnd = Color(0xFF8E24AA)
}

/** Maps category names to their gradient pairs */
fun categoryGradient(category: String): List<Color> = when (category) {
    "Attitude" -> StatusWalaColors.AttitudeGradient
    "Hindi Shayari" -> StatusWalaColors.ShayariGradient
    "Love" -> StatusWalaColors.LoveGradient
    "Motivation" -> StatusWalaColors.MotivationGradient
    "Friendship" -> StatusWalaColors.FriendshipGradient
    "Sad" -> StatusWalaColors.SadGradient
    "Birthday" -> StatusWalaColors.BirthdayGradient
    "Festivals" -> StatusWalaColors.FestivalsGradient
    else -> listOf(StatusWalaColors.Primary, StatusWalaColors.PrimaryLight)
}

/** Maps category names to their accent colors */
fun categoryColor(category: String): Color = when (category) {
    "Attitude" -> StatusWalaColors.Attitude
    "Hindi Shayari" -> StatusWalaColors.Shayari
    "Love" -> StatusWalaColors.Love
    "Motivation" -> StatusWalaColors.Motivation
    "Friendship" -> StatusWalaColors.Friendship
    "Sad" -> StatusWalaColors.Sad
    "Birthday" -> StatusWalaColors.Birthday
    "Festivals" -> StatusWalaColors.Festivals
    else -> StatusWalaColors.Primary
}

/** Category emoji icons for visual flair */
fun categoryEmoji(category: String): String = when (category) {
    "Attitude" -> "🔥"
    "Hindi Shayari" -> "✍️"
    "Love" -> "❤️"
    "Motivation" -> "💪"
    "Friendship" -> "🤝"
    "Sad" -> "💔"
    "Birthday" -> "🎂"
    "Festivals" -> "🎉"
    else -> "✨"
}
