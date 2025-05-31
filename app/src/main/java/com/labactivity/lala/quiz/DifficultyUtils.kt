package com.labactivity.lala.quiz

import com.labactivity.lala.PYTHONASSESMENT.Challenge

/**
 * Utility class to handle difficulty-related operations
 */
object DifficultyUtils {

    // Constants for difficulty levels
    const val DIFFICULTY_EASY = "Easy"
    const val DIFFICULTY_MEDIUM = "Medium"
    const val DIFFICULTY_HARD = "Hard"

    /**
     * Returns the color resource ID for a given difficulty level
     */
    fun getColorForDifficulty(difficulty: String): Int {
        return when (difficulty) {
            DIFFICULTY_EASY -> android.R.color.holo_green_dark
            DIFFICULTY_MEDIUM -> android.R.color.holo_orange_dark
            DIFFICULTY_HARD -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }
    }

    /**
     * Returns the point value for completing a challenge of given difficulty
     */
    fun getPointsForDifficulty(difficulty: String): Int {
        return when (difficulty) {
            DIFFICULTY_EASY -> 10
            DIFFICULTY_MEDIUM -> 20
            DIFFICULTY_HARD -> 35
            else -> 5
        }
    }

    /**
     * Returns a list of challenges filtered by difficulty
     */
    fun filterChallengesByDifficulty(challenges: List<Challenge>, difficulty: String?): List<Challenge> {
        return if (difficulty == null) {
            challenges
        } else {
            challenges.filter { it.difficulty == difficulty }
        }
    }

    /**
     * Returns a list of suggested next challenges based on completed challenges
     */
    fun getSuggestedChallenges(
        allChallenges: List<Challenge>,
        completedChallengeIds: Set<Int>,
        maxSuggestions: Int = 3
    ): List<Challenge> {
        val incompleteChallenges = allChallenges.filter { it.id !in completedChallengeIds }

        // If user has completed no challenges, suggest some easy ones
        if (completedChallengeIds.isEmpty()) {
            return incompleteChallenges
                .filter { it.difficulty == DIFFICULTY_EASY }
                .take(maxSuggestions)
        }

        // Count completed challenges by difficulty
        val completedEasy = allChallenges.count {
            it.id in completedChallengeIds && it.difficulty == DIFFICULTY_EASY
        }
        val completedMedium = allChallenges.count {
            it.id in completedChallengeIds && it.difficulty == DIFFICULTY_MEDIUM
        }

        // Suggest challenges based on user progress
        return when {
            // If completed more than 2 medium, suggest some hard ones
            completedMedium >= 2 -> {
                incompleteChallenges
                    .filter { it.difficulty == DIFFICULTY_HARD }
                    .take(maxSuggestions)
            }
            // If completed more than 3 easy, suggest some medium ones
            completedEasy >= 3 -> {
                incompleteChallenges
                    .filter { it.difficulty == DIFFICULTY_MEDIUM }
                    .take(maxSuggestions)
            }
            // Otherwise suggest easy ones
            else -> {
                incompleteChallenges
                    .filter { it.difficulty == DIFFICULTY_EASY }
                    .take(maxSuggestions)
            }
        }
    }
}