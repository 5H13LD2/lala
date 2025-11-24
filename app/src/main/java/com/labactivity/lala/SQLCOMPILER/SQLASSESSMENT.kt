package com.labactivity.lala.SQLCOMPILER

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.SQLCOMPILER.adapters.SQLChallengeAdapter
import com.labactivity.lala.SQLCOMPILER.services.FirestoreSQLHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Object to setup SQL challenges in PYTHONASSESSMENT folder style
 * Provides easy integration for homepage and other screens
 */
object SQLASSESSMENT {

    private const val TAG = "SQLASSESSMENT"
    private val sqlHelper = FirestoreSQLHelper.getInstance()

    /**
     * Setup SQL challenges RecyclerView with horizontal layout (for homepage)
     * Mirrors PYTHONASSESMENT.TechnicalAssesment() method
     *
     * @param context Current context
     * @param recyclerView RecyclerView to populate
     * @param viewAllTextView TextView for "View All" click handler
     */
    fun SQLTechnicalAssessment(
        context: Context,
        recyclerView: RecyclerView,
        viewAllTextView: TextView
    ) {
        // Setup horizontal layout manager
        recyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Create adapter with loading state (shows skeletons by default)
        val adapter = SQLChallengeAdapter(context)
        recyclerView.adapter = adapter

        // Load challenges from Firestore (with shimmer/skeleton first)
        loadChallenges(adapter)

        // "View All" click listener
        viewAllTextView.setOnClickListener {
            it.isSelected = true
            val intent = Intent(context, AllSQLChallengesActivity::class.java)
            context.startActivity(intent)
        }
    }

    /**
     * Load SQL challenges asynchronously with skeleton loading state
     */
    private fun loadChallenges(adapter: SQLChallengeAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading SQL challenges from Firestore...")

                // Fetch challenges and progress in parallel
                val (challenges, userProgress) = withContext(Dispatchers.IO) {
                    val challengesList = sqlHelper.getAllChallenges()
                    val progressList = sqlHelper.getAllUserProgress()
                    Pair(challengesList, progressList)
                }

                Log.d(TAG, "✅ Loaded ${challenges.size} SQL challenges with ${userProgress.size} progress records")

                // Build progress map
                val progressMap = userProgress.associateBy { it.challengeId }

                // Update adapter with 5 second delay (shows skeleton for 5 seconds minimum)
                // This matches PYTHONASSESMENT behavior
                adapter.setChallengesWithProgressAndDelay(challenges, progressMap, 5000)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading SQL challenges", e)
                // Fallback: empty list with delay
                adapter.setChallengesWithProgressAndDelay(emptyList(), emptyMap(), 5000)
            }
        }
    }

    /**
     * Refresh the challenges list
     * Can be called when user returns to the page or manually refreshes
     */
    fun refreshChallenges(context: Context, recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter
        if (adapter is SQLChallengeAdapter) {
            // Re-show skeletons
            val skeletonAdapter = SQLChallengeAdapter(context)
            recyclerView.adapter = skeletonAdapter
            loadChallenges(skeletonAdapter)
        } else {
            // First-time setup
            SQLTechnicalAssessment(context, recyclerView, TextView(context))
        }
    }
}
