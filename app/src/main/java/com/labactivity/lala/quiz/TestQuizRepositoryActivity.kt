package com.labactivity.lala.quiz

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.R

/**
 * Activity for testing and demonstrating the QuizRepositoryFactory
 * and the different quiz repositories.
 */
class TestQuizRepositoryActivity : AppCompatActivity() {

    private lateinit var moduleIdEditText: EditText
    private lateinit var queryButton: Button
    private lateinit var resetButton: Button
    private lateinit var repoNameTextView: TextView
    private lateinit var questionCountTextView: TextView
    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var adapter: QuizTestAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_quiz_repository)
        
        // Initialize views
        moduleIdEditText = findViewById(R.id.module_id_edit_text)
        queryButton = findViewById(R.id.query_button)
        resetButton = findViewById(R.id.reset_button)
        repoNameTextView = findViewById(R.id.repo_name_text_view)
        questionCountTextView = findViewById(R.id.question_count_text_view)
        questionsRecyclerView = findViewById(R.id.questions_recycler_view)
        
        // Initialize RecyclerView and adapter
        adapter = QuizTestAdapter(emptyList())
        questionsRecyclerView.adapter = adapter
        questionsRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Set up query button
        queryButton.setOnClickListener {
            val moduleId = moduleIdEditText.text.toString().trim()
            if (moduleId.isNotEmpty()) {
                fetchQuestions(moduleId)
            }
        }
        
        // Set up reset button
        resetButton.setOnClickListener {
            moduleIdEditText.setText("")
            repoNameTextView.text = "Repository: Not Selected"
            questionCountTextView.text = "Question Count: 0"
            adapter.updateQuestions(emptyList())
        }
    }
    
    private fun fetchQuestions(moduleId: String) {
        // Get the appropriate repository for this module
        val repository = QuizRepositoryFactory.getRepositoryForModule(moduleId)
        val repoName = repository.javaClass.simpleName
        
        // Log repository info
        Log.d("TestQuizRepositoryActivity", "Using repository $repoName for module ID: $moduleId")
        
        // Get questions from the repository
        val questions = repository.getQuestionsForModule(moduleId)
        val questionCount = questions.size
        
        // Update UI
        repoNameTextView.text = "Repository: $repoName"
        questionCountTextView.text = "Question Count: $questionCount"
        adapter.updateQuestions(questions)
        
        // Log questions
        Log.d("TestQuizRepositoryActivity", "Found $questionCount questions for module ID: $moduleId")
        questions.forEachIndexed { index, quiz ->
            Log.d("TestQuizRepositoryActivity", "Question ${index + 1}: ${quiz.id} - ${quiz.question}")
        }
    }
}

/**
 * Adapter for displaying Quiz objects in the test activity
 */
class QuizTestAdapter(
    private var quizzes: List<Quiz>
) : RecyclerView.Adapter<QuizTestAdapter.QuizViewHolder>() {

    class QuizViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idTextView: TextView = view.findViewById(R.id.quiz_id_text_view)
        val questionTextView: TextView = view.findViewById(R.id.quiz_question_text_view)
        val difficultyTextView: TextView = view.findViewById(R.id.quiz_difficulty_text_view)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): QuizViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_test, parent, false)
        return QuizViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizzes[position]
        holder.idTextView.text = "ID: ${quiz.id}"
        holder.questionTextView.text = quiz.question
        holder.difficultyTextView.text = "Difficulty: ${quiz.difficulty.name}"
    }
    
    override fun getItemCount() = quizzes.size
    
    fun updateQuestions(newQuizzes: List<Quiz>) {
        quizzes = newQuizzes
        notifyDataSetChanged()
    }
} 