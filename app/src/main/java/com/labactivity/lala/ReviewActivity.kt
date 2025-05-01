package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.quiz.QuizRepositoryFactory
import com.labactivity.lala.quiz.Quiz

class ReviewActivity : AppCompatActivity() {

    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: QuizReviewAdapter
    private lateinit var moduleTitleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        questionsRecyclerView = findViewById(R.id.questions_recycler_view)
        moduleTitleTextView = findViewById(R.id.module_title_text)

        // Get module information from intent
        val moduleId = intent.getStringExtra("MODULE_ID") ?: "python"
        val moduleTitle = intent.getStringExtra("MODULE_TITLE") ?: "Quiz Review"
        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 0)
        
        Log.d("ReviewActivity", "Reviewing questions for module: $moduleId, Title: $moduleTitle, Score: $score/$total")

        // Update UI with module information
        moduleTitleTextView.text = "$moduleTitle - Review"
        
        // Get the appropriate repository for this module using the factory
        val quizRepository = QuizRepositoryFactory.getRepositoryForModule(moduleId)
        Log.d("ReviewActivity", "Using repository: ${quizRepository.javaClass.simpleName}")
        
        // Get questions for this specific module
        val questions = quizRepository.getQuestionsForModule(moduleId)

        if (questions.isEmpty()) {
            Log.w("ReviewActivity", "No questions found for module ID: $moduleId")
            // Show a message or handle the empty case
        } else {
            Log.d("ReviewActivity", "Found ${questions.size} questions for review")
        }

        // Initialize the adapter with the questions from the selected repository
        reviewAdapter = QuizReviewAdapter(questions, moduleId)
        questionsRecyclerView.adapter = reviewAdapter
        questionsRecyclerView.layoutManager = LinearLayoutManager(this)

        val homeButton = findViewById<Button>(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity4::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}

/**
 * Adapter for displaying Quiz objects in the review screen
 */
class QuizReviewAdapter(
    private val quizzes: List<Quiz>,
    private val moduleId: String
) : RecyclerView.Adapter<QuizReviewAdapter.QuizViewHolder>() {

    class QuizViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val questionText: android.widget.TextView = view.findViewById(R.id.question_text)
        val correctAnswerText: android.widget.TextView = view.findViewById(R.id.correct_answer_text)
        val difficultyText: android.widget.TextView = view.findViewById(R.id.difficulty_text)
        val questionIdText: android.widget.TextView = view.findViewById(R.id.question_id_text)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): QuizViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_review, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizzes[position]
        
        // Set question number and ID
        holder.questionIdText.text = "Question ${position + 1} (ID: ${quiz.id})"
        
        // Set question text
        holder.questionText.text = quiz.question
        
        // Get the correct answer text from the options using the correctOptionIndex
        val correctAnswer = quiz.options[quiz.correctOptionIndex]
        holder.correctAnswerText.text = "Correct answer: $correctAnswer"
        
        // Format the difficulty level for display
        holder.difficultyText.text = "Difficulty: ${quiz.difficulty.name.lowercase().capitalize()}"
    }

    override fun getItemCount() = quizzes.size
}