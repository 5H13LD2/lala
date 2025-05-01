package com.labactivity.lala

import android.content.Context

/**
 * A utility class for testing quiz score functionality.
 * This can be used to simulate various quiz results without taking quizzes.
 */
class QuizScoreTestUtil(private val context: Context) {
    private val quizScoreManager = QuizScoreManager(context)
    
    /**
     * Sets test scores for various modules to test the scoring UI
     */
    fun setTestScores() {
        // Set a passing score for Module 1
        quizScoreManager.saveQuizScore("1", 10, 10)
        
        // Set a failing score for Module 2
        quizScoreManager.saveQuizScore("2", 3, 10)
        
        // Set a borderline passing score for Module 3
        quizScoreManager.saveQuizScore("3", 7, 10)
    }
    
    /**
     * Clears all quiz scores
     */
    fun clearAllScores() {
        quizScoreManager.clearAllScores()
    }
} 