package com.labactivity.lala.GAMIFICATION

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.R
import kotlinx.coroutines.tasks.await

/**
 * AchievementManager - Manages user achievements based on XP milestones
 *
 * Achievement Tiers:
 * - Bronze: 500 XP (Level 1)
 * - Silver: 1500 XP (Level 3)
 * - Gold: 3000 XP (Level 6)
 * - Diamond: 5000 XP (Level 10)
 * - Master: 10000 XP (Level 20)
 */
class AchievementManager(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AchievementManager"
        private const val COLLECTION_ACHIEVEMENTS = "achievements"

        // XP Milestones for achievements
        const val BRONZE_MILESTONE = 500
        const val SILVER_MILESTONE = 1500
        const val GOLD_MILESTONE = 3000
        const val DIAMOND_MILESTONE = 5000
        const val MASTER_MILESTONE = 10000
    }

    /**
     * Achievement data class
     */
    data class Achievement(
        val id: String = "",
        val name: String = "",
        val description: String = "",
        val requiredXP: Int = 0,
        val unlockedAt: Long = 0,
        val isUnlocked: Boolean = false
    )

    /**
     * Get achievement tier based on total XP
     */
    fun getAchievementTier(totalXP: Int): AchievementTier {
        return when {
            totalXP >= MASTER_MILESTONE -> AchievementTier.MASTER
            totalXP >= DIAMOND_MILESTONE -> AchievementTier.DIAMOND
            totalXP >= GOLD_MILESTONE -> AchievementTier.GOLD
            totalXP >= SILVER_MILESTONE -> AchievementTier.SILVER
            totalXP >= BRONZE_MILESTONE -> AchievementTier.BRONZE
            else -> AchievementTier.NONE
        }
    }

    /**
     * Get achievement badge drawable resource ID
     */
    fun getAchievementBadge(totalXP: Int): Int? {
        return when (getAchievementTier(totalXP)) {
            AchievementTier.MASTER -> R.drawable.achievement_master
            AchievementTier.DIAMOND -> R.drawable.achievement_diamond
            AchievementTier.GOLD -> R.drawable.achievement_gold
            AchievementTier.SILVER -> R.drawable.achievement_silver
            AchievementTier.BRONZE -> R.drawable.achievement_bronze
            AchievementTier.NONE -> null
        }
    }

    /**
     * Get achievement name
     */
    fun getAchievementName(totalXP: Int): String {
        return when (getAchievementTier(totalXP)) {
            AchievementTier.MASTER -> "Master Coder"
            AchievementTier.DIAMOND -> "Diamond Developer"
            AchievementTier.GOLD -> "Gold Programmer"
            AchievementTier.SILVER -> "Silver Scholar"
            AchievementTier.BRONZE -> "Bronze Beginner"
            AchievementTier.NONE -> "No Badge"
        }
    }

    /**
     * Get achievement color
     */
    fun getAchievementColor(totalXP: Int): Int {
        return when (getAchievementTier(totalXP)) {
            AchievementTier.MASTER -> android.graphics.Color.parseColor("#9C27B0")
            AchievementTier.DIAMOND -> android.graphics.Color.parseColor("#00BCD4")
            AchievementTier.GOLD -> android.graphics.Color.parseColor("#FFD700")
            AchievementTier.SILVER -> android.graphics.Color.parseColor("#C0C0C0")
            AchievementTier.BRONZE -> android.graphics.Color.parseColor("#CD7F32")
            AchievementTier.NONE -> android.graphics.Color.parseColor("#888888")
        }
    }

    /**
     * Check and unlock achievements for user
     */
    suspend fun checkAndUnlockAchievements(totalXP: Int): List<Achievement> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val newlyUnlocked = mutableListOf<Achievement>()

        try {
            val achievementsTier = getAchievementTier(totalXP)

            // Get all achievements user should have
            val achievementsToUnlock = mutableListOf<String>()

            if (totalXP >= BRONZE_MILESTONE) achievementsToUnlock.add("bronze")
            if (totalXP >= SILVER_MILESTONE) achievementsToUnlock.add("silver")
            if (totalXP >= GOLD_MILESTONE) achievementsToUnlock.add("gold")
            if (totalXP >= DIAMOND_MILESTONE) achievementsToUnlock.add("diamond")
            if (totalXP >= MASTER_MILESTONE) achievementsToUnlock.add("master")

            // Get currently unlocked achievements
            val userAchievements = firestore.collection("users")
                .document(userId)
                .collection(COLLECTION_ACHIEVEMENTS)
                .get()
                .await()

            val unlockedIds = userAchievements.documents.map { it.id }

            // Unlock new achievements
            for (achievementId in achievementsToUnlock) {
                if (achievementId !in unlockedIds) {
                    val achievement = createAchievement(achievementId)

                    firestore.collection("users")
                        .document(userId)
                        .collection(COLLECTION_ACHIEVEMENTS)
                        .document(achievementId)
                        .set(achievement)
                        .await()

                    newlyUnlocked.add(achievement)
                    Log.d(TAG, "✅ Unlocked achievement: ${achievement.name}")
                }
            }

            // Update user's current achievement tier
            firestore.collection("users")
                .document(userId)
                .update("achievementTier", achievementsTier.name)
                .await()

        } catch (e: Exception) {
            Log.e(TAG, "Error checking achievements", e)
        }

        return newlyUnlocked
    }

    /**
     * Create achievement object
     */
    private fun createAchievement(achievementId: String): Achievement {
        return when (achievementId) {
            "bronze" -> Achievement(
                id = "bronze",
                name = "Bronze Beginner",
                description = "Earned 500 XP",
                requiredXP = BRONZE_MILESTONE,
                unlockedAt = System.currentTimeMillis(),
                isUnlocked = true
            )
            "silver" -> Achievement(
                id = "silver",
                name = "Silver Scholar",
                description = "Earned 1,500 XP",
                requiredXP = SILVER_MILESTONE,
                unlockedAt = System.currentTimeMillis(),
                isUnlocked = true
            )
            "gold" -> Achievement(
                id = "gold",
                name = "Gold Programmer",
                description = "Earned 3,000 XP",
                requiredXP = GOLD_MILESTONE,
                unlockedAt = System.currentTimeMillis(),
                isUnlocked = true
            )
            "diamond" -> Achievement(
                id = "diamond",
                name = "Diamond Developer",
                description = "Earned 5,000 XP",
                requiredXP = DIAMOND_MILESTONE,
                unlockedAt = System.currentTimeMillis(),
                isUnlocked = true
            )
            "master" -> Achievement(
                id = "master",
                name = "Master Coder",
                description = "Earned 10,000 XP",
                requiredXP = MASTER_MILESTONE,
                unlockedAt = System.currentTimeMillis(),
                isUnlocked = true
            )
            else -> Achievement()
        }
    }

    /**
     * Get all unlocked achievements for user
     */
    suspend fun getUserAchievements(): List<Achievement> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection(COLLECTION_ACHIEVEMENTS)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Achievement::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting achievements", e)
            emptyList()
        }
    }
}

/**
 * Achievement tier enum
 */
enum class AchievementTier {
    NONE,
    BRONZE,
    SILVER,
    GOLD,
    DIAMOND,
    MASTER
}
