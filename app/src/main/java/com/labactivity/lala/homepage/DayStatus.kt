package com.labactivity.lala.homepage

/**
 * Represents the status of a day in the calendar
 * Used to show quiz/assessment results with color coding
 */
enum class DayStatus {
    NONE,       // No activity - gray (default)
    PASSED,     // All quizzes passed - green
    FAILED,     // All quizzes failed - red
    MIXED       // Some passed, some failed - yellow/orange
}

/**
 * Data class to hold daily quiz results
 */
data class DailyResult(
    val date: String,           // YYYY-MM-DD format
    val passedCount: Int = 0,   // Number of passed assessments
    val failedCount: Int = 0,   // Number of failed assessments
    val totalCount: Int = 0     // Total assessments taken
) {
    /**
     * Calculate the status based on results
     */
    fun getStatus(): DayStatus {
        return when {
            totalCount == 0 -> DayStatus.NONE
            failedCount == 0 && passedCount > 0 -> DayStatus.PASSED
            passedCount == 0 && failedCount > 0 -> DayStatus.FAILED
            passedCount > 0 && failedCount > 0 -> DayStatus.MIXED
            else -> DayStatus.NONE
        }
    }

    /**
     * Get the fill percentage for water animation (0.0 to 1.0)
     */
    fun getFillPercentage(): Float {
        if (totalCount == 0) return 0f
        return passedCount.toFloat() / totalCount.toFloat()
    }
}
