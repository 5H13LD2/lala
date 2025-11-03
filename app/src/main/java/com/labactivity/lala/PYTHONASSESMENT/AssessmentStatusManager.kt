package com.labactivity.lala.PYTHONASSESMENT

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility class for managing assessment status updates
 * This can be called from CompilerActivity when an assessment is completed
 */
object AssessmentStatusManager {
    private const val TAG = "AssessmentStatusManager"
    private val assessmentService = TechnicalAssessmentService()

    /**
     * Marks an assessment as completed and saves progress
     * This should be called when the user successfully completes an assessment
     *
     * @param context Application context
     * @param challengeId The Firestore document ID of the challenge
     * @param challengeTitle The title of the challenge
     * @param passed Whether the user passed the challenge
     * @param score The score achieved (0-100, default 100)
     * @param timeTaken Time taken in milliseconds (default 0)
     * @param userCode The user's submitted code (default empty)
     */
    fun markAssessmentCompleted(
        context: Context,
        challengeId: String,
        challengeTitle: String,
        passed: Boolean = true,
        score: Int = 100,
        timeTaken: Long = 0,
        userCode: String = ""
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Save user progress to user_progress/{userId}/technical_assessment_progress/{challengeId}
                val success = assessmentService.saveUserProgress(
                    challengeId = challengeId,
                    challengeTitle = challengeTitle,
                    passed = passed,
                    score = score,
                    timeTaken = timeTaken,
                    userCode = userCode
                )

                if (success) {
                    Log.d(TAG, "✅ Marked assessment '$challengeTitle' as completed - Progress saved")
                } else {
                    Log.w(TAG, "⚠️ Failed to save progress for assessment '$challengeTitle'")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error marking assessment as completed", e)
            }
        }
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use markAssessmentCompleted with challengeId parameter
     */
    @Deprecated(
        "Use markAssessmentCompleted with challengeId parameter",
        ReplaceWith("markAssessmentCompleted(context, challengeId, challengeTitle, true)")
    )
    fun markAssessmentCompleted(context: Context, challengeTitle: String, courseId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val challengeId = generateChallengeId(challengeTitle, courseId)
                markAssessmentCompleted(context, challengeId, challengeTitle, true)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error marking assessment as completed", e)
            }
        }
    }

    /**
     * Generates a consistent challenge ID based on title and course
     * In a real implementation, this would be the actual Firestore document ID
     */
    private fun generateChallengeId(title: String, courseId: String): String {
        return "${courseId}_${title.lowercase().replace(" ", "_")}"
    }

    /**
     * Resets an assessment status to available (for retry scenarios)
     */
    fun resetAssessmentStatus(context: Context, challengeId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                assessmentService.updateChallengeStatus(challengeId, "available")
                Log.d(TAG, "✅ Reset assessment status to available")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error resetting assessment status", e)
            }
        }
    }

    /**
     * Gets user progress for a specific challenge
     * @return TechnicalAssessmentProgress object or null if not found
     */
    suspend fun getUserProgress(challengeId: String): TechnicalAssessmentProgress? {
        return try {
            assessmentService.getUserProgress(challengeId)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user progress", e)
            null
        }
    }

    /**
     * Gets all user progress for technical assessments
     * @return List of TechnicalAssessmentProgress objects
     */
    suspend fun getAllUserProgress(): List<TechnicalAssessmentProgress> {
        return try {
            assessmentService.getAllUserProgress()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting all user progress", e)
            emptyList()
        }
    }
}
