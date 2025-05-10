package com.labactivity.lala

import com.labactivity.lala.Challenge
import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.labactivity.lala.databinding.ActivityMain4Binding
import java.util.Calendar

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

        dayViews.forEachIndexed { index, view ->
            view.setOnClickListener { toggleDay(index) }
        }

        updateDayStates()
        setupRecyclerView()
        setupBottomNavigation()
        setupTechnicalAssessmentSection()
    }

    private fun toggleDay(dayIndex: Int) {
        dayViews[dayIndex].setChecked(!dayViews[dayIndex].isChecked())
    }

    private fun updateDayStates() {
        for (i in dayViews.indices) {
            dayViews[i].setChecked(false)
        }

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

        if (index != -1) {
            dayViews[index].setChecked(true)
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

    private fun setupTechnicalAssessmentSection() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAssessments)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.right = itemSpacing
                outRect.left = itemSpacing
            }
        })

        // Define your Python challenges with proper structure
        val challenges = listOf(
            Challenge(
                id = 1,
                title = "Fix the Loop",
                difficulty = "Easy",
                brokenCode = """
                # Task: Print 'Hello, TechLauncher!' 5 times.
                # Bug: Loop is not correctly formatted.
                
                for i in range(5):
                    print(f"Line {}: Hello, TechLauncher!")
            """.trimIndent(),
                correctOutput = """
                Line 0: Hello, TechLauncher!
                Line 1: Hello, TechLauncher!
                Line 2: Hello, TechLauncher!
                Line 3: Hello, TechLauncher!
                Line 4: Hello, TechLauncher!
            """.trimIndent(),
                hint = "Maybe the variable is missing inside the f-string."
            ),
            Challenge(
                id = 2,
                title = "Debug If Statement",
                difficulty = "Easy",
                brokenCode = """
                # Task: Check if the number is positive, negative, or zero
                # Bug: The if-else structure has an issue
                
                num = 15
                
                if num > 0
                    print("The number is positive")
                elif num < 0:
                    print("The number is negative")
                else:
                    print("The number is zero")
            """.trimIndent(),
                correctOutput = "The number is positive",
                hint = "Check the syntax of the if statement. Is it properly terminated?"
            ),
            Challenge(
                id = 3,
                title = "Fix Function Call",
                difficulty = "Medium",
                brokenCode = """
                # Task: Calculate the factorial of a number
                # Bug: Function call is incorrect
                
                def factorial(n):
                    if n == 0 or n == 1:
                        return 1
                    else:
                        return n * factorial(n-1)
                
                number = 5
                result = factorial[number]
                print(f"The factorial of {number} is {result}")
            """.trimIndent(),
                correctOutput = "The factorial of 5 is 120",
                hint = "How do you call a function in Python? [] is for different data structures."
            )
            // You can add more challenges here from your list
        )

        // If you want to use the simple challenges instead (without Python code), use this instead:
        // val challenges = listOf(
        //    Challenge("Challenge One", "Easy"),
        //    Challenge("Challenge Two", "Medium"),
        //    Challenge("Challenge Three", "Hard")
        // )

        val adapter = TechnicalAssessmentAdapter(this, challenges)
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.textViewAllAssessments).setOnClickListener {
            Toast.makeText(this, "View all assessments", Toast.LENGTH_SHORT).show()
        }
    }

}
