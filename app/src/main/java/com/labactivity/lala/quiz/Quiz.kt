package com.labactivity.lala.quiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a single quiz question with multiple choice options
 */
@Parcelize
data class Quiz(
    val id: String, // Unique identifier for the question
    val question: String, // The question text
    val options: List<String>, // List of available options
    val correctOptionIndex: Int, // Index of the correct option (0-based)
    val difficulty: Difficulty = Difficulty.NORMAL // Optional difficulty level
) : Parcelable

/**
 * Enum representing difficulty levels for quiz questions
 */
enum class Difficulty {
    EASY,
    NORMAL,
    HARD
} 