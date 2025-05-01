package com.labactivity.lala

import android.content.Context
import android.content.SharedPreferences

class QuizScoreManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        QUIZ_SCORES_PREFS, Context.MODE_PRIVATE
    )

    fun saveQuizScore(moduleId: String, score: Int, totalQuestions: Int) {
        sharedPreferences.edit()
            .putInt("${moduleId}_score", score)
            .putInt("${moduleId}_total", totalQuestions)
            .apply()
    }

    fun getQuizScore(moduleId: String): Pair<Int, Int>? {
        val score = sharedPreferences.getInt("${moduleId}_score", -1)
        val total = sharedPreferences.getInt("${moduleId}_total", -1)
        
        return if (score == -1 || total == -1) {
            null // No score saved yet
        } else {
            Pair(score, total)
        }
    }

    fun isPassing(moduleId: String): Boolean {
        val scorePair = getQuizScore(moduleId) ?: return false
        val (score, total) = scorePair
        
        // Consider 70% as passing grade (can be adjusted)
        return score >= (total * 0.7).toInt()
    }

    fun clearAllScores() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val QUIZ_SCORES_PREFS = "quiz_scores_preferences"
    }
} 