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

        // Load challenges dynamically from Firestore
        loadChallenges(context, recyclerView)

        viewAllAssessments.setOnClickListener {
            // Placeholder action; replace with navigation if needed
            it.isSelected = true
        }
    }

    private fun loadChallenges(context: Context, recyclerView: RecyclerView) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading challenges from Firestore...")
                
                // Show loading state (optional)
                recyclerView.adapter = TechnicalAssessmentAdapter(context, emptyList())

                // Fetch challenges in background
                val challenges = withContext(Dispatchers.IO) {
                    assessmentService.getChallengesForUser()
                }

                Log.d(TAG, "Loaded ${challenges.size} challenges")
                
                // Update adapter with fetched challenges
                recyclerView.adapter = TechnicalAssessmentAdapter(context, challenges)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading challenges", e)
                // Show empty state or error state
                recyclerView.adapter = TechnicalAssessmentAdapter(context, emptyList())
            }
        }
    }

    /**
     * Refreshes the challenges list
     * Can be called when user returns to the page or manually refreshes
     */
    fun refreshChallenges(context: Context, recyclerView: RecyclerView) {
        loadChallenges(context, recyclerView)
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