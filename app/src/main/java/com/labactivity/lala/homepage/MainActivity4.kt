package com.labactivity.lala.homepage

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.labactivity.lala.UTILS.DialogUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.AVAILABLECOURSEPAGE.MainActivity3
import com.labactivity.lala.AVAILABLECOURSEPAGE.Course  // Import the correct Course class
import com.labactivity.lala.PYTHONASSESMENT.PYTHONASSESMENT
import com.labactivity.lala.PYTHONASSESMENT.AllAssessmentsActivity
import com.labactivity.lala.PYTHONASSESMENT.AllInterviewsActivity
import com.labactivity.lala.SQLCOMPILER.SQLASSESSMENT
import com.labactivity.lala.SQLCOMPILER.AllSQLChallengesActivity
import com.labactivity.lala.JAVACOMPILER.JAVAASSESSMENT
import com.labactivity.lala.JAVACOMPILER.AllJavaChallengesActivity
import com.labactivity.lala.ProfileMainActivity5.ProfileMainActivity5
import com.labactivity.lala.R
import com.labactivity.lala.SettingsActivity
import com.labactivity.lala.databinding.ActivityMain4Binding
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.UTILS.setupWithSafeNavigation
import com.labactivity.lala.UTILS.AnimationUtils.animateCardPress
import com.labactivity.lala.UTILS.AnimationUtils.slideUpFadeIn
import com.labactivity.lala.UTILS.AnimationUtils.animateItems
import com.labactivity.lala.UTILS.AnimationUtils.pulse
import com.labactivity.lala.UTILS.AnimationUtils.scaleIn
import com.labactivity.lala.UTILS.StreakManager
import com.labactivity.lala.UTILS.AssessmentResultTracker
import java.util.Calendar
import me.relex.circleindicator.CircleIndicator2
import androidx.recyclerview.widget.PagerSnapHelper

class MainActivity4 : BaseActivity() {

    private lateinit var binding: ActivityMain4Binding
    private lateinit var dayViews: Array<DayCircleView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // ==============================================
        // ANIMATE UI ELEMENTS ON SCREEN LOAD
        // ==============================================
        animateInitialLoad()

        // ==============================================
        // ALL COURSES BUTTON → punta sa MainActivity3
        // With smooth animation
        // ==============================================
        binding.textAllPractice.setOnClickListener {
            Log.d("MainActivity4", "✅ textAllPractice clicked!")

            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)

