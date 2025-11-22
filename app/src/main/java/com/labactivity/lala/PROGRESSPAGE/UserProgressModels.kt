package com.labactivity.lala.PROGRESSPAGE

import com.google.firebase.Timestamp

/**
 * Represents a user's login history and weekly progress
 */
data class UserProgress(
    val userId: String = "",
    val loginDates: List<Timestamp> = emptyList(), // All login dates
    val lastLoginDate: Timestamp? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyTarget: Int = 7, // Default: login all 7 days
    val weeklyAchievement: Int = 0 // Number of days logged in this week
)

/**
 * Represents a single day in the calendar view
 */
data class CalendarDay(
    val timestamp: Timestamp,
    val isActive: Boolean = false, // User logged in on this day
    val dayOfMonth: Int,
    val month: Int,
    val year: Int
)

/**
 * Represents a week's login status
 */
data class WeekStatus(
    val monday: Boolean = false,
    val tuesday: Boolean = false,
    val wednesday: Boolean = false,
    val thursday: Boolean = false,
    val friday: Boolean = false,
    val saturday: Boolean = false,
    val sunday: Boolean = false
) {
    fun getAchievedCount(): Int {
        return listOf(monday, tuesday, wednesday, thursday, friday, saturday, sunday).count { it }
    }
}
