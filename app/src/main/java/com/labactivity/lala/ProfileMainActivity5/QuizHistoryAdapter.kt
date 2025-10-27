package com.labactivity.lala.ProfileMainActivity5

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.labactivity.lala.R

class QuizHistoryAdapter(
    private val context: Context,
    private val quizHistory: MutableList<QuizHistoryItem>,
    private val onQuizClick: (QuizHistoryItem) -> Unit
) : RecyclerView.Adapter<QuizHistoryAdapter.QuizViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        android.util.Log.d("QuizHistoryAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_history, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        android.util.Log.d("QuizHistoryAdapter", "onBindViewHolder called for position $position: ${quizHistory[position].courseName}")
        holder.bind(quizHistory[position])
    }

    override fun getItemCount() = quizHistory.size

    fun updateQuizHistory(newHistory: List<QuizHistoryItem>) {
        android.util.Log.d("QuizHistoryAdapter", "updateQuizHistory called with ${newHistory.size} items")
        quizHistory.clear()
        quizHistory.addAll(newHistory)
        notifyDataSetChanged()
        android.util.Log.d("QuizHistoryAdapter", "Adapter updated, itemCount = $itemCount")
    }

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quizCard: MaterialCardView = itemView.findViewById(R.id.quizCard)
        private val quizIcon: ImageView = itemView.findViewById(R.id.quizIcon)
        private val courseName: TextView = itemView.findViewById(R.id.courseName)
        private val quizDate: TextView = itemView.findViewById(R.id.quizDate)
        private val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        private val percentageText: TextView = itemView.findViewById(R.id.percentageText)

        fun bind(quiz: QuizHistoryItem) {
            courseName.text = quiz.courseName
            scoreText.text = "${quiz.score}/${quiz.totalQuestions}"
            percentageText.text = "${quiz.percentage}%"

            // Format date as relative time (e.g., "2 hours ago", "Just now")
            val relativeTime = DateUtils.getRelativeTimeSpanString(
                quiz.completedAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            quizDate.text = relativeTime

            // Set icon based on course
            val iconResId = when {
                quiz.courseName.contains("Java", ignoreCase = true) -> R.drawable.java
                quiz.courseName.contains("Python", ignoreCase = true) -> R.drawable.python
                quiz.courseName.contains("SQL", ignoreCase = true) -> R.drawable.sql
                else -> R.drawable.ic_quiz
            }
            quizIcon.setImageResource(iconResId)

            // Set percentage color based on score
            val percentageColor = when {
                quiz.percentage >= 80 -> context.getColor(R.color.quiz_excellent)
                quiz.percentage >= 60 -> context.getColor(R.color.quiz_good)
                quiz.percentage >= 40 -> context.getColor(R.color.quiz_average)
                else -> context.getColor(R.color.quiz_poor)
            }
            percentageText.setTextColor(percentageColor)

            quizCard.setOnClickListener {
                onQuizClick(quiz)
            }
        }
    }
}