            Log.d("MainActivity4", "➡️ Intent to MainActivity3 triggered!")
        }

        // Add card press animation
        binding.cardViewPractice.animateCardPress {
            binding.textAllPractice.performClick()
        }

        // ==============================================
        // SETUP DAY CIRCLE VIEW (M T W TH F SAT SUN)
        // ==============================================
        dayViews = arrayOf(
            binding.dayMonday,
            binding.dayTuesday,
            binding.dayWednesday,
            binding.dayThursday,
            binding.dayFriday,
            binding.daySaturday,
            binding.daySunday
        )
        dayViews.forEachIndexed { index, view ->
            view.setOnClickListener { toggleDay(index) }
        }

        // ==============================================
        // SETUP STREAK BADGE
        // ==============================================
        setupStreakBadge()

        // ==============================================
        // UPDATE DAY STATES WITH QUIZ RESULTS
        // ==============================================
        updateDayStates()

        // ==============================================
        // SETUP COURSE RECYCLER VIEW (PYTHON, JAVA, SQL)
        // ==============================================
        setupRecyclerView()

        // ==============================================
        // CHECK ENROLLMENT AND SETUP ASSESSMENTS
        // ==============================================
        checkEnrollmentAndSetupSections()

        // ==============================================
        // SETUP BOTTOM NAVIGATION
        // ==============================================
        setupBottomNavigation()
    }

    // ==============================================
    // TOGGLE DAY CLICK STATE
    // ==============================================
    private fun toggleDay(dayIndex: Int) {
        dayViews[dayIndex].setChecked(!dayViews[dayIndex].isChecked())
    }

    // ==============================================
    // UPDATE CURRENT DAY HIGHLIGHT + QUIZ RESULTS
    // ==============================================
    private fun updateDayStates() {
        // Get quiz results for the week
        val weekResults = AssessmentResultTracker.getWeekResults(this)

        // Update each day with its status and water animation
        dayViews.forEachIndexed { index, view ->
            view.setChecked(false)

            val dailyResult = weekResults[index]
            if (dailyResult != null && dailyResult.totalCount > 0) {
                // Set water fill animation based on results
                view.setDayStatus(
                    status = dailyResult.getStatus(),
                    fillPercentage = dailyResult.getFillPercentage(),
                    animate = true
                )
            } else {
                // No activity for this day
                view.setDayStatus(DayStatus.NONE, 0f, animate = false)
            }
        }

        // Check current day
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val index = when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> -1
        }
        if (index != -1) dayViews[index].setChecked(true)
    }

    // ==============================================
    // SETUP STREAK BADGE
    // ==============================================
    private fun setupStreakBadge() {
        // Record activity (this updates streak if user hasn't been active today)
        val streak = StreakManager.recordActivity(this)

        // Update UI
        binding.textStreakCount.text = streak.toString()

        // Animate badge if milestone reached
        if (StreakManager.shouldAnimateStreak(streak)) {
            binding.iconStreakBadge.pulse(duration = 1000, repeatCount = 3)
            binding.streakBadgeContainer.scaleIn(duration = 500, startDelay = 200)
        }

        // Make badge clickable to show stats
        binding.streakBadgeContainer.setOnClickListener {
            showStreakStats()
        }
    }

    // ==============================================
    // SHOW STREAK STATISTICS DIALOG
    // ==============================================
    private fun showStreakStats() {
        val currentStreak = StreakManager.getCurrentStreak(this)
        val longestStreak = StreakManager.getLongestStreak(this)
        val totalDays = StreakManager.getTotalDaysActive(this)
        val totalAssessments = AssessmentResultTracker.getTotalAssessmentsTaken(this)
        val passedAssessments = AssessmentResultTracker.getTotalPassedAssessments(this)

        val message = """
            🔥 Current Streak: $currentStreak days
            🏆 Longest Streak: $longestStreak days
            📅 Total Active Days: $totalDays
            ✍️ Total Assessments: $totalAssessments
            ✅ Passed: $passedAssessments
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("Your Progress")
            .setMessage(message)
            .setPositiveButton("Keep Going!") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // ==============================================
    // SETUP COURSE RECYCLERVIEW (HORIZONTAL)
    // ==============================================
    private fun setupRecyclerView() {
        binding.textMyLibrary.paintFlags = binding.textMyLibrary.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Add click listener to "My Library" text
        binding.textMyLibrary.setOnClickListener {
            val intent = Intent(this, com.labactivity.lala.MYLIBRARY.MyLibraryActivity::class.java)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Auto-load only enrolled courses from Firestore with callback
        val adapter = CourseAdapter(autoLoadEnrolled = true, onCoursesLoaded = ::updateRecentCourseVisibility)
        binding.recyclerView.adapter = adapter

        // Add snap helper for page-like scrolling
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

        // Attach the indicator to the RecyclerView with SnapHelper
        binding.indicator.attachToRecyclerView(binding.recyclerView, snapHelper)

        // Register adapter data observer to update indicator when data changes
        adapter.registerAdapterDataObserver(binding.indicator.adapterDataObserver)
    }

    // ==============================================
    // UPDATE RECENT COURSE SECTION VISIBILITY
    // ==============================================
    private fun updateRecentCourseVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.layoutRecentCourseHeader.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.indicator.visibility = View.GONE
        } else {
            binding.layoutRecentCourseHeader.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE
            binding.indicator.visibility = View.VISIBLE
        }
    }

    // ==============================================
    // CHECK ENROLLMENT AND SETUP ASSESSMENT SECTIONS
    // ==============================================
    private fun checkEnrollmentAndSetupSections() {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        auth.currentUser?.let { user ->
            firestore.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val enrolledCourses = document.get("courseTaken") as? List<Map<String, Any>> ?: listOf()

                    // Check if user is enrolled in Python
                    val hasPython = enrolledCourses.any { course ->
                        val courseId = course["courseId"] as? String ?: ""
                        courseId.contains("python", ignoreCase = true)
                    }

                    // Check if user is enrolled in SQL
                    val hasSQL = enrolledCourses.any { course ->
                        val courseId = course["courseId"] as? String ?: ""
                        courseId.contains("sql", ignoreCase = true)
                    }

                    // Check if user is enrolled in Java
                    val hasJava = enrolledCourses.any { course ->
                        val courseId = course["courseId"] as? String ?: ""
                        courseId.contains("java", ignoreCase = true)
                    }

                    // Show/Hide Technical Assessment (Python)
                    if (hasPython) {
                        binding.textTechAssessmentTitle.visibility = View.VISIBLE
                        binding.textViewAllAssessments.visibility = View.VISIBLE
                        binding.recyclerViewAssessments.visibility = View.VISIBLE

                        PYTHONASSESMENT.TechnicalAssesment(
                            this,
                            binding.recyclerViewAssessments,
                            binding.textViewAllAssessments
                        )

                        binding.textViewAllAssessments.setOnClickListener {
                            val intent = Intent(this, AllAssessmentsActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        binding.textTechAssessmentTitle.visibility = View.GONE
                        binding.textViewAllAssessments.visibility = View.GONE
                        binding.recyclerViewAssessments.visibility = View.GONE
                    }

                    // Show/Hide SQL Challenges
                    if (hasSQL) {
                        binding.textSQLChallengesTitle.visibility = View.VISIBLE
                        binding.textViewAllSQLChallenges.visibility = View.VISIBLE
                        binding.recyclerViewSQLChallenges.visibility = View.VISIBLE

                        SQLASSESSMENT.SQLTechnicalAssessment(
                            this,
                            binding.recyclerViewSQLChallenges,
                            binding.textViewAllSQLChallenges
                        )

                        binding.textViewAllSQLChallenges.setOnClickListener {
                            val intent = Intent(this, AllSQLChallengesActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        binding.textSQLChallengesTitle.visibility = View.GONE
                        binding.textViewAllSQLChallenges.visibility = View.GONE
                        binding.recyclerViewSQLChallenges.visibility = View.GONE
                    }

                    // Show/Hide Java Challenges
                    if (hasJava) {
                        binding.textJavaChallengesTitle.visibility = View.VISIBLE
                        binding.textViewAllJavaChallenges.visibility = View.VISIBLE
                        binding.recyclerViewJavaChallenges.visibility = View.VISIBLE

                        JAVAASSESSMENT.JavaTechnicalAssessment(
                            this,
                            binding.recyclerViewJavaChallenges,
                            binding.textViewAllJavaChallenges
                        )

                        binding.textViewAllJavaChallenges.setOnClickListener {
                            val intent = Intent(this, AllJavaChallengesActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        binding.textJavaChallengesTitle.visibility = View.GONE
                        binding.textViewAllJavaChallenges.visibility = View.GONE
                        binding.recyclerViewJavaChallenges.visibility = View.GONE
                    }

                    // Show/Hide Technical Interviews (Python)
                    if (hasPython) {
                        binding.textInterviewsTitle.visibility = View.VISIBLE
                        binding.textViewAllInterviews.visibility = View.VISIBLE
                        binding.recyclerViewInterviews.visibility = View.VISIBLE

                        PYTHONASSESMENT.TechnicalInterview(
                            this,
                            binding.recyclerViewInterviews,
                            binding.textViewAllInterviews,
                            binding.textViewAllInterviews
                        )

                        binding.textViewAllInterviews.setOnClickListener {
                            val intent = Intent(this, AllInterviewsActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        binding.textInterviewsTitle.visibility = View.GONE
                        binding.textViewAllInterviews.visibility = View.GONE
                        binding.recyclerViewInterviews.visibility = View.GONE
                    }

                    Log.d("MainActivity4", "Enrollment check: Python=$hasPython, SQL=$hasSQL, Java=$hasJava")
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity4", "Error checking enrollment", e)
                }
        }
    }

    // ==============================================
    // SETUP BOTTOM NAVIGATION (USING UTILITY)
    // ==============================================
    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigationView

        bottomNavigationView.setupWithSafeNavigation(
            this,
            MainActivity4::class.java,
            mapOf(
                R.id.nav_home to MainActivity4::class.java,
                R.id.nav_profile to ProfileMainActivity5::class.java,
                R.id.nav_settings to SettingsActivity::class.java
            )
        )
    }

    // ==============================================
    // REFRESH ASSESSMENTS WHEN USER RETURNS
    // ==============================================
    override fun onResume() {
        super.onResume()
        // Re-check enrollment and refresh sections
        checkEnrollmentAndSetupSections()
    }

    // ==============================================
    // ANIMATION: INITIAL SCREEN LOAD
    // Animates all major sections with staggered delays
    // ==============================================
    private fun animateInitialLoad() {
        // Hide all elements initially
        binding.streakBadgeContainer.alpha = 0f
        binding.daySelectorContainer.alpha = 0f
        binding.recyclerView.alpha = 0f
        binding.recyclerViewAssessments.alpha = 0f
        binding.recyclerViewSQLChallenges.alpha = 0f
        binding.recyclerViewJavaChallenges.alpha = 0f
        binding.recyclerViewInterviews.alpha = 0f
        binding.cardViewPractice.alpha = 0f

        // Animate streak badge first
        binding.streakBadgeContainer.scaleIn(duration = 500, startDelay = 50)

        // Animate day selector with slide up
        binding.daySelectorContainer.slideUpFadeIn(duration = 400, startDelay = 150)

        // Animate Recent Course section
        binding.recyclerView.slideUpFadeIn(duration = 400, startDelay = 200)

        // Animate Technical Assessment section
        binding.recyclerViewAssessments.slideUpFadeIn(duration = 400, startDelay = 300)

        // Animate SQL Challenges section
        binding.recyclerViewSQLChallenges.slideUpFadeIn(duration = 400, startDelay = 350)

        // Animate Java Challenges section
        binding.recyclerViewJavaChallenges.slideUpFadeIn(duration = 400, startDelay = 380)

        // Animate Technical Interview section
        binding.recyclerViewInterviews.slideUpFadeIn(duration = 400, startDelay = 420)

        // Animate Practice Card
        binding.cardViewPractice.slideUpFadeIn(duration = 400, startDelay = 500)

        // Animate RecyclerView items after the view is laid out
        binding.recyclerView.post {
            binding.recyclerView.animateItems(itemDelay = 80, itemDuration = 300)
        }

        binding.recyclerViewAssessments.post {
            binding.recyclerViewAssessments.animateItems(itemDelay = 80, itemDuration = 300)
        }

        binding.recyclerViewSQLChallenges.post {
            binding.recyclerViewSQLChallenges.animateItems(itemDelay = 80, itemDuration = 300)
        }

        binding.recyclerViewJavaChallenges.post {
            binding.recyclerViewJavaChallenges.animateItems(itemDelay = 80, itemDuration = 300)
        }

        binding.recyclerViewInterviews.post {
            binding.recyclerViewInterviews.animateItems(itemDelay = 80, itemDuration = 300)
        }
    }
}
