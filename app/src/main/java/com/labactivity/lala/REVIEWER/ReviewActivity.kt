package com.labactivity.lala.REVIEWER

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.UTILS.DialogUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.LOGINPAGE.MainActivity
import com.labactivity.lala.R
import com.labactivity.lala.homepage.MainActivity4

import com.labactivity.lala.quiz.Quiz

class ReviewActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ReviewActivity"
    }

    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: QuizReviewAdapter
    private lateinit var moduleTitleTextView: TextView
    private lateinit var homeButton: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val quizList = mutableListOf<Quiz>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        questionsRecyclerView = findViewById(R.id.questions_recycler_view)
        moduleTitleTextView = findViewById(R.id.module_title_text)
        homeButton = findViewById(R.id.home_button)

        // Get module information from intent
        val moduleId = intent.getStringExtra("MODULE_ID") ?: "python"
        val moduleTitle = intent.getStringExtra("MODULE_TITLE") ?: "Quiz Review"
        val quizId = intent.getStringExtra("QUIZ_ID") ?: "technical_quiz"
        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 0)

        // Get quiz questions from intent
        val quizQuestions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("QUIZ_QUESTIONS", Quiz::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<Quiz>("QUIZ_QUESTIONS")
        }

        Log.d(
            TAG,
            "Reviewing questions for module: $moduleId, Title: $moduleTitle, Quiz ID: $quizId, Score: $score/$total"
        )
        Log.d(TAG, "Received ${quizQuestions?.size ?: 0} questions from intent")

        // Update UI with module information
        moduleTitleTextView.text = "$moduleTitle - Review (Score: $score/$total)"

        // Setup RecyclerView
        questionsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Use passed questions if available, otherwise load from Firebase
        if (quizQuestions != null && quizQuestions.isNotEmpty()) {
            Log.d(TAG, "Using ${quizQuestions.size} questions passed from quiz")
            quizList.clear()
            quizList.addAll(quizQuestions)
            reviewAdapter = QuizReviewAdapter(quizList, moduleId)
            questionsRecyclerView.adapter = reviewAdapter
        } else {
            Log.d(TAG, "No questions passed, loading from Firebase")
            loadReviewQuestions(moduleId, quizId)
        }

        // Setup home button
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity4::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loadReviewQuestions(moduleId: String, quizId: String) {
        Log.d(TAG, "Loading review questions for module: $moduleId from quiz: $quizId")

        firestore.collection("course_quiz")
            .document(quizId)
            .collection("questions")
            .whereEqualTo("module_id", moduleId)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Successfully loaded ${documents.size()} questions from quiz: $quizId")

                if (documents.isEmpty) {
                    DialogUtils.showInfoDialog(this, "No Questions", "No questions found for review")
                    finish()
                    return@addOnSuccessListener
                }

                quizList.clear()
                documents.forEach { document ->
                    try {
                        val quiz = document.toObject(Quiz::class.java)
                        if (quiz.question.isNotEmpty()) {
                            quizList.add(quiz)
                            Log.d(TAG, "Loaded: ${quiz.question}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing quiz document", e)
                    }
                }

                // Setup adapter with loaded questions
                reviewAdapter = QuizReviewAdapter(quizList, moduleId)
                questionsRecyclerView.adapter = reviewAdapter

                Log.d(TAG, "Review adapter setup with ${quizList.size} questions")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading review questions", exception)
                DialogUtils.showErrorDialog(
                    this,
                    "Error",
                    "Error loading questions: ${exception.message}"
                )
                finish()
            }

        // ✅ FIXED: Replace binding with findViewById
        val homeButton = findViewById<Button>(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
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
        val questionIdText: android.widget.TextView = view.findViewById(R.id.question_id_text)
        val optionsContainer: android.widget.LinearLayout = view.findViewById(R.id.options_container)
        val explanationText: android.widget.TextView = view.findViewById(R.id.explanation_text)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): QuizViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
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
            holder.explanationText.visibility = android.view.View.VISIBLE
            holder.explanationText.text = "Explanation: ${quiz.explanation}"
        } else {
            holder.explanationText.visibility = android.view.View.GONE
        }
    }

    override fun getItemCount() = quizzes.size
}