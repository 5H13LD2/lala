package com.labactivity.lala.GAMIFICATION

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.LEADERBOARDPAGE.Achievement
import kotlinx.coroutines.tasks.await

/**
 * AchievementManager - Manages achievement unlocking and badge awarding
 *
 * Responsibilities:
 * - Check and unlock achievements when XP thresholds are met
 * - Award badges tied to achievements
 * - Sync user achievements to Firestore
 * - Notify when new achievements are unlocked
 */
class AchievementManager {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AchievementManager"

        // Firestore field names
        private const val FIELD_ACHIEVEMENTS_UNLOCKED = "achievementsUnlocked"
        private const val FIELD_CURRENT_BADGE = "currentBadge"
        private const val FIELD_TOTAL_XP = "totalXP"

        // Achievement subcollection fields
        private const val FIELD_IS_UNLOCKED = "isUnlocked"
        private const val FIELD_UNLOCKED_AT = "unlockedAt"
        private const val FIELD_BADGE_EARNED = "badgeEarned"
        private const val FIELD_BADGE_TIER = "badgeTier"
    }

    /**
     * Data class for newly unlocked achievement
     */
    data class UnlockedAchievement(
        val achievement: Achievement,
        val badgeEarned: String
    )

    /**
     * Check and unlock achievements based on user's total XP
     * Returns list of newly unlocked achievements
     */
    suspend fun checkAndUnlockAchievements(totalXP: Int): List<UnlockedAchievement> {
        val userId = auth.currentUser?.uid ?: run {
            Log.w(TAG, "User not authenticated")
            return emptyList()
        }

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "Checking achievements for totalXP: $totalXP")

        val newlyUnlocked = mutableListOf<UnlockedAchievement>()

        try {
            val userRef = firestore.collection("users").document(userId)

            // Get current user data
            val userDoc = userRef.get().await()
            val unlockedAchievementIds = userDoc.get(FIELD_ACHIEVEMENTS_UNLOCKED) as? List<String> ?: emptyList()

            Log.d(TAG, "Currently unlocked: ${unlockedAchievementIds.size} achievements")

            // Get all milestone achievements
            val allAchievements = Achievement.getMilestoneAchievements()

            // Check each achievement
            for (achievement in allAchievements) {
                // Skip if already unlocked
                if (unlockedAchievementIds.contains(achievement.id)) {
                    continue
                }

                // Check if XP threshold is met
                if (totalXP >= achievement.requiredXP) {
                    Log.d(TAG, "🏆 Unlocking achievement: ${achievement.title}")

                    // Add to user's achievements subcollection
                    val achievementData = hashMapOf(
                        FIELD_IS_UNLOCKED to true,
                        FIELD_UNLOCKED_AT to FieldValue.serverTimestamp(),
                        FIELD_BADGE_EARNED to achievement.badge,
                        FIELD_BADGE_TIER to achievement.badgeTier
                    )

                    userRef.collection("achievements")
                        .document(achievement.id)
                        .set(achievementData)
                        .await()

                    // Add to achievementsUnlocked array in user document
                    userRef.update(FIELD_ACHIEVEMENTS_UNLOCKED, FieldValue.arrayUnion(achievement.id))
                        .await()

                    // Update current badge if this is a higher tier
                    val currentBadge = userDoc.getString(FIELD_CURRENT_BADGE) ?: Achievement.TIER_NONE
                    if (isHigherTier(achievement.badgeTier, currentBadge)) {
                        userRef.update(FIELD_CURRENT_BADGE, achievement.badgeTier)
                            .await()
                        Log.d(TAG, "  Badge upgraded: $currentBadge → ${achievement.badgeTier}")
                    }

                    newlyUnlocked.add(
                        UnlockedAchievement(
                            achievement = achievement,
                            badgeEarned = achievement.badgeTier
                        )
                    )
                }
            }

            if (newlyUnlocked.isNotEmpty()) {
                Log.d(TAG, "✓ Unlocked ${newlyUnlocked.size} new achievement(s)")
            } else {
                Log.d(TAG, "No new achievements unlocked")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error checking achievements", e)
        }

        Log.d(TAG, "═══════════════════════════════════════")
        return newlyUnlocked
    }

    /**
     * Get user's unlocked achievements from Firestore
     */
    suspend fun getUserAchievements(): List<Achievement> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val userRef = firestore.collection("users").document(userId)
            val achievementsSnapshot = userRef.collection("achievements").get().await()

            val unlockedIds = achievementsSnapshot.documents.map { it.id }
            val allAchievements = Achievement.getMilestoneAchievements()

            // Map achievements and mark as unlocked if in user's collection
            allAchievements.map { achievement ->
                val doc = achievementsSnapshot.documents.find { it.id == achievement.id }
                if (doc != null && doc.exists()) {
                    achievement.copy(
                        isUnlocked = true,
                        unlockedAt = doc.getTimestamp("unlockedAt")?.toDate()?.time ?: 0L
                    )
                } else {
                    achievement
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting user achievements", e)
            emptyList()
        }
    }

    /**
     * Get user's current badge
     */
    suspend fun getUserBadge(): String {
        val userId = auth.currentUser?.uid ?: return Achievement.TIER_NONE

        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            userDoc.getString(FIELD_CURRENT_BADGE) ?: Achievement.TIER_NONE

        } catch (e: Exception) {
            Log.e(TAG, "Error getting user badge", e)
            Achievement.TIER_NONE
        }
    }

    /**
     * Check if badge tier B is higher than badge tier A
     */
    private fun isHigherTier(tierB: String, tierA: String): Boolean {
        val tierOrder = listOf(
            Achievement.TIER_NONE,
            Achievement.TIER_BRONZE,
            Achievement.TIER_SILVER,
            Achievement.TIER_GOLD,
            Achievement.TIER_PLATINUM,
            Achievement.TIER_DIAMOND
        )

        val indexA = tierOrder.indexOf(tierA)
        val indexB = tierOrder.indexOf(tierB)

        return indexB > indexA
    }

    /**
     * Initialize user achievement fields if they don't exist
     */
    suspend fun initializeUserAchievements() {
        val userId = auth.currentUser?.uid ?: return

        try {
            val userRef = firestore.collection("users").document(userId)
            val userDoc = userRef.get().await()

            val updates = mutableMapOf<String, Any>()

            // Initialize achievementsUnlocked array if missing
            if (!userDoc.contains(FIELD_ACHIEVEMENTS_UNLOCKED)) {
                updates[FIELD_ACHIEVEMENTS_UNLOCKED] = emptyList<String>()
            }

            // Initialize currentBadge if missing
            if (!userDoc.contains(FIELD_CURRENT_BADGE)) {
                updates[FIELD_CURRENT_BADGE] = Achievement.TIER_NONE
            }

            // Apply updates if any
            if (updates.isNotEmpty()) {
                userRef.update(updates).await()
                Log.d(TAG, "User achievement fields initialized")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing user achievements", e)
        }
    }
}
