package com.labactivity.lala

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.labactivity.lala.databinding.ActivityMain4Binding

class MainActivity4 : AppCompatActivity() {

    private lateinit var binding: ActivityMain4Binding
    private lateinit var dayViews: Array<DayCircleView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        dayViews = arrayOf(
            binding.dayMonday,
            binding.dayTuesday,
            binding.dayWednesday,
            binding.dayThursday,
            binding.dayFriday,
            binding.daySaturday,
            binding.daySunday
        )

        // Set click listeners for day buttons
        dayViews.forEachIndexed { index, view ->
            view.setOnClickListener { toggleDay(index) }
        }

        // Set initial states for days
        updateDayStates()

        // Setup RecyclerView to show courses
        setupRecyclerView()

        // Setup bottom navigation bar
        setupBottomNavigation()
    }

    private fun toggleDay(dayIndex: Int) {
        dayViews[dayIndex].setChecked(!dayViews[dayIndex].isChecked())
    }

    private fun updateDayStates() {
        for (i in dayViews.indices) {
            dayViews[i].setChecked(true)
        }
    }

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

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity4::class.java))
                    true
                }

                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileMainActivity5::class.java))
                    true
                }

                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                R.id.nav_notifications -> {
                    Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }
}
