package com.labactivity.lala.JAVACOMPILER.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Main data class representing a Java challenge from Firestore
 * Maps to: technical_assesment/{challengeId} where courseId contains "java"
 */
data class JavaChallenge(
    @DocumentId
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("difficulty")
    val difficulty: String = "",  // "Easy", "Medium", "Hard"

    @PropertyName("courseId")
    val courseId: String = "",

    @PropertyName("brokenCode")
    val brokenCode: String = "",

    @PropertyName("correctOutput")
    val correctOutput: String = "",

    @PropertyName("hint")
    val hint: String = "",

    @PropertyName("category")
    val category: String = "",

    @PropertyName("status")
    val status: String = "available",  // "available", "locked", "completed"

    @PropertyName("createdAt")
    val createdAt: String = "",

    @PropertyName("order")
    val order: Int = 0,

    @PropertyName("tags")
    val tags: List<String> = emptyList(),

    // Whether challenge is unlocked for the current user (computed at runtime)
    val isUnlocked: Boolean = true
) {
    /**
     * Returns a preview of the description (first 100 characters)
     */
    val descriptionPreview: String
        get() = if (description.length > 100) {
            description.substring(0, 100) + "..."
        } else {
            description
        }

    /**
     * Returns the difficulty color for UI display
     */
    val difficultyColor: String
        get() = when (difficulty.lowercase()) {
            "easy" -> "#4CAF50"
            "medium" -> "#FF9800"
            "hard" -> "#F44336"
            else -> "#9E9E9E"
        }

    /**
     * Returns a comma-separated string of tags
     */
    val tagsString: String
        get() = tags.joinToString(", ")
}

/**
 * User progress for a Java challenge
 * Maps to: user_progress/{userId}/technical_assessment_progress/{challengeId}
 */
data class JavaChallengeProgress(
    @PropertyName("challengeId")
    val challengeId: String = "",

    @PropertyName("status")
    val status: String = "not_started",  // "not_started", "in_progress", "completed"

    @PropertyName("attempts")
    val attempts: Int = 0,

    @PropertyName("bestScore")
    val bestScore: Int = 0,

    @PropertyName("lastAttemptDate")
    val lastAttemptDate: String = "",

    @PropertyName("timeTaken")
    val timeTaken: Long = 0,

    @PropertyName("userCode")
    val userCode: String = "",

    @PropertyName("passed")
    val passed: Boolean = false
) {
    /**
     * Returns progress percentage (0-100)
     */
    val progressPercentage: Int
        get() = if (passed) 100 else (bestScore.coerceIn(0, 100))

    /**
     * Returns a user-friendly status text
     */
    val statusText: String
        get() = when (status) {
            "completed" -> "Completed"
            "in_progress" -> "In Progress"
            else -> "Not Started"
        }
}

/**
 * Filter criteria for fetching Java challenges
 */
data class JavaChallengeFilter(
    val courseId: String? = null,
    val difficulty: String? = null,
    val category: String? = null,
    val status: String = "available",
    val searchQuery: String? = null
)

/**
 * Statistics for user's Java challenge progress
 */
data class JavaChallengeStats(
    val totalChallenges: Int = 0,
    val completedChallenges: Int = 0,
    val totalAttempts: Int = 0,
    val averageScore: Double = 0.0,
    val totalTimeTaken: Long = 0
) {
    /**
     * Returns completion percentage
     */
    val completionPercentage: Int
        get() = if (totalChallenges > 0) {
            ((completedChallenges.toDouble() / totalChallenges) * 100).toInt()
        } else {
            0
        }
}
