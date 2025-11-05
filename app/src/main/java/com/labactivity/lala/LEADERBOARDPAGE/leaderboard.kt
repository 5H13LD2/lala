package com.labactivity.lala.LEADERBOARDPAGE

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.R

class Leaderboard : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private val userList = mutableListOf<User>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Back button logic
        findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(userList)
        recyclerView.adapter = adapter

        // Real-time leaderboard listener
        listenToUserLeaderboard()
    }

    private fun listenToUserLeaderboard() {
        firestore.collection("users")
            .orderBy("totalXP", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("Leaderboard", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    userList.clear()
                    for (doc in snapshots) {
                        val user = doc.toObject(User::class.java)
                        // Only add users with XP > 0 to leaderboard
                        if (user.totalXP > 0 || user.score > 0) {
                            userList.add(user)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
