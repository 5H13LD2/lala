package com.labactivity.lala.GAMIFICATION

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * XPManager - Handles experience points and leveling system
 *
 * EXP Earning Rules:
 * - 100 EXP for perfect score (100%)
 * - 70 EXP for passing (≥70%)
 * - 50 EXP for completing a technical assessment
 * - 20 EXP for failed quiz (<70%)
 *
 * Leveling Formula:
 * - level = floor(exp / 500)
 * - Each level requires 500 EXP
 */
class XPManager {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "XPManager"

        // XP Rewards
        const val XP_PERFECT_SCORE = 100  // 100% quiz score
        const val XP_PASSING_SCORE = 70   // ≥70% quiz score
        const val XP_TECHNICAL_ASSESSMENT = 50  // Complete technical assessment
        const val XP_FAILED_QUIZ = 20     // <70% quiz score

        // Leveling
        const val XP_PER_LEVEL = 500

        // User document fields
        private const val FIELD_TOTAL_XP = "totalXP"
        private const val FIELD_LEVEL = "level"
        private const val FIELD_COURSES_COMPLETED = "coursesCompleted"
        private const val FIELD_QUIZZES_TAKEN = "quizzesTaken"
        private const val FIELD_TECHNICAL_ASSESSMENTS_COMPLETED = "technicalAssessmentsCompleted"
    }

    /**
     * Awards XP for completing a quiz
     * Automatically calculates XP based on score percentage
     */
    suspend fun awardQuizXP(
        score: Int,
        totalQuestions: Int,
        difficulty: String = "NORMAL"
    ): Boolean {
        val percentage = (score * 100.0 / totalQuestions)
        val xpAmount = calculateQuizXP(percentage)

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "awarding Quiz XP")
        Log.d(TAG, "  Score: $score/$totalQuestions (${"%.1f".format(percentage)}%)")
        Log.d(TAG, "  Difficulty: $difficulty")
        Log.d(TAG, "  XP Awarded: $xpAmount")

        val success = updateUserXP(xpAmount, updateQuizzesTaken = true)

        if (success) {
            Log.d(TAG, "  ✓ Quiz XP awarded successfully")
        } else {
            Log.e(TAG, "  ✗ Failed to award quiz XP")
        }
        Log.d(TAG, "═══════════════════════════════════════")

        return success
    }

    /**
     * Awards XP for completing a technical assessment
     */
    suspend fun awardTechnicalAssessmentXP(
        challengeTitle: String,
        passed: Boolean,
        score: Int = 100
    ): Boolean {
        if (!passed) {
            Log.d(TAG, "Technical assessment not passed - no XP awarded")
            return true // Not an error, just no XP
        }

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "Awarding Technical Assessment XP")
        Log.d(TAG, "  Challenge: $challengeTitle")
        Log.d(TAG, "  Score: $score")
        Log.d(TAG, "  XP Awarded: $XP_TECHNICAL_ASSESSMENT")

        val success = updateUserXP(
            XP_TECHNICAL_ASSESSMENT,
            updateTechnicalAssessments = true
        )

        if (success) {
            Log.d(TAG, "  ✓ Technical assessment XP awarded successfully")
        } else {
            Log.e(TAG, "  ✗ Failed to award technical assessment XP")
        }
        Log.d(TAG, "═══════════════════════════════════════")

        return success
    }

    /**
     * Calculates XP amount based on quiz percentage
     */
    private fun calculateQuizXP(percentage: Double): Int {
        return when {
            percentage >= 100.0 -> XP_PERFECT_SCORE
            percentage >= 70.0 -> XP_PASSING_SCORE
            else -> XP_FAILED_QUIZ
        }
    }

    /**
     * Updates user's total XP and recalculates level
     * Also increments relevant counters and checks for achievements
     */
    private suspend fun updateUserXP(
        xpAmount: Int,
        updateQuizzesTaken: Boolean = false,
        updateTechnicalAssessments: Boolean = false
    ): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: run {
                Log.w(TAG, "⚠ User not authenticated")
                return false
            }

            val userDocRef = firestore.collection("users").document(userId)

            // Get current XP to calculate new level
            val currentDoc = userDocRef.get().await()
            val currentXP = (currentDoc.getLong(FIELD_TOTAL_XP) ?: 0).toInt()
            val newXP = currentXP + xpAmount
            val newLevel = calculateLevel(newXP)

            Log.d(TAG, "  Current XP: $currentXP → New XP: $newXP")
            Log.d(TAG, "  New Level: $newLevel")

            // Build update map
            val updates = mutableMapOf<String, Any>(
                FIELD_TOTAL_XP to FieldValue.increment(xpAmount.toLong()),
                FIELD_LEVEL to newLevel
            )

            // Add counter increments if needed
            if (updateQuizzesTaken) {
                updates[FIELD_QUIZZES_TAKEN] = FieldValue.increment(1)
            }
            if (updateTechnicalAssessments) {
                updates[FIELD_TECHNICAL_ASSESSMENTS_COMPLETED] = FieldValue.increment(1)
            }

            // Update Firestore
            userDocRef.update(updates).await()

            Log.d(TAG, "  ✓ User XP updated successfully")
            Log.d(TAG, "    totalXP: +$xpAmount")
            Log.d(TAG, "    level: $newLevel")
            if (updateQuizzesTaken) Log.d(TAG, "    quizzesTaken: +1")
            if (updateTechnicalAssessments) Log.d(TAG, "    technicalAssessmentsCompleted: +1")

            // Check and unlock achievements
            val achievementManager = AchievementManager()
            val unlockedAchievements = achievementManager.checkAndUnlockAchievements(newXP)

            if (unlockedAchievements.isNotEmpty()) {
                Log.d(TAG, "  🏆 New achievements unlocked: ${unlockedAchievements.size}")
                unlockedAchievements.forEach { unlocked ->
                    Log.d(TAG, "    - ${unlocked.achievement.title} (${unlocked.badgeEarned})")
                }
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "  ✗ Error updating user XP", e)
            false
        }
    }

    /**
     * Calculates level from total XP
     * Formula: level = floor(exp / 500)
     */
    fun calculateLevel(totalXP: Int): Int {
        return totalXP / XP_PER_LEVEL
    }

    /**
     * Calculates XP progress within current level (0-500)
     */
    fun getXPProgressInLevel(totalXP: Int): Int {
        return totalXP % XP_PER_LEVEL
    }

    /**
     * Calculates percentage progress to next level
     */
    fun getProgressPercentage(totalXP: Int): Int {
        val progressInLevel = getXPProgressInLevel(totalXP)
        return ((progressInLevel * 100.0) / XP_PER_LEVEL).toInt()
    }

    /**
     * Gets formatted level string (e.g., "Level 3")
     */
    fun getLevelString(totalXP: Int): String {
        val level = calculateLevel(totalXP)
        return "Level $level"
    }

    /**
     * Gets XP remaining to next level
     */
    fun getXPToNextLevel(totalXP: Int): Int {
        return XP_PER_LEVEL - getXPProgressInLevel(totalXP)
    }

    /**
     * Gets user XP data from Firestore
     */
    suspend fun getUserXPData(): UserXPData? {
        return try {
            val userId = auth.currentUser?.uid ?: return null

            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!document.exists()) return null

            val totalXP = (document.getLong(FIELD_TOTAL_XP) ?: 0).toInt()
            val level = calculateLevel(totalXP)
            val quizzesTaken = (document.getLong(FIELD_QUIZZES_TAKEN) ?: 0).toInt()
            val coursesCompleted = (document.getLong(FIELD_COURSES_COMPLETED) ?: 0).toInt()
            val technicalAssessments = (document.getLong(FIELD_TECHNICAL_ASSESSMENTS_COMPLETED) ?: 0).toInt()

            UserXPData(
                totalXP = totalXP,
                level = level,
                progressInLevel = getXPProgressInLevel(totalXP),
                progressPercentage = getProgressPercentage(totalXP),
                xpToNextLevel = getXPToNextLevel(totalXP),
                quizzesTaken = quizzesTaken,
                coursesCompleted = coursesCompleted,
                technicalAssessmentsCompleted = technicalAssessments
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting user XP data", e)
            null
        }
    }
}

/**
 * Data class for user XP information
 */
data class UserXPData(
    val totalXP: Int,
    val level: Int,
    val progressInLevel: Int,
    val progressPercentage: Int,
    val xpToNextLevel: Int,
    val quizzesTaken: Int,
    val coursesCompleted: Int,
    val technicalAssessmentsCompleted: Int
)
