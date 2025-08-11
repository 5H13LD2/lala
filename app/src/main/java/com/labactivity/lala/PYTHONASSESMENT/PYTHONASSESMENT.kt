package com.labactivity.lala.PYTHONASSESMENT

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.FLASHCARD.Flashcard
import com.labactivity.lala.FLASHCARD.FlashcardTopic

object PYTHONASSESMENT {

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
        recyclerView.adapter = TechnicalAssessmentAdapter(context, challenges)

        viewAllAssessments.setOnClickListener {
            // Placeholder action; replace with navigation if needed
            it.isSelected = true
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