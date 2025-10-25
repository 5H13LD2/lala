package com.labactivity.lala.UTILS

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages user's coding streak - tracks consecutive days of app usage
 * and quiz/assessment completions
 */
object StreakManager {

    private const val PREFS_NAME = "streak_prefs"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_LONGEST_STREAK = "longest_streak"
    private const val KEY_LAST_ACTIVITY_DATE = "last_activity_date"
    private const val KEY_TOTAL_DAYS_ACTIVE = "total_days_active"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Call this when user opens the app or completes an activity
     * Returns the new streak count
     */
    fun recordActivity(context: Context): Int {
        val prefs = getPrefs(context)
        val today = getTodayDateString()
        val lastActivityDate = prefs.getString(KEY_LAST_ACTIVITY_DATE, "")

        return when {
            lastActivityDate == today -> {
                // Already recorded today, return current streak
                prefs.getInt(KEY_CURRENT_STREAK, 1)
            }
            isYesterday(lastActivityDate) -> {
                // Consecutive day - increment streak
                val newStreak = prefs.getInt(KEY_CURRENT_STREAK, 0) + 1
                val longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)
                val totalDays = prefs.getInt(KEY_TOTAL_DAYS_ACTIVE, 0) + 1

                prefs.edit().apply {
                    putInt(KEY_CURRENT_STREAK, newStreak)
                    putInt(KEY_LONGEST_STREAK, maxOf(newStreak, longestStreak))
                    putString(KEY_LAST_ACTIVITY_DATE, today)
                    putInt(KEY_TOTAL_DAYS_ACTIVE, totalDays)
                    apply()
                }
                newStreak
            }
            else -> {
                // Streak broken - reset to 1
                val totalDays = prefs.getInt(KEY_TOTAL_DAYS_ACTIVE, 0) + 1
                prefs.edit().apply {
                    putInt(KEY_CURRENT_STREAK, 1)
                    putString(KEY_LAST_ACTIVITY_DATE, today)
                    putInt(KEY_TOTAL_DAYS_ACTIVE, totalDays)
                    apply()
                }
                1
            }
        }
    }

    /**
     * Get current streak count
     */
    fun getCurrentStreak(context: Context): Int {
        val prefs = getPrefs(context)
        val lastActivityDate = prefs.getString(KEY_LAST_ACTIVITY_DATE, "")
        val today = getTodayDateString()

        // If last activity was yesterday or today, return current streak
        // Otherwise streak is broken (return 0)
        return if (lastActivityDate == today || isYesterday(lastActivityDate)) {
            prefs.getInt(KEY_CURRENT_STREAK, 0)
        } else {
            0
        }
    }

    /**
     * Get longest streak ever
     */
    fun getLongestStreak(context: Context): Int {
        return getPrefs(context).getInt(KEY_LONGEST_STREAK, 0)
    }

    /**
     * Get total days the user has been active
     */
    fun getTotalDaysActive(context: Context): Int {
        return getPrefs(context).getInt(KEY_TOTAL_DAYS_ACTIVE, 0)
    }

    /**
     * Check if streak should animate (new milestone reached)
     */
    fun shouldAnimateStreak(streak: Int): Boolean {
        return streak > 0 && (streak % 7 == 0 || streak in listOf(3, 5, 10, 30, 50, 100))
    }

    /**
     * Get today's date as a string (YYYY-MM-DD format)
     */
    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Check if given date string is yesterday
     */
    private fun isYesterday(dateString: String?): Boolean {
        if (dateString.isNullOrEmpty()) return false

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val givenDate = dateFormat.parse(dateString) ?: return false
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.time
            dateFormat.format(givenDate) == dateFormat.format(yesterday)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reset streak (for testing or user request)
     */
    fun resetStreak(context: Context) {
        getPrefs(context).edit().apply {
            putInt(KEY_CURRENT_STREAK, 0)
            putString(KEY_LAST_ACTIVITY_DATE, "")
            apply()
        }
    }
}
