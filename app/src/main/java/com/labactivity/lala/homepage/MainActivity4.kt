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

import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.AVAILABLECOURSEPAGE.MainActivity3
import com.labactivity.lala.AVAILABLECOURSEPAGE.Course
import com.labactivity.lala.PYTHONASSESMENT.PYTHONASSESMENT
import com.labactivity.lala.PYTHONASSESMENT.AllAssessmentsActivity
import com.labactivity.lala.PYTHONASSESMENT.AllInterviewsActivity
import com.labactivity.lala.SQLCOMPILER.SQLASSESSMENT
import com.labactivity.lala.SQLCOMPILER.AllSQLChallengesActivity
import com.labactivity.lala.JAVACOMPILER.JAVAASSESSMENT
import com.labactivity.lala.JAVACOMPILER.AllJavaChallengesActivity
import com.labactivity.lala.ProfileMainActivity5.ProfileMainActivity5
import com.labactivity.lala.PROGRESSPAGE.UserProgressActivity
import com.labactivity.lala.R
import com.labactivity.lala.SettingsActivity
import com.labactivity.lala.databinding.ActivityMain4Binding
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.UTILS.setupWithSafeNavigation
import com.labactivity.lala.UTILS.AnimationUtils.animateCardPress
import com.labactivity.lala.UTILS.AnimationUtils.slideUpFadeIn
import com.labactivity.lala.UTILS.AnimationUtils.animateItems
import me.relex.circleindicator.CircleIndicator2
import androidx.recyclerview.widget.PagerSnapHelper
import com.labactivity.lala.DAILYPROBLEMPAGE.DailyProblemViewModel
import com.labactivity.lala.PROGRESSPAGE.ProgressService
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class MainActivity4 : BaseActivity() {

    private lateinit var binding: ActivityMain4Binding
    private val dailyProblemViewModel: DailyProblemViewModel by viewModels()
    private val progressService = ProgressService()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // ==============================================
        // RECORD USER LOGIN
        // ==============================================
        recordUserLogin()

        // ==============================================
        // ANIMATE UI ELEMENTS ON SCREEN LOAD
        // ==============================================
        animateInitialLoad()

        // ==============================================
        // ALL COURSES BUTTON → punta sa MainActivity3
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
        // SETUP COURSE RECYCLER VIEW
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

        // ==============================================
        // SETUP DAILY PROBLEM OF THE DAY
        // ==============================================
        setupDailyProblem()
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
                        val result = courseId.contains("sql", ignoreCase = true)
                        if (result) {
                            Log.d("MainActivity4", "✅ Found SQL course: $courseId")
                        }
                        result
                    }

                    // Check if user is enrolled in Java
                    val hasJava = enrolledCourses.any { course ->
                        val courseId = course["courseId"] as? String ?: ""
                        val result = courseId.contains("java", ignoreCase = true)
                        if (result) {
                            Log.d("MainActivity4", "✅ Found Java course: $courseId")
                        }
                        result
                    }

                    Log.d("MainActivity4", "📊 Enrollment check complete: Python=$hasPython, SQL=$hasSQL, Java=$hasJava")
                    Log.d("MainActivity4", "📋 All enrolled courseIds: ${enrolledCourses.map { it["courseId"] }}")

                    // Show/Hide Technical Assessment (Python)
                    if (hasPython) {
                        val assessmentHeader = findViewById<View>(R.id.layoutAssessmentsHeader)
                        assessmentHeader.visibility = View.VISIBLE
                        val assessmentTitle = assessmentHeader.findViewById<TextView>(R.id.textSectionTitle)
                        assessmentTitle.text = "Technical Assessment"
                        binding.recyclerViewAssessments.visibility = View.VISIBLE

                        val assessmentViewAll = assessmentHeader.findViewById<TextView>(R.id.textViewAll)
                        PYTHONASSESMENT.TechnicalAssesment(
                            this,
                            binding.recyclerViewAssessments,
                            assessmentViewAll
                        )

                        assessmentViewAll.setOnClickListener {
                            val intent = Intent(this, AllAssessmentsActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        findViewById<View>(R.id.layoutAssessmentsHeader).visibility = View.GONE
                        binding.recyclerViewAssessments.visibility = View.GONE
                    }

                    // Show/Hide SQL Challenges
                    if (hasSQL) {
                        val sqlHeader = findViewById<View>(R.id.layoutSQLChallengesHeader)
                        sqlHeader.visibility = View.VISIBLE
                        val sqlTitle = sqlHeader.findViewById<TextView>(R.id.textSectionTitle)
                        sqlTitle.text = "SQL Challenges"
                        binding.recyclerViewSQLChallenges.visibility = View.VISIBLE

                        val sqlViewAll = sqlHeader.findViewById<TextView>(R.id.textViewAll)
                        SQLASSESSMENT.SQLTechnicalAssessment(
                            this,
                            binding.recyclerViewSQLChallenges,
                            sqlViewAll
                        )

                        sqlViewAll.setOnClickListener {
                            val intent = Intent(this, AllSQLChallengesActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        findViewById<View>(R.id.layoutSQLChallengesHeader).visibility = View.GONE
                        binding.recyclerViewSQLChallenges.visibility = View.GONE
                    }

                    // Show/Hide Java Challenges
                    if (hasJava) {
                        val javaHeader = findViewById<View>(R.id.layoutJavaChallengesHeader)
                        javaHeader.visibility = View.VISIBLE
                        val javaTitle = javaHeader.findViewById<TextView>(R.id.textSectionTitle)
                        javaTitle.text = "Java Challenges"
                        binding.recyclerViewJavaChallenges.visibility = View.VISIBLE

                        val javaViewAll = javaHeader.findViewById<TextView>(R.id.textViewAll)
                        JAVAASSESSMENT.JavaTechnicalAssessment(
                            this,
                            binding.recyclerViewJavaChallenges,
                            javaViewAll
                        )

                        javaViewAll.setOnClickListener {
                            val intent = Intent(this, AllJavaChallengesActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        findViewById<View>(R.id.layoutJavaChallengesHeader).visibility = View.GONE
                        binding.recyclerViewJavaChallenges.visibility = View.GONE
                    }

                    // Show/Hide Technical Interviews (Python)
                    if (hasPython) {
                        val interviewHeader = findViewById<View>(R.id.layoutInterviewsHeader)
                        interviewHeader.visibility = View.VISIBLE
                        val interviewTitle = interviewHeader.findViewById<TextView>(R.id.textSectionTitle)
                        interviewTitle.text = "Technical Interview"
                        binding.recyclerViewInterviews.visibility = View.VISIBLE

                        val interviewViewAll = interviewHeader.findViewById<TextView>(R.id.textViewAll)
                        PYTHONASSESMENT.TechnicalInterview(
                            this,
                            binding.recyclerViewInterviews,
                            interviewViewAll,
                            interviewViewAll
                        )

                        interviewViewAll.setOnClickListener {
                            val intent = Intent(this, AllInterviewsActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        findViewById<View>(R.id.layoutInterviewsHeader).visibility = View.GONE
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
    // SETUP DAILY PROBLEM OF THE DAY
    // ==============================================
    private fun setupDailyProblem() {
        // Initially hide the card
        binding.cardProblemOfDay.visibility = View.GONE

        lifecycleScope.launch {
            // Observe active problem
            launch {
                dailyProblemViewModel.activeProblem.collect { problem ->
                    if (problem != null) {
                        binding.cardProblemOfDay.visibility = View.VISIBLE
                        binding.textQuestion.text = problem.title
                        binding.textDescription.text = problem.description

                        // Format and set date
                        problem.createdAt?.let { timestamp ->
                            val date = timestamp.toDate()
                            val dayOfMonth = date.date
                            val suffix = when {
                                dayOfMonth in 11..13 -> "th"
                                dayOfMonth % 10 == 1 -> "st"
                                dayOfMonth % 10 == 2 -> "nd"
                                dayOfMonth % 10 == 3 -> "rd"
                                else -> "th"
                            }
                            val dateFormat = SimpleDateFormat("d'$suffix' MMMM", Locale.getDefault())
                            binding.textDate.text = dateFormat.format(date)
                        }

                        // Click to open problem
                        binding.cardProblemOfDay.setOnClickListener {
                            navigateToDailyProblem(problem.problemId, problem.courseId, problem.compilerType)
                        }

                        // Animate card appearance
                        binding.cardProblemOfDay.slideUpFadeIn(duration = 400, startDelay = 100)
                    } else {
                        binding.cardProblemOfDay.visibility = View.GONE
                    }
                }
            }

            // Observe countdown timer
            launch {
                dailyProblemViewModel.timeRemaining.collect { time ->
                    binding.textHours.text = String.format("%02d", time.hours)
                    binding.textMinutes.text = String.format("%02d", time.minutes)
                    binding.textSeconds.text = String.format("%02d", time.seconds)
                }
            }

            // Observe problem expiration
            launch {
                dailyProblemViewModel.isProblemExpired.collect { isExpired ->
                    if (isExpired) {
                        binding.cardProblemOfDay.visibility = View.GONE
                    }
                }
            }
        }
    }

    // ==============================================
    // NAVIGATE TO DAILY PROBLEM ACTIVITY
    // ==============================================
    private fun navigateToDailyProblem(problemId: String, courseId: String, compilerType: String) {
        Log.d("MainActivity4", "Navigate to daily problem: $problemId, type: $compilerType")

        val intent = Intent(this, com.labactivity.lala.DAILYPROBLEMPAGE.ProblemOfDayActivity::class.java).apply {
            putExtra(com.labactivity.lala.DAILYPROBLEMPAGE.ProblemOfDayActivity.EXTRA_PROBLEM_ID, problemId)
            putExtra(com.labactivity.lala.DAILYPROBLEMPAGE.ProblemOfDayActivity.EXTRA_COURSE_ID, courseId)
            putExtra(com.labactivity.lala.DAILYPROBLEMPAGE.ProblemOfDayActivity.EXTRA_COMPILER_TYPE, compilerType)
        }
        startActivity(intent)
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
                R.id.nav_settings to SettingsActivity::class.java,
                R.id.user_progress to UserProgressActivity::class.java
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
    // ==============================================
    private fun animateInitialLoad() {
        // Hide all elements initially
        binding.recyclerView.alpha = 0f
        binding.recyclerViewAssessments.alpha = 0f
        binding.recyclerViewSQLChallenges.alpha = 0f
        binding.recyclerViewJavaChallenges.alpha = 0f
        binding.recyclerViewInterviews.alpha = 0f
        binding.cardViewPractice.alpha = 0f

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

    // ==============================================
    // RECORD USER LOGIN FOR TRACKING
    // ==============================================
    private fun recordUserLogin() {
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
                Log.d("MainActivity4", "Login tracked for today")
            } else {
                Log.e("MainActivity4", "Failed to track login")
            }
        }
    }
}
