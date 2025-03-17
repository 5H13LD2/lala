package com.labactivity.lala

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // 🔹 Sample Data
        val users = listOf(
            LeaderboardUser("Jerico", 100),
            LeaderboardUser("Mike", 90),
            LeaderboardUser("Anna", 85),
            LeaderboardUser("John", 80),
            LeaderboardUser("Sarah", 75)
        )

        // 🔹 Hanapin ang RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 🔹 Set Adapter
        val adapter = LeaderboardAdapter(users)
        recyclerView.adapter = adapter

        // 🔹 Log para makita kung may laman ang users list
        Log.d("LeaderboardData", "Users: $users")
    }
}
