package com.labactivity.lala.LEADERBOARDPAGE

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.R

class Leaderboard : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private lateinit var ineligibleLayout: LinearLayout
    private lateinit var currentXpText: TextView
    private lateinit var requiredXpText: TextView
    private val userList = mutableListOf<User>()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Back button logic
        findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        ineligibleLayout = findViewById(R.id.ineligibleLayout)
        currentXpText = findViewById(R.id.currentXpText)
        requiredXpText = findViewById(R.id.requiredXpText)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(userList)
        recyclerView.adapter = adapter

        // Check current user eligibility first
        checkCurrentUserEligibility()
    }

    private fun checkCurrentUserEligibility() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("Leaderboard", "No authenticated user")
            showIneligibleScreen(0)
            return
        }

        // Check current user's XP
        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val totalXP = (document.getLong("totalXP") ?: 0).toInt()

                    if (totalXP >= 500) {
                        // User is eligible, show leaderboard
                        Log.d("Leaderboard", "User eligible with $totalXP XP")
                        showLeaderboard()
                    } else {
                        // User is not eligible, show blocked screen
                        Log.d("Leaderboard", "User not eligible with $totalXP XP")
                        showIneligibleScreen(totalXP)
                    }
                } else {
                    Log.w("Leaderboard", "No user document found")
                    showIneligibleScreen(0)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Leaderboard", "Error checking eligibility", e)
                showIneligibleScreen(0)
            }
    }

    private fun showLeaderboard() {
        recyclerView.visibility = View.VISIBLE
        ineligibleLayout.visibility = View.GONE
        // Load leaderboard data
        listenToUserLeaderboard()
    }

    private fun showIneligibleScreen(currentXP: Int) {
        recyclerView.visibility = View.GONE
        ineligibleLayout.visibility = View.VISIBLE

        // Update the UI with current XP info
        currentXpText.text = "Current XP: $currentXP"
        val remaining = 500 - currentXP
        requiredXpText.text = "Need $remaining more XP"
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
                        // Only add users with XP >= 500 to leaderboard (eligible users)
                        val userXP = if (user.totalXP > 0) user.totalXP else user.score
                        if (userXP >= 500) {
                            userList.add(user)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    Log.d("Leaderboard", "Loaded ${userList.size} eligible users (>=500 XP)")
                }
            }
    }
}
