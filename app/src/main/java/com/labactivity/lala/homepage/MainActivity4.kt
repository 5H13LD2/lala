package com.labactivity.lala.homepage

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.labactivity.lala.AVAILABLECOURSEPAGE.MainActivity3
import com.labactivity.lala.AVAILABLECOURSEPAGE.Course  // Import the correct Course class
import com.labactivity.lala.PYTHONASSESMENT.PYTHONASSESMENT
import com.labactivity.lala.ProfileMainActivity5
import com.labactivity.lala.R
import com.labactivity.lala.SettingsActivity
import com.labactivity.lala.databinding.ActivityMain4Binding
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.UTILS.setupWithSafeNavigation
import java.util.Calendar

class MainActivity4 : BaseActivity() {

    private lateinit var binding: ActivityMain4Binding
    private lateinit var dayViews: Array<DayCircleView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // ==============================================
        // ALL COURSES BUTTON → punta sa MainActivity3
        // ==============================================
        binding.textAllPractice.setOnClickListener {
            Log.d("MainActivity4", "✅ textAllPractice clicked!")
            Toast.makeText(this, "Opening All Courses...", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)

            Log.d("MainActivity4", "➡️ Intent to MainActivity3 triggered!")
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
        updateDayStates()

        // ==============================================
        // SETUP COURSE RECYCLER VIEW (PYTHON, JAVA, SQL)
        // ==============================================
        setupRecyclerView()

        // ==============================================
        // SETUP BOTTOM NAVIGATION
        // ==============================================
        setupBottomNavigation()

        // ==============================================
        // BIND ASSESSMENTS TO RECYCLER VIEW
        // ==============================================
        PYTHONASSESMENT.TechnicalAssesment(
            this,
            binding.recyclerViewAssessments,
            binding.textViewAllAssessments
        )

        // ==============================================
        // BIND INTERVIEWS TO RECYCLER VIEW
        // ⚠️ HUWAG nang gamitin textAllPractice dito
        // ==============================================
        PYTHONASSESMENT.TechnicalInterview(
            this,
            binding.recyclerViewInterviews,
            binding.textViewAllInterviews,
            binding.textViewAllInterviews
        )
    }

    // ==============================================
    // TOGGLE DAY CLICK STATE
    // ==============================================
    private fun toggleDay(dayIndex: Int) {
        dayViews[dayIndex].setChecked(!dayViews[dayIndex].isChecked())
    }

    // ==============================================
    // UPDATE CURRENT DAY HIGHLIGHT
    // ==============================================
    private fun updateDayStates() {
        dayViews.forEach { it.setChecked(false) }

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
    // SETUP COURSE RECYCLERVIEW (HORIZONTAL)
    // ==============================================
    private fun setupRecyclerView() {
        val courseList = listOf(
            Course(
                courseId = "python_basics",
                name = "Python Basics",
                imageResId = R.drawable.python,
                description = "Learn Python programming from scratch",
                category = "Programming",
                difficulty = "Beginner"
            ),
            Course(
                courseId = "java_fundamentals",
                name = "Java Fundamentals",
                imageResId = R.drawable.java,
                description = "Master Java programming fundamentals",
                category = "Programming",
                difficulty = "Beginner"
            ),
            Course(
                courseId = "sql_basics",
                name = "SQL Basics",
                imageResId = R.drawable.sql,
                description = "Learn database management with SQL",
                category = "Database",
                difficulty = "Beginner"
            )
        )

        val recyclerView: RecyclerView = binding.recyclerView
        val textMyLibrary: TextView = binding.textMyLibrary
        textMyLibrary.paintFlags = textMyLibrary.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = CourseAdapter(courseList.toMutableList())
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
        // Refresh assessments to show updated status
        PYTHONASSESMENT.refreshChallenges(this, binding.recyclerViewAssessments)
    }
}
