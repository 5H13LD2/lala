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
     * Marks an assessment as completed
     * This should be called when the user successfully completes an assessment
     */
    fun markAssessmentCompleted(context: Context, challengeTitle: String, courseId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // For now, we'll use the challenge title as the document ID
                // In a real implementation, you might want to use a proper challenge ID
                val challengeId = generateChallengeId(challengeTitle, courseId)
                
                assessmentService.updateChallengeStatus(challengeId, "taken")
                Log.d(TAG, "Marked assessment '$challengeTitle' as completed")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error marking assessment as completed", e)
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
    fun resetAssessmentStatus(context: Context, challengeTitle: String, courseId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val challengeId = generateChallengeId(challengeTitle, courseId)
                assessmentService.updateChallengeStatus(challengeId, "available")
                Log.d(TAG, "Reset assessment '$challengeTitle' status to available")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting assessment status", e)
            }
        }
    }
}
