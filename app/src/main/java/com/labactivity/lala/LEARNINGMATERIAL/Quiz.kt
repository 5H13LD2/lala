package com.labactivity.lala.LEARNINGMATERIAL

/**
 * Represents a quiz question in the learning material.
 * This is a simplified version of the quiz model used in the quiz module.
 */
data class Quiz(
    val id: String,
    val question: String,
    val options: List<String>,
    val answer: Int,  // Index of correct answer
    val explanation: String = "",
    val difficulty: String = "normal",
    val order: Int = 0
) 