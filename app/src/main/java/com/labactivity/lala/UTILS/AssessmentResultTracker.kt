package com.labactivity.lala.UTILS

import android.content.Context
import android.content.SharedPreferences
import com.labactivity.lala.homepage.DailyResult
import com.labactivity.lala.homepage.DayStatus
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tracks quiz/assessment results per day for calendar visualization
 */
object AssessmentResultTracker {

    private const val PREFS_NAME = "assessment_results"
    private const val KEY_RESULTS_JSON = "results_json"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Record an assessment result
     * @param passed true if assessment was passed, false if failed
     */
    fun recordResult(context: Context, passed: Boolean, date: String = getTodayDateString()) {
        val prefs = getPrefs(context)
        val resultsJson = prefs.getString(KEY_RESULTS_JSON, "{}") ?: "{}"
        val results = JSONObject(resultsJson)

        // Get or create today's result
        val todayResult = if (results.has(date)) {
            val dayData = results.getJSONObject(date)
            DailyResult(
                date = date,
                passedCount = dayData.optInt("passed", 0),
                failedCount = dayData.optInt("failed", 0),
                totalCount = dayData.optInt("total", 0)
            )
        } else {
            DailyResult(date = date)
        }

        // Update counts
        val updatedResult = DailyResult(
            date = date,
            passedCount = todayResult.passedCount + if (passed) 1 else 0,
            failedCount = todayResult.failedCount + if (!passed) 1 else 0,
            totalCount = todayResult.totalCount + 1
        )

        // Save back to JSON
        val dayData = JSONObject().apply {
            put("passed", updatedResult.passedCount)
            put("failed", updatedResult.failedCount)
            put("total", updatedResult.totalCount)
        }
        results.put(date, dayData)

        prefs.edit().putString(KEY_RESULTS_JSON, results.toString()).apply()

        // Also record activity for streak
        StreakManager.recordActivity(context)
    }

    /**
     * Get result for a specific date
     */
    fun getResultForDate(context: Context, date: String): DailyResult {
        val prefs = getPrefs(context)
        val resultsJson = prefs.getString(KEY_RESULTS_JSON, "{}") ?: "{}"
        val results = JSONObject(resultsJson)

        return if (results.has(date)) {
            val dayData = results.getJSONObject(date)
            DailyResult(
                date = date,
                passedCount = dayData.optInt("passed", 0),
                failedCount = dayData.optInt("failed", 0),
                totalCount = dayData.optInt("total", 0)
            )
        } else {
            DailyResult(date = date)
        }
    }

    /**
     * Get results for current week (Mon-Sun)
     */
    fun getWeekResults(context: Context): Map<Int, DailyResult> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Calculate Monday of current week
        val daysToMonday = when (dayOfWeek) {
            Calendar.SUNDAY -> 6
            else -> dayOfWeek - Calendar.MONDAY
        }
        calendar.add(Calendar.DAY_OF_YEAR, -daysToMonday)

        val weekResults = mutableMapOf<Int, DailyResult>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Get results for each day (Monday = 0, Sunday = 6)
        for (i in 0..6) {
            val date = dateFormat.format(calendar.time)
            weekResults[i] = getResultForDate(context, date)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return weekResults
    }

    /**
     * Get status for a specific day index (0=Monday, 6=Sunday)
     */
    fun getStatusForDayIndex(context: Context, dayIndex: Int): DayStatus {
        val weekResults = getWeekResults(context)
        return weekResults[dayIndex]?.getStatus() ?: DayStatus.NONE
    }

    /**
     * Clear all results (for testing)
     */
    fun clearResults(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    /**
     * Get today's date as string
     */
    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Get total assessments taken
     */
    fun getTotalAssessmentsTaken(context: Context): Int {
        val prefs = getPrefs(context)
        val resultsJson = prefs.getString(KEY_RESULTS_JSON, "{}") ?: "{}"
        val results = JSONObject(resultsJson)

        var total = 0
        results.keys().forEach { date ->
            val dayData = results.getJSONObject(date)
            total += dayData.optInt("total", 0)
        }
        return total
    }

    /**
     * Get total passed assessments
     */
    fun getTotalPassedAssessments(context: Context): Int {
        val prefs = getPrefs(context)
        val resultsJson = prefs.getString(KEY_RESULTS_JSON, "{}") ?: "{}"
        val results = JSONObject(resultsJson)

        var total = 0
        results.keys().forEach { date ->
            val dayData = results.getJSONObject(date)
            total += dayData.optInt("passed", 0)
        }
        return total
    }
}
