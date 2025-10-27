package com.labactivity.lala.ProfileMainActivity5

/**
 * Data class representing a quiz attempt in user's history
 */
data class QuizHistoryItem(
    val quizId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val completedAt: Long = 0L,
    val difficulty: String = "NORMAL"
) {
    val percentage: Int
        get() = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
}
