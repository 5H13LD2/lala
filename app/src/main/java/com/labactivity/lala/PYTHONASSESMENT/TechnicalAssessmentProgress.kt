package com.labactivity.lala.PYTHONASSESMENT

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data class representing user's progress on a technical assessment
 * Maps to: user_progress/{userId}/technical_assessment_progress/{challengeId}
 */
data class TechnicalAssessmentProgress(
    @DocumentId
    val challengeId: String = "",

    @PropertyName("challengeTitle")
    val challengeTitle: String = "",

    @PropertyName("status")
    val status: String = "not_started",  // "not_started", "in_progress", "completed"

    @PropertyName("attempts")
    val attempts: Int = 0,

    @PropertyName("bestScore")
    val bestScore: Int = 0,

    @PropertyName("lastAttemptDate")
    val lastAttemptDate: Timestamp? = null,

    @PropertyName("timeTaken")
    val timeTaken: Long = 0,  // in milliseconds

    @PropertyName("userCode")
    val userCode: String = "",

    @PropertyName("passed")
    val passed: Boolean = false,

    @PropertyName("updatedAt")
    val updatedAt: Timestamp? = null
) {
    /**
     * Returns a user-friendly status text
     */
    val statusText: String
        get() = when (status) {
            "not_started" -> "Not Started"
            "in_progress" -> "In Progress"
            "completed" -> "Completed"
            else -> "Unknown"
        }

    /**
     * Returns a formatted time taken string
     */
    val formattedTimeTaken: String
        get() {
            if (timeTaken == 0L) return "N/A"
            val seconds = timeTaken / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return if (minutes > 0) {
                "${minutes}m ${remainingSeconds}s"
            } else {
                "${remainingSeconds}s"
            }
        }

    /**
     * Returns formatted last attempt date
     */
    val formattedLastAttemptDate: String
        get() {
            return lastAttemptDate?.toDate()?.toString() ?: "Never"
        }
}
