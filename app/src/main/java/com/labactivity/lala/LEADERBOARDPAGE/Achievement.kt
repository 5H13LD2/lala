package com.labactivity.lala.LEADERBOARDPAGE

data class Achievement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val requiredXP: Int = 0,
    val rewardMessage: String = "",
    val iconResource: Int = 0,
    val badge: String = "",  // Badge identifier (e.g., "badge_bronze", "badge_silver")
    val badgeTier: String = "",  // Badge tier (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND)
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
) {
    constructor() : this("", "", "", 0, "", 0, "", "", false, 0L)

    companion object {
        // Badge Tier Constants
        const val TIER_NONE = "NONE"
        const val TIER_BRONZE = "BRONZE"
        const val TIER_SILVER = "SILVER"
        const val TIER_GOLD = "GOLD"
        const val TIER_PLATINUM = "PLATINUM"
        const val TIER_DIAMOND = "DIAMOND"

        // Define milestone achievements with badges
        fun getMilestoneAchievements(): List<Achievement> {
            return listOf(
                Achievement(
                    id = "achievement_500XP",
                    title = "Beginner Explorer",
                    description = "Reached 500 XP milestone!",
                    requiredXP = 500,
                    rewardMessage = "You've entered the leaderboard! Keep learning!",
                    iconResource = android.R.drawable.star_on,
                    badge = "badge_bronze",
                    badgeTier = TIER_BRONZE
                ),
                Achievement(
                    id = "achievement_1000XP",
                    title = "Skilled Challenger",
                    description = "Reached 1000 XP milestone!",
                    requiredXP = 1000,
                    rewardMessage = "Great progress! You're on your way!",
                    iconResource = android.R.drawable.star_on,
                    badge = "badge_silver",
                    badgeTier = TIER_SILVER
                ),
                Achievement(
                    id = "achievement_2000XP",
                    title = "Elite Professional",
                    description = "Reached 2000 XP milestone!",
                    requiredXP = 2000,
                    rewardMessage = "Impressive dedication to learning!",
                    iconResource = android.R.drawable.star_on,
                    badge = "badge_gold",
                    badgeTier = TIER_GOLD
                ),
                Achievement(
                    id = "achievement_3000XP",
                    title = "Master Developer",
                    description = "Reached 3000 XP milestone!",
                    requiredXP = 3000,
                    rewardMessage = "You're becoming an expert!",
                    iconResource = android.R.drawable.star_on,
                    badge = "badge_platinum",
                    badgeTier = TIER_PLATINUM
                ),
                Achievement(
                    id = "achievement_5000XP",
                    title = "Diamond Legend",
                    description = "Reached 5000 XP milestone!",
                    requiredXP = 5000,
                    rewardMessage = "You've achieved legendary status! Congratulations!",
                    iconResource = android.R.drawable.star_on,
                    badge = "badge_diamond",
                    badgeTier = TIER_DIAMOND
                )
            )
        }

        // Check which achievements are unlocked based on XP
        fun getUnlockedAchievements(totalXP: Int): List<Achievement> {
            return getMilestoneAchievements().map { achievement ->
                achievement.copy(isUnlocked = totalXP >= achievement.requiredXP)
            }
        }

        // Get next achievement to unlock
        fun getNextAchievement(totalXP: Int): Achievement? {
            return getMilestoneAchievements().firstOrNull { it.requiredXP > totalXP }
        }

        // Get current badge based on total XP
        fun getCurrentBadge(totalXP: Int): String {
            return when {
                totalXP >= 5000 -> TIER_DIAMOND
                totalXP >= 3000 -> TIER_PLATINUM
                totalXP >= 2000 -> TIER_GOLD
                totalXP >= 1000 -> TIER_SILVER
                totalXP >= 500 -> TIER_BRONZE
                else -> TIER_NONE
            }
        }

        // Get badge display name
        fun getBadgeDisplayName(tier: String): String {
            return when (tier) {
                TIER_BRONZE -> "Bronze Badge"
                TIER_SILVER -> "Silver Badge"
                TIER_GOLD -> "Gold Badge"
                TIER_PLATINUM -> "Platinum Badge"
                TIER_DIAMOND -> "Diamond Badge"
                else -> "No Badge"
            }
        }

        // Get badge color
        fun getBadgeColor(tier: String): String {
            return when (tier) {
                TIER_BRONZE -> "#CD7F32"  // Bronze
                TIER_SILVER -> "#C0C0C0"  // Silver
                TIER_GOLD -> "#FFD700"    // Gold
                TIER_PLATINUM -> "#E5E4E2" // Platinum
                TIER_DIAMOND -> "#B9F2FF"  // Diamond
                else -> "#999999"          // Gray
            }
        }
    }
}
