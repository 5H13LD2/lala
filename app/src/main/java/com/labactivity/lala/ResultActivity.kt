package com.labactivity.lala
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

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

        // Save the score if not already saved
        if (moduleId.isNotEmpty()) {
            quizScoreManager.saveQuizScore(moduleId, score, total)
        }

        val scoreTextView = findViewById<TextView>(R.id.score_text)
        scoreTextView.text = "You got $score out of $total correct!"

        // Determine if passing and set appropriate color
        val isPassing = score >= (total * 0.7).toInt()
        val resultMessageView = findViewById<TextView>(R.id.result_message)

        if (isPassing) {
            resultMessageView.text = "Congratulations! You passed the quiz."
            resultMessageView.setTextColor(ContextCompat.getColor(this, R.color.success_green))
        } else {
            resultMessageView.text = "You didn't pass this time. Keep studying and try again!"
            resultMessageView.setTextColor(ContextCompat.getColor(this, R.color.error_red))
        }

        val reviewAnswersButton = findViewById<Button>(R.id.review_answers_button)
        reviewAnswersButton.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("SCORE", score)
            intent.putExtra("MODULE_ID", moduleId)
            intent.putExtra("MODULE_TITLE", moduleTitle)
            startActivity(intent)
        }

        val tryAgainButton = findViewById<Button>(R.id.try_again_button)
        tryAgainButton.setOnClickListener {
            val intent = Intent(this, com.labactivity.lala.quiz.DynamicQuizActivity::class.java).apply {
                putExtra("module_id", moduleId)
                putExtra("module_title", moduleTitle)
            }
            startActivity(intent)
            finish()
        }
    }
}