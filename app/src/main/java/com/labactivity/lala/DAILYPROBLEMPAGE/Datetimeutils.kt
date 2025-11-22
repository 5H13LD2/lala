package com.labactivity.lala.DAILYPROBLEMPAGE

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    /**
     * Format timestamp to readable date string
     * Example: "22nd November"
     */
    fun formatDate(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val date = timestamp.toDate()
        val day = getDayWithSuffix(date)
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)

        return "$day $month"
    }

    /**
     * Format timestamp to full date string
     * Example: "22nd November 2024"
     */
    fun formatFullDate(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val date = timestamp.toDate()
        val day = getDayWithSuffix(date)
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)

        return "$day $monthYear"
    }

    /**
     * Get day with ordinal suffix
     * Example: 1st, 2nd, 3rd, 4th, etc.
     */
    private fun getDayWithSuffix(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val suffix = when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }

        return "$day$suffix"
    }

    /**
     * Calculate time remaining until expiration
     */
    fun calculateTimeRemaining(expiryTimestamp: Timestamp?): TimeRemaining {
        if (expiryTimestamp == null) return TimeRemaining()

        val currentTime = System.currentTimeMillis()
        val expiryTime = expiryTimestamp.toDate().time
        val difference = expiryTime - currentTime

        if (difference <= 0) {
            return TimeRemaining()
        }

        val hours = TimeUnit.MILLISECONDS.toHours(difference).toInt()
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(difference) % 60).toInt()
        val seconds = (TimeUnit.MILLISECONDS.toSeconds(difference) % 60).toInt()

        return TimeRemaining(hours, minutes, seconds)
    }

    /**
     * Check if timestamp has expired
     */
    fun isExpired(timestamp: Timestamp?): Boolean {
        if (timestamp == null) return true
        return timestamp.toDate().time <= System.currentTimeMillis()
    }

    /**
     * Format duration in milliseconds to readable string
     * Example: "2.5 seconds"
     */
    fun formatDuration(milliseconds: Long): String {
        return when {
            milliseconds < 1000 -> "${milliseconds}ms"
            milliseconds < 60000 -> String.format("%.1f seconds", milliseconds / 1000.0)
            else -> String.format("%.1f minutes", milliseconds / 60000.0)
        }
    }

    /**
     * Get relative time string
     * Example: "2 hours ago", "Yesterday", "Just now"
     */
    fun getRelativeTimeString(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        val difference = now - time

        return when {
            difference < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            difference < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
                "$minutes minute${if (minutes > 1) "s" else ""} ago"
            }
            difference < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(difference)
                "$hours hour${if (hours > 1) "s" else ""} ago"
            }
            difference < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(difference)
                when (days.toInt()) {
                    1 -> "Yesterday"
                    else -> "$days days ago"
                }
            }
            else -> {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp.toDate())
            }
        }
    }
}

data class TimeRemaining(
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0
) {
    fun isExpired(): Boolean = hours == 0 && minutes == 0 && seconds == 0

    fun toFormattedString(): String {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}