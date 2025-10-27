package com.labactivity.lala.quiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.Timestamp

/**
 * Data model for a single quiz attempt.
 * Stores complete information about each quiz completion attempt.
 *
 * This model is used in the versioned quiz attempt tracking system where
 * all attempts are preserved in Firestore under:
 * users/{userId}/quiz_scores/{quizId}/attempts/{attemptId}
 */
@Parcelize
data class QuizAttempt(
    val attemptId: String = "",           // Auto-generated Firestore document ID
    val quizId: String = "",              // Quiz/module identifier
    val courseId: String = "",            // Course identifier
    val courseName: String = "",          // Display name of the course
    val score: Int = 0,                   // Number of correct answers
    val totalQuestions: Int = 0,          // Total questions in the quiz
    val percentage: Double = 0.0,         // Calculated percentage (score/total * 100)
    val passed: Boolean = false,          // True if score >= 70%
    val difficulty: String = "NORMAL",    // EASY, NORMAL, or HARD
    val timestamp: Long = 0L,             // Completion time (millis since epoch)
    val timeTaken: Long = 0L,             // Time taken to complete (in milliseconds)
    val attemptNumber: Int = 1            // Sequential attempt number (1st, 2nd, 3rd, etc.)
) : Parcelable {

    /**
     * Converts this QuizAttempt to a Map for Firestore storage.
     */
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "attemptId" to attemptId,
            "quizId" to quizId,
            "courseId" to courseId,
            "courseName" to courseName,
            "score" to score,
            "totalQuestions" to totalQuestions,
            "percentage" to percentage,
            "passed" to passed,
            "difficulty" to difficulty,
            "timestamp" to timestamp,
            "timeTaken" to timeTaken,
            "attemptNumber" to attemptNumber
        )
    }

    /**
     * Gets a formatted percentage string (e.g., "85.5%")
     */
    fun getFormattedPercentage(): String {
        return String.format("%.1f%%", percentage)
    }

    /**
     * Gets a formatted score string (e.g., "17/20")
     */
    fun getFormattedScore(): String {
        return "$score/$totalQuestions"
    }

    /**
     * Gets a performance category based on percentage
     */
    fun getPerformanceCategory(): PerformanceCategory {
        return when {
            percentage >= 90.0 -> PerformanceCategory.EXCELLENT
            percentage >= 80.0 -> PerformanceCategory.VERY_GOOD
            percentage >= 70.0 -> PerformanceCategory.GOOD
            percentage >= 60.0 -> PerformanceCategory.FAIR
            else -> PerformanceCategory.NEEDS_IMPROVEMENT
        }
    }

    companion object {
        /**
         * Creates a QuizAttempt instance from a Firestore document map.
         */
        fun fromMap(data: Map<String, Any>, documentId: String = ""): QuizAttempt {
            return QuizAttempt(
                attemptId = documentId.ifEmpty { data["attemptId"] as? String ?: "" },
                quizId = data["quizId"] as? String ?: "",
                courseId = data["courseId"] as? String ?: "",
                courseName = data["courseName"] as? String ?: "",
                score = (data["score"] as? Long)?.toInt() ?: 0,
                totalQuestions = (data["totalQuestions"] as? Long)?.toInt() ?: 0,
                percentage = (data["percentage"] as? Number)?.toDouble() ?: 0.0,
                passed = data["passed"] as? Boolean ?: false,
                difficulty = data["difficulty"] as? String ?: "NORMAL",
                timestamp = (data["timestamp"] as? Long) ?: 0L,
                timeTaken = (data["timeTaken"] as? Long) ?: 0L,
                attemptNumber = (data["attemptNumber"] as? Long)?.toInt() ?: 1
            )
        }
    }
}

/**
 * Performance categories for quiz attempts
 */
enum class PerformanceCategory(val displayName: String, val colorHex: String) {
    EXCELLENT("Excellent", "#4CAF50"),        // Green
    VERY_GOOD("Very Good", "#8BC34A"),        // Light Green
    GOOD("Good", "#CDDC39"),                  // Lime
    FAIR("Fair", "#FF9800"),                  // Orange
    NEEDS_IMPROVEMENT("Needs Improvement", "#F44336")  // Red
}

/**
 * Summary data for all attempts of a specific quiz.
 * Stored at: users/{userId}/quiz_scores/{quizId}
 */
@Parcelize
data class QuizScoreSummary(
    val quizId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val totalAttempts: Int = 0,
    val latestScore: Int = 0,
    val latestTotal: Int = 0,
    val latestPercentage: Double = 0.0,
    val latestPassed: Boolean = false,
    val latestDifficulty: String = "NORMAL",
    val latestTimestamp: Long = 0L,
    val highestScore: Int = 0,
    val highestPercentage: Double = 0.0,
    val averageScore: Double = 0.0,
    val averagePercentage: Double = 0.0,
    val firstAttemptTimestamp: Long = 0L,
    val lastAttemptTimestamp: Long = 0L
) : Parcelable {

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "quizId" to quizId,
            "courseId" to courseId,
            "courseName" to courseName,
            "totalAttempts" to totalAttempts,
            "latestScore" to latestScore,
            "latestTotal" to latestTotal,
            "latestPercentage" to latestPercentage,
            "latestPassed" to latestPassed,
            "latestDifficulty" to latestDifficulty,
            "latestTimestamp" to latestTimestamp,
            "highestScore" to highestScore,
            "highestPercentage" to highestPercentage,
            "averageScore" to averageScore,
            "averagePercentage" to averagePercentage,
            "firstAttemptTimestamp" to firstAttemptTimestamp,
            "lastAttemptTimestamp" to lastAttemptTimestamp
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any>): QuizScoreSummary {
            return QuizScoreSummary(
                quizId = data["quizId"] as? String ?: "",
                courseId = data["courseId"] as? String ?: "",
                courseName = data["courseName"] as? String ?: "",
                totalAttempts = (data["totalAttempts"] as? Long)?.toInt() ?: 0,
                latestScore = (data["latestScore"] as? Long)?.toInt() ?: 0,
                latestTotal = (data["latestTotal"] as? Long)?.toInt() ?: 0,
                latestPercentage = (data["latestPercentage"] as? Number)?.toDouble() ?: 0.0,
                latestPassed = data["latestPassed"] as? Boolean ?: false,
                latestDifficulty = data["latestDifficulty"] as? String ?: "NORMAL",
                latestTimestamp = (data["latestTimestamp"] as? Long) ?: 0L,
                highestScore = (data["highestScore"] as? Long)?.toInt() ?: 0,
                highestPercentage = (data["highestPercentage"] as? Number)?.toDouble() ?: 0.0,
                averageScore = (data["averageScore"] as? Number)?.toDouble() ?: 0.0,
                averagePercentage = (data["averagePercentage"] as? Number)?.toDouble() ?: 0.0,
                firstAttemptTimestamp = (data["firstAttemptTimestamp"] as? Long) ?: 0L,
                lastAttemptTimestamp = (data["lastAttemptTimestamp"] as? Long) ?: 0L
            )
        }
    }
}
