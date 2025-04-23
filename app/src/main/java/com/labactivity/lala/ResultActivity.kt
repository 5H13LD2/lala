package com.labactivity.lala
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 0)

        val scoreTextView = findViewById<TextView>(R.id.score_text)
        scoreTextView.text = "You got $score out of $total correct!"

        val reviewAnswersButton = findViewById<Button>(R.id.review_answers_button)
        reviewAnswersButton.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("SCORE", score)
            startActivity(intent)
        }

        val tryAgainButton = findViewById<Button>(R.id.try_again_button)
        tryAgainButton.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}