package com.labactivity.lala.PROGRESSPAGE

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProgressService {

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "ProgressService"
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_LOGIN = "login_tracking"
        private const val DOCUMENT_PROGRESS = "progress"
    }

    /**
     * Records a login for the specified date
     * Uses Firestore security rules defined in firestore.rules
     */
    fun recordLogin(userId: String, loginDate: Timestamp, onComplete: (Boolean) -> Unit) {
        val progressRef = firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_LOGIN)
            .document(DOCUMENT_PROGRESS)

        // First, check if this date already exists
        progressRef.get()
            .addOnSuccessListener { document ->
                val existingDates = document.get("loginDates") as? List<Timestamp> ?: emptyList()

                // Check if today is already recorded
                val alreadyRecorded = existingDates.any { existingDate ->
                    isSameDay(existingDate.toDate(), loginDate.toDate())
                }

                if (alreadyRecorded) {
                    Log.d(TAG, "Login already recorded for this date")
                    onComplete(true)
                    return@addOnSuccessListener
                }

                // Add the new login date
                progressRef.update(
                    mapOf(
                        "loginDates" to FieldValue.arrayUnion(loginDate),
                        "lastLoginDate" to loginDate
                    )
                ).addOnSuccessListener {
                    Log.d(TAG, "Login date added successfully")
                    updateStreakData(userId)
                    onComplete(true)
                }.addOnFailureListener { e ->
                    // Document might not exist, create it
                    if (e.message?.contains("NOT_FOUND") == true) {
                        createProgressDocument(userId, loginDate, onComplete)
                    } else {
                        Log.e(TAG, "Error updating login date", e)
                        onComplete(false)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking existing dates", e)
                // Try to create the document
                createProgressDocument(userId, loginDate, onComplete)
            }
    }

    private fun createProgressDocument(userId: String, loginDate: Timestamp, onComplete: (Boolean) -> Unit) {
        val progressData = hashMapOf(
            "userId" to userId,
            "loginDates" to listOf(loginDate),
            "lastLoginDate" to loginDate,
            "currentStreak" to 1,
            "longestStreak" to 1,
            "weeklyTarget" to 7,
            "weeklyAchievement" to 1
        )

        firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_LOGIN)
            .document(DOCUMENT_PROGRESS)
            .set(progressData)
            .addOnSuccessListener {
                Log.d(TAG, "Progress document created successfully")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating progress document", e)
                onComplete(false)
            }
    }

    private fun updateStreakData(userId: String) {
        val progressRef = firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_LOGIN)
            .document(DOCUMENT_PROGRESS)

        progressRef.get()
            .addOnSuccessListener { document ->
                val loginDates = (document.get("loginDates") as? List<Timestamp> ?: emptyList())
                    .map { it.toDate() }
                    .sortedDescending()

                if (loginDates.isEmpty()) return@addOnSuccessListener

                // Calculate current streak
                val currentStreak = calculateCurrentStreak(loginDates)
                val longestStreak = calculateLongestStreak(loginDates)

                // Calculate weekly achievement
                val weeklyAchievement = calculateWeeklyAchievement(loginDates)

                progressRef.update(
                    mapOf(
                        "currentStreak" to currentStreak,
                        "longestStreak" to longestStreak,
                        "weeklyAchievement" to weeklyAchievement
                    )
                ).addOnSuccessListener {
                    Log.d(TAG, "Streak data updated: current=$currentStreak, longest=$longestStreak, weekly=$weeklyAchievement")
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error updating streak data", e)
                }
            }
    }

    private fun calculateCurrentStreak(sortedDates: List<Date>): Int {
        if (sortedDates.isEmpty()) return 0

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var streak = 0
        var checkDate = today.clone() as Calendar

        for (date in sortedDates) {
            val loginCal = Calendar.getInstance().apply { time = date }

            if (isSameDay(loginCal.time, checkDate.time)) {
                streak++
                checkDate.add(Calendar.DAY_OF_YEAR, -1)
            } else if (loginCal.before(checkDate.time)) {
                break
            }
        }

        return streak
    }

    private fun calculateLongestStreak(sortedDates: List<Date>): Int {
        if (sortedDates.isEmpty()) return 0

        var longestStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val prevCal = Calendar.getInstance().apply { time = sortedDates[i - 1] }
            val currCal = Calendar.getInstance().apply { time = sortedDates[i] }

            // Check if dates are consecutive
            prevCal.add(Calendar.DAY_OF_YEAR, -1)

            if (isSameDay(prevCal.time, currCal.time)) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return longestStreak
    }

    private fun calculateWeeklyAchievement(loginDates: List<Date>): Int {
        val calendar = Calendar.getInstance()

        // Get current week's start (Monday)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY

        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val weekStart = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val weekEnd = calendar.time

        return loginDates.count { date ->
            date.after(weekStart) && date.before(weekEnd) || isSameDay(date, weekStart)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}