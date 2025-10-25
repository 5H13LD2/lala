package com.labactivity.lala.quiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a quiz question from Firestore.
 *
 * Firestore document structure:
 * /course_quiz/{quizId}/questions/{questionId}
 * {
 *   "question": "What is Python?",
 *   "options": ["A programming language", "A snake", "A framework", "A library"],
 *   "correctOptionIndex": 0,
 *   "explanation": "The 'def' keyword is used to define a function in Python.",
 *   "difficulty": "EASY",
 *   "order": 1,
 *   "module_id": "java_module_1"
 * }
 */
@Parcelize
data class Quiz(
    var question: String = "",
    var options: List<String> = emptyList(),
    var correctOptionIndex: Int = -1,
    var explanation: String = "",
    var difficulty: String = "NORMAL",
    var order: Int = 0,
    var module_id: String = ""
) : Parcelable {
    /**
     * Validates that this Quiz object has all required fields properly set
     * @return true if valid, false otherwise
     */
    fun isValid(): Boolean {
        return question.isNotEmpty() &&
                options.size >= 2 &&
                correctOptionIndex in options.indices &&
                module_id.isNotEmpty()
    }

    /**
     * Gets a human-readable validation error message
     * @return Error message or null if valid
     */
    fun getValidationError(): String? {
        return when {
            question.isEmpty() -> "Question text is empty"
            options.isEmpty() -> "No options provided"
            options.size < 2 -> "Less than 2 options (need at least 2)"
            correctOptionIndex < 0 -> "Invalid correctOptionIndex (negative)"
            correctOptionIndex >= options.size -> "Invalid correctOptionIndex ($correctOptionIndex >= ${options.size})"
            module_id.isEmpty() -> "module_id is empty"
            else -> null
        }
    }

    /**
     * Difficulty enum values
     */
    enum class Difficulty {
        EASY, NORMAL, HARD;

        companion object {
            fun fromString(value: String): Difficulty {
                return try {
                    valueOf(value.uppercase())
                } catch (e: IllegalArgumentException) {
                    NORMAL // Default fallback
                }
            }
        }
    }
}