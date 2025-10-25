package com.labactivity.lala.REVIEWER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.quiz.Quiz
import com.labactivity.lala.R

class ReviewAdapter(private val quizzes: List<Quiz>) :
    RecyclerView.Adapter<ReviewAdapter.QuizViewHolder>() {

    class QuizViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.question_text)
        val questionIdText: TextView = view.findViewById(R.id.question_id_text)
        val optionsContainer: LinearLayout = view.findViewById(R.id.options_container)
        val explanationText: TextView = view.findViewById(R.id.explanation_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_review, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizzes[position]
        val context = holder.itemView.context

        // Set question number
        holder.questionIdText.text = "Question ${position + 1}"

        // Set question text
        holder.questionText.text = quiz.question

        // Clear previous options
        holder.optionsContainer.removeAllViews()

        // Display all options with correct answer highlighted
        quiz.options.forEachIndexed { index, option ->
            val optionTextView = TextView(context).apply {
                text = "${('A' + index)}. $option"
                textSize = 14f
                setPadding(16, 8, 16, 8)

                // Highlight the correct answer
                if (index == quiz.correctOptionIndex) {
                    setTextColor(context.getColor(R.color.success_green))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setBackgroundResource(R.color.light_green_background)
                } else {
                    setTextColor(context.getColor(android.R.color.black))
                }
            }
            holder.optionsContainer.addView(optionTextView)
        }

        // Set explanation
        if (quiz.explanation.isNotEmpty()) {
            holder.explanationText.visibility = View.VISIBLE
            holder.explanationText.text = "Explanation: ${quiz.explanation}"
        } else {
            holder.explanationText.visibility = View.GONE
        }
    }

    override fun getItemCount() = quizzes.size
}