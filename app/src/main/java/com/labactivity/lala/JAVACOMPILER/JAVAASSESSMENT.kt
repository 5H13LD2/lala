package com.labactivity.lala.JAVACOMPILER

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.JAVACOMPILER.adapters.JavaChallengeAdapter
import com.labactivity.lala.JAVACOMPILER.services.FirestoreJavaHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Object to setup Java challenges in PYTHONASSESSMENT folder style
 * Provides easy integration for homepage and other screens
 */
object JAVAASSESSMENT {

    private const val TAG = "JAVAASSESSMENT"
    private val javaHelper = FirestoreJavaHelper.getInstance()

    /**
     * Setup Java challenges RecyclerView with horizontal layout (for homepage)
     * Mirrors PYTHONASSESMENT.TechnicalAssesment() method
     *
     * @param context Current context
     * @param recyclerView RecyclerView to populate
     * @param viewAllTextView TextView for "View All" click handler
     */
    fun JavaTechnicalAssessment(
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
        val adapter = JavaChallengeAdapter(context)
        recyclerView.adapter = adapter

        // Load challenges from Firestore (with shimmer/skeleton first)
        loadChallenges(adapter)

        // "View All" click listener
        viewAllTextView.setOnClickListener {
            it.isSelected = true
            val intent = Intent(context, AllJavaChallengesActivity::class.java)
            context.startActivity(intent)
        }
    }

    /**
     * Load Java challenges asynchronously with skeleton loading state
     */
    private fun loadChallenges(adapter: JavaChallengeAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading Java challenges from Firestore...")

                // Fetch challenges and progress in parallel
                val (challenges, userProgress) = withContext(Dispatchers.IO) {
                    val challengesList = javaHelper.getAllChallenges()
                    val progressList = javaHelper.getAllUserProgress()
                    Pair(challengesList, progressList)
                }

                Log.d(TAG, "✅ Loaded ${challenges.size} Java challenges with ${userProgress.size} progress records")

                // Build progress map
                val progressMap = userProgress.associateBy { it.challengeId }

                // Update adapter with 5 second delay (shows skeleton for 5 seconds minimum)
                // This matches PYTHONASSESMENT behavior
                adapter.setChallengesWithProgressAndDelay(challenges, progressMap, 2000)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading Java challenges", e)
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
        if (adapter is JavaChallengeAdapter) {
            // Re-show skeletons
            val skeletonAdapter = JavaChallengeAdapter(context)
            recyclerView.adapter = skeletonAdapter
            loadChallenges(skeletonAdapter)
        } else {
            // First-time setup
            JavaTechnicalAssessment(context, recyclerView, TextView(context))
        }
    }
}
