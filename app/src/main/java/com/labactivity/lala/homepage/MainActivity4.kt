package com.labactivity.lala.homepage


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
import com.labactivity.lala.AVAILABALECOURSEPAGE.CourseAdapter
import com.labactivity.lala.AVAILABALECOURSEPAGE.MainActivity3
import com.labactivity.lala.PYTHONASSESMENT.Challenge
import com.labactivity.lala.FLASHCARD.FlashcardTopic
import com.labactivity.lala.ProfileMainActivity5
import com.labactivity.lala.R
import com.labactivity.lala.SettingsActivity
import com.labactivity.lala.PYTHONASSESMENT.TechnicalAssessmentAdapter
import com.labactivity.lala.PYTHONASSESMENT.TechnicalInterviewAdapter
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
        setupTechnicalInterviewSection() // Added this line to set up the Technical Interview section
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
                correctOutput = "The number is positive ",
                hint = "I-Check mo yung syntax of the if statement.tignan mo kung may kulang!!!?"
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
                print(f"The factorial of {number} is {result} \n\nCongrats you solved the problem!")
            """.trimIndent(),
                correctOutput = "The factorial of 5 is 120",
                hint = "pano tawagin yung function sa python? [] ganyan ba?."
            )
        )

        val adapter = TechnicalAssessmentAdapter(this, challenges)
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.textViewAllAssessments).setOnClickListener {
            Toast.makeText(this, "View all assessments", Toast.LENGTH_SHORT).show()
        }

        binding.textAllPractice.setOnClickListener {
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
        }
    }

    // Added the setupTechnicalInterviewSection function
    private fun setupTechnicalInterviewSection() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInterviews)

        // Set up layout manager
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        // Add spacing between items
        val itemSpacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.right = itemSpacing
                outRect.left = itemSpacing
            }
        })

        // Create interview topics
        val topics = listOf(
            FlashcardTopic(1, "Programming Basics", "Beginner", emptyList()),
            FlashcardTopic(2, "Data Structures", "Intermediate", emptyList()),
            FlashcardTopic(3, "Algorithms", "Advanced", emptyList()),
            FlashcardTopic(4, "Object-Oriented Programming", "Intermediate", emptyList())
        )

        // Set adapter
        val adapter = TechnicalInterviewAdapter(this, topics)
        recyclerView.adapter = adapter

        // Set click listener for "View All" text
        findViewById<TextView>(R.id.textViewAllInterviews).setOnClickListener {
            Toast.makeText(this, "View all interview topics", Toast.LENGTH_SHORT).show()
            // Optionally, launch an activity that shows all topics
        }
    }
}