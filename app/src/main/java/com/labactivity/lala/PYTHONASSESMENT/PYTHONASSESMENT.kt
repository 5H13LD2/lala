package com.labactivity.lala.PYTHONASSESMENT

import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.FLASHCARD.Flashcard
import com.labactivity.lala.FLASHCARD.FlashcardTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PYTHONASSESMENT {

    private const val TAG = "PYTHONASSESMENT"
    private val assessmentService = TechnicalAssessmentService()

    fun TechnicalAssesment(
        context: Context,
        recyclerView: RecyclerView,
        viewAllAssessments: TextView
    ) {
        recyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // 🔹 Create adapter with loading state (shows skeletons by default)
        val adapter = TechnicalAssessmentAdapter(context)
        recyclerView.adapter = adapter

        // 🔹 Load challenges from Firestore (with shimmer/skeleton first)
        loadChallenges(adapter)

        // 🔹 Optional click listener for "View All"
        viewAllAssessments.setOnClickListener {
            it.isSelected = true
        }
    }

    private fun loadChallenges(adapter: TechnicalAssessmentAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading challenges from Firestore...")

                // 🔸 Fetch challenges asynchronously
                val challenges = withContext(Dispatchers.IO) {
                    assessmentService.getChallengesForUser()
                }

                Log.d(TAG, "✅ Loaded ${challenges.size} challenges")

                // 🔸 Update adapter with 5 second delay (shows skeleton for 5 seconds minimum)
                adapter.setChallengesWithDelay(challenges, 1000)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading challenges", e)
                // Fallback: empty list with delay
                adapter.setChallengesWithDelay(emptyList(), 5000)
            }
        }
    }

    /**
     * 🔄 Refreshes the challenges list.
     * Can be called when user returns to the page or manually refreshes.
     */
    fun refreshChallenges(context: Context, recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter
        if (adapter is TechnicalAssessmentAdapter) {
            // Re-show skeletons
            val skeletonAdapter = TechnicalAssessmentAdapter(context)
            recyclerView.adapter = skeletonAdapter
            loadChallenges(skeletonAdapter)
        } else {
            // First-time setup
            TechnicalAssesment(context, recyclerView, TextView(context))
        }
    }

    fun TechnicalInterview(
        context: Context,
        recyclerView: RecyclerView,
        textAllPractice: TextView,
        viewAllInterviews: TextView
    ) {
        val topics = listOf(
            FlashcardTopic(
                id = 1,
                title = "Python Basics",
                difficulty = "Easy",
                flashcards = emptyList<Flashcard>()
            ),
            FlashcardTopic(
                id = 2,
                title = "Data Structures",
                difficulty = "Medium",
                flashcards = emptyList<Flashcard>()
            )
        )

        recyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerView.adapter = TechnicalInterviewAdapter(context, topics)

        textAllPractice.setOnClickListener { it.isSelected = true }
        viewAllInterviews.setOnClickListener { it.isSelected = true }
    }
}
