package com.labactivity.lala.REVIEWER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.quiz.Quiz
import com.labactivity.lala.R

class ReviewAdapter(private val quizzes: List<Quiz>) :
    RecyclerView.Adapter<ReviewAdapter.QuizViewHolder>() {

    class QuizViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.question_text)
        val correctAnswerText: TextView = view.findViewById(R.id.correct_answer_text)
        val difficultyText: TextView = view.findViewById(R.id.difficulty_text)
        val questionIdText: TextView = view.findViewById(R.id.question_id_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_review, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizzes[position]
        
        // Set question number
        holder.questionIdText.text = "Question ${position + 1}"
        
        // Set question text
        holder.questionText.text = quiz.question
        
        // Set correct answer
        val correctAnswer = quiz.options.getOrNull(quiz.correctOptionIndex) ?: "Unknown"
        holder.correctAnswerText.text = "Correct answer: $correctAnswer"
        
        // Set difficulty
        holder.difficultyText.text = "Difficulty: ${quiz.difficulty.capitalize()}"
    }

    override fun getItemCount() = quizzes.size

    private fun String.capitalize() = this.replaceFirstChar { it.uppercase() }
}