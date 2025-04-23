package com.labactivity.lala

data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val difficulty: Difficulty
)

enum class Difficulty {
    FUNDAMENTAL,
    BEGINNER,
    INTERMEDIATE,
    ADVANCED_INTERMEDIATE
}