package com.labactivity.lala

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class MainActivity4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        // 🔹 Dummy Data for Courses
        val courseList = listOf(
            Course("Python Basics", R.drawable.logo2),
            Course("Android Development", R.drawable.logo2),
            Course("Data Structures", R.drawable.logo2)
        )

        // 🔹 Find RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val textMyLibrary = findViewById<TextView>(R.id.textMyLibrary)
        textMyLibrary.paintFlags = textMyLibrary.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // 🔹 Set Layout Manager
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 🔹 Set Adapter
        recyclerView.adapter = CourseAdapter(courseList)

        // 🔹 Setup Bottom Navigation View
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_notifications -> {
                    Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // 🔹 Floating Action Button (Leaderboard)
        val fabLeaderboard = findViewById<FloatingActionButton>(R.id.fabLeaderboard)
        fabLeaderboard.setOnClickListener {
            val intent = Intent(this@MainActivity4, LeaderboardAdapterActivity::class.java)
            startActivity(intent)
        }

        // 🔥 Enable Dragging for FloatingActionButton
        fabLeaderboard.setOnTouchListener(object : View.OnTouchListener {
            var dX = 0f
            var dY = 0f

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                    }

                    MotionEvent.ACTION_MOVE -> {
                        view.x = event.rawX + dX
                        view.y = event.rawY + dY
                    }
                }
                return true
            }
        })

        // 🔹 Hanapin ang TextView para sa kasalukuyang araw
        val textCurrentDay = findViewById<TextView>(R.id.textCurrentDay)

        // 🔹 Kunin ang araw ngayon at i-set sa TextView
        val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        textCurrentDay.text = "$currentDay GRIND!"
    }
}
