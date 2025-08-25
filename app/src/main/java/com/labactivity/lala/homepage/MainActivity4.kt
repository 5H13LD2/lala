package com.labactivity.lala.homepage

import android.graphics.Paint
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.labactivity.lala.PYTHONASSESMENT.PYTHONASSESMENT
import com.labactivity.lala.ProfileMainActivity5
import com.labactivity.lala.R
import com.labactivity.lala.SettingsActivity
import com.labactivity.lala.databinding.ActivityMain4Binding
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.UTILS.setupWithSafeNavigation   // ✅ IMPORT UTILITY
import java.util.Calendar

class MainActivity4 : BaseActivity() {   // ✅ INHERIT FROM BASEACTIVITY

    private lateinit var binding: ActivityMain4Binding
    private lateinit var dayViews: Array<DayCircleView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)

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
        dayViews.forEachIndexed { index, view -> view.setOnClickListener { toggleDay(index) } }
        updateDayStates()

        // ==============================================
        // SETUP COURSE RECYCLER VIEW (PYTHON, JAVA, SQL)
        // ==============================================
        setupRecyclerView()

        // ==============================================
        // SETUP BOTTOM NAVIGATION WITH SAFE NAVIGATION
        // ==============================================
        setupBottomNavigation()

        // ==============================================
        // BIND ASSESSMENTS TO RECYCLER VIEW
        // ==============================================
        PYTHONASSESMENT.TechnicalAssesment(
            this,
            findViewById(R.id.recyclerViewAssessments),
            findViewById(R.id.textViewAllAssessments)
        )

        // ==============================================
        // BIND INTERVIEWS TO RECYCLER VIEW
        // ==============================================
        PYTHONASSESMENT.TechnicalInterview(
            this,
            findViewById(R.id.recyclerViewInterviews),
            binding.textAllPractice,
            findViewById(R.id.textViewAllInterviews)
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
            Course("Python Basics", R.drawable.python),
            Course("Java Fundamentals", R.drawable.java),
            Course("SQL Basics", R.drawable.sql)
        )

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val textMyLibrary = findViewById<TextView>(R.id.textMyLibrary)
        textMyLibrary.paintFlags = textMyLibrary.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = CourseAdapter(courseList)
    }

    // ==============================================
    // SETUP BOTTOM NAVIGATION (USING UTILITY)
    // ==============================================
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setupWithSafeNavigation(
            this,
            MainActivity4::class.java,   // ✅ CURRENT ACTIVITY
            mapOf(
                R.id.nav_home to MainActivity4::class.java,
                R.id.nav_profile to ProfileMainActivity5::class.java,
                R.id.nav_settings to SettingsActivity::class.java
                // R.id.nav_notifications -> WALA PANG TARGET ACTIVITY
            )
        )
    }
}
