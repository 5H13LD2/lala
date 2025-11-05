package com.labactivity.lala.ProfileMainActivity5

/**
 * Data class representing an achievement badge in the user's profile
 */
data class AchievementBadgeItem(
    val name: String = "",
    val description: String = "",
    val requiredXP: Int = 0,
    val badgeDrawable: Int = 0,  // Drawable resource ID
    val isUnlocked: Boolean = false
)
