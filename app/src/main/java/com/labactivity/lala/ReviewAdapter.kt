    package com.labactivity.lala
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView

    class ReviewAdapter(private val questions: List<Question>) :
        RecyclerView.Adapter<ReviewAdapter.QuestionViewHolder>() {

        class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val questionText: TextView = view.findViewById(R.id.question_text)
            val correctAnswerText: TextView = view.findViewById(R.id.correct_answer_text)
            val difficultyText: TextView = view.findViewById(R.id.difficulty_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_question_review, parent, false)
            return QuestionViewHolder(view)
        }

        override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
            val question = questions[position]
            holder.questionText.text = question.text
            holder.correctAnswerText.text = "Correct answer: ${question.options[question.correctAnswerIndex]}"
            holder.difficultyText.text = "Difficulty: ${question.difficulty.name.toLowerCase().capitalize()}"
        }

        override fun getItemCount() = questions.size
    }