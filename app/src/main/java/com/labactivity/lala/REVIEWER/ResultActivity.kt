package com.labactivity.lala.REVIEWER
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.labactivity.lala.quiz.Quiz
import com.labactivity.lala.quiz.QuizScoreManager
import com.labactivity.lala.R

class ResultActivity : AppCompatActivity() {

    private lateinit var quizScoreManager: QuizScoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        quizScoreManager = QuizScoreManager(this)

        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 0)
        val moduleId = intent.getStringExtra("MODULE_ID") ?: ""
        val moduleTitle = intent.getStringExtra("MODULE_TITLE") ?: ""
        val quizId = intent.getStringExtra("QUIZ_ID") ?: "technical_quiz"
        val timeTaken = intent.getLongExtra("TIME_TAKEN", 0L)

        // Get quiz questions from intent
        val quizQuestions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("QUIZ_QUESTIONS", Quiz::class.java) ?: arrayListOf()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<Quiz>("QUIZ_QUESTIONS") ?: arrayListOf()
        }

        // Save the score with time taken
        if (moduleId.isNotEmpty()) {
            quizScoreManager.saveQuizScore(
                moduleId = moduleId,
                score = score,
                total = total,
                courseName = moduleTitle.ifEmpty { "Quiz" },
                courseId = moduleId,
                difficulty = "NORMAL",
                timeTaken = timeTaken
            )
        }

        // Calculate percentage
        val percentage = if (total > 0) (score.toFloat() / total.toFloat() * 100).toInt() else 0

        // Update score text
        val scoreTextView = findViewById<TextView>(R.id.score_text)
        scoreTextView.text = "You got $score out of $total correct!"

        // Update percentage display
        val scorePercentageView = findViewById<TextView>(R.id.score_percentage)
        scorePercentageView.text = "$percentage%"

        // Determine if passing and set appropriate color and icon
        val isPassing = percentage >= 70
        val resultMessageView = findViewById<TextView>(R.id.result_message)
        val resultIconView = findViewById<TextView>(R.id.result_icon)

        if (isPassing) {
            resultIconView.text = "🎉"
            resultMessageView.text = "Congratulations! You passed the quiz."
            resultMessageView.setTextColor(ContextCompat.getColor(this, R.color.success_green))
            scorePercentageView.setTextColor(ContextCompat.getColor(this, R.color.brand_blue))
        } else {
            resultIconView.text = "📚"
            resultMessageView.text = "You didn't pass this time. Keep studying and try again!"
            resultMessageView.setTextColor(ContextCompat.getColor(this, R.color.error_red))
            scorePercentageView.setTextColor(ContextCompat.getColor(this, R.color.brand_mauve))
        }

        val reviewAnswersButton = findViewById<Button>(R.id.review_answers_button)
        reviewAnswersButton.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("SCORE", score)
            intent.putExtra("TOTAL", total)
            intent.putExtra("MODULE_ID", moduleId)
            intent.putExtra("MODULE_TITLE", moduleTitle)
            intent.putExtra("QUIZ_ID", quizId)
            intent.putParcelableArrayListExtra("QUIZ_QUESTIONS", quizQuestions)
            startActivity(intent)
        }

        val tryAgainButton = findViewById<Button>(R.id.try_again_button)
        tryAgainButton.setOnClickListener {
            val intent = Intent(this, com.labactivity.lala.quiz.DynamicQuizActivity::class.java).apply {
                putExtra("module_id", moduleId)
                putExtra("module_title", moduleTitle)
                putExtra("quiz_id", quizId)
            }
            startActivity(intent)
            finish()
        }
    }
}