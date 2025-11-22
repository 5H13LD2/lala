package com.labactivity.lala.PROGRESSPAGE

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.GAMIFICATION.XPManager
import com.labactivity.lala.ProfileMainActivity5.ProfileMainActivity5
import com.labactivity.lala.R
import com.labactivity.lala.SettingsActivity
import com.labactivity.lala.UTILS.setupWithSafeNavigation
import com.labactivity.lala.databinding.UserProgressBinding
import com.labactivity.lala.homepage.MainActivity4
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserProgressActivity : BaseActivity() {

    private lateinit var binding: UserProgressBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val progressService = ProgressService()
    private val xpManager = XPManager()

    private lateinit var calendarAdapters: Map<String, CalendarAdapter>

    companion object {
        private const val TAG = "UserProgressActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup back button
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup bottom navigation
        setupBottomNavigation()

        // Load XP data
        loadUserXPData()

        // Record login for today, then load progress
        recordLoginForToday()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.selectedItemId = R.id.user_progress

        bottomNavigationView.setupWithSafeNavigation(
            this,
            UserProgressActivity::class.java,
            mapOf(
                R.id.nav_home to MainActivity4::class.java,
                R.id.nav_profile to ProfileMainActivity5::class.java,
                R.id.nav_settings to SettingsActivity::class.java,
                R.id.user_progress to UserProgressActivity::class.java
            )
        )
    }

    private fun recordLoginForToday() {
        val userId = auth.currentUser?.uid ?: return

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val todayTimestamp = Timestamp(today)

        progressService.recordLogin(userId, todayTimestamp) { success ->
            if (success) {
                Log.d(TAG, "Login recorded successfully for today")
                // Load progress data after recording login
                loadUserProgress()
            } else {
                Log.e(TAG, "Failed to record login")
                // Still load progress even if recording fails
                loadUserProgress()
            }
        }
    }

    private fun loadUserProgress() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("login_tracking")
            .document("progress")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loginDates = document.get("loginDates") as? List<Timestamp> ?: emptyList()

                    // Update weekly progress
                    updateWeeklyProgress(loginDates)

                    // Update calendar view
                    setupCalendarView(loginDates)
                } else {
                    // No data yet, show empty state
                    updateWeeklyProgress(emptyList())
                    setupCalendarView(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading user progress", e)
            }
    }

    private fun updateWeeklyProgress(loginDates: List<Timestamp>) {
        val calendar = Calendar.getInstance()

        // Get current week's start (Monday) and end (Sunday)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY

        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val weekStart = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val weekEnd = calendar.time

        // Format date range
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val dateRange = "${dateFormat.format(weekStart)} - ${dateFormat.format(weekEnd)}"
        binding.tvDateRange.text = dateRange

        // Count logins this week
        val weekStatus = getWeekStatus(loginDates, weekStart)
        val achievedDays = weekStatus.getAchievedCount()

        binding.tvAchievement.text = "$achievedDays/7"

        // Update circle indicators
        updateWeekCircles(weekStatus)
    }

    private fun getWeekStatus(loginDates: List<Timestamp>, weekStart: Date): WeekStatus {
        val calendar = Calendar.getInstance()
        calendar.time = weekStart

        val weekDays = mutableListOf<Date>()
        for (i in 0..6) {
            weekDays.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return WeekStatus(
            monday = hasLoginOnDate(loginDates, weekDays[0]),
            tuesday = hasLoginOnDate(loginDates, weekDays[1]),
            wednesday = hasLoginOnDate(loginDates, weekDays[2]),
            thursday = hasLoginOnDate(loginDates, weekDays[3]),
            friday = hasLoginOnDate(loginDates, weekDays[4]),
            saturday = hasLoginOnDate(loginDates, weekDays[5]),
            sunday = hasLoginOnDate(loginDates, weekDays[6])
        )
    }

    private fun hasLoginOnDate(loginDates: List<Timestamp>, targetDate: Date): Boolean {
        val targetCal = Calendar.getInstance().apply { time = targetDate }

        return loginDates.any { timestamp ->
            val loginCal = Calendar.getInstance().apply { time = timestamp.toDate() }
            loginCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
            loginCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
        }
    }

    private fun updateWeekCircles(weekStatus: WeekStatus) {
        binding.circleMonday.setBackgroundResource(
            if (weekStatus.monday) R.drawable.circle_active else R.drawable.circle_inactive
        )
        binding.circleTuesday.setBackgroundResource(
            if (weekStatus.tuesday) R.drawable.circle_active else R.drawable.circle_inactive
        )
        binding.circleWednesday.setBackgroundResource(
            if (weekStatus.wednesday) R.drawable.circle_active else R.drawable.circle_inactive
        )
        binding.circleThursday.setBackgroundResource(
            if (weekStatus.thursday) R.drawable.circle_active else R.drawable.circle_inactive
        )
        binding.circleFriday.setBackgroundResource(
            if (weekStatus.friday) R.drawable.circle_active else R.drawable.circle_inactive
        )
        binding.circleSaturday.setBackgroundResource(
            if (weekStatus.saturday) R.drawable.circle_active else R.drawable.circle_inactive
        )
        binding.circleSunday.setBackgroundResource(
            if (weekStatus.sunday) R.drawable.circle_active else R.drawable.circle_inactive
        )
    }

    private fun setupCalendarView(loginDates: List<Timestamp>) {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Create adapters for past 4 months
        calendarAdapters = mapOf(
            "august" to setupMonthCalendar(binding.rvAugust, currentYear, currentMonth - 3, loginDates),
            "september" to setupMonthCalendar(binding.rvSeptember, currentYear, currentMonth - 2, loginDates),
            "october" to setupMonthCalendar(binding.rvOctober, currentYear, currentMonth - 1, loginDates),
            "november" to setupMonthCalendar(binding.rvNovember, currentYear, currentMonth, loginDates)
        )
    }

    private fun setupMonthCalendar(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        year: Int,
        month: Int,
        loginDates: List<Timestamp>
    ): CalendarAdapter {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        // Adjust year if month is negative
        while (calendar.get(Calendar.MONTH) < 0) {
            calendar.add(Calendar.YEAR, -1)
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val days = mutableListOf<CalendarDay?>()

        // Add empty days for alignment (Sunday = 1, Saturday = 7)
        val emptyDays = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - Calendar.SUNDAY
        repeat(emptyDays) {
            days.add(null)
        }

        // Add actual days
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dayTimestamp = Timestamp(calendar.time)
            val isActive = hasLoginOnDate(loginDates, calendar.time)

            days.add(
                CalendarDay(
                    timestamp = dayTimestamp,
                    isActive = isActive,
                    dayOfMonth = day,
                    month = calendar.get(Calendar.MONTH),
                    year = calendar.get(Calendar.YEAR)
                )
            )
        }

        val adapter = CalendarAdapter(days)
        recyclerView.layoutManager = GridLayoutManager(this, 7)
        recyclerView.adapter = adapter

        return adapter
    }

    private fun loadUserXPData() {
        lifecycleScope.launch {
            try {
                val xpData = xpManager.getUserXPData()
                if (xpData != null) {
                    // Update last 30 days XP (for now showing total)
                    binding.tvXpLast30Days.text = "${xpData.totalXP} XP"

                    // Update today's XP (placeholder - would need to track daily XP separately)
                    binding.tvXpToday.text = "0 XP"

                    // Update timeline dates
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())

                    // Start date (30 days ago)
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    binding.tvTimelineStart.text = dateFormat.format(calendar.time)

                    // End date (today)
                    calendar.add(Calendar.DAY_OF_YEAR, 30)
                    binding.tvTimelineEnd.text = "Today"

                    Log.d(TAG, "XP Data loaded: Total XP = ${xpData.totalXP}, Level = ${xpData.level}")
                } else {
                    Log.w(TAG, "No XP data found for user")
                    binding.tvXpLast30Days.text = "0 XP"
                    binding.tvXpToday.text = "0 XP"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading XP data", e)
                binding.tvXpLast30Days.text = "0 XP"
                binding.tvXpToday.text = "0 XP"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when user returns
        loadUserProgress()
        loadUserXPData()
    }
}
