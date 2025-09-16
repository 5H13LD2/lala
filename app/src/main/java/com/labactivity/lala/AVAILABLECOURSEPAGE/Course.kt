package com.labactivity.lala.AVAILABLECOURSEPAGE

// Reusable Course data class
data class Course(
    val name: String,
    val courseId: String,
    val imageResId: Int,
    val description: String = "",
    val category: String = "General",
    val difficulty: String = "Beginner"

)
