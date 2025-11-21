package com.labactivity.lala.ProfileMainActivity5

/**
 * Data class representing a technical assessment item for the profile page
 */
data class TechnicalAssessmentItem(
    val id: String = "",
    val title: String = "",
    val difficulty: String = "",
    val courseId: String = "",
    val category: String = "",
    val status: String = "available",  // "available", "in_progress", "completed"
    val isUnlocked: Boolean = true,
    val bestScore: Int = 0,
    val attempts: Int = 0,
    val passed: Boolean = false
)
