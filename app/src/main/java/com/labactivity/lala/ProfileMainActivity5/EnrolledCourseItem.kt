package com.labactivity.lala.ProfileMainActivity5

/**
 * Data class representing an enrolled course in the user's profile
 */
data class EnrolledCourseItem(
    val courseId: String = "",
    val courseName: String = "",
    val category: String = "General",
    val difficulty: String = "Beginner",
    val enrolledAt: Long = 0L,
    val progress: Int = 0  // Progress percentage (0-100)
)
