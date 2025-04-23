package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReviewActivity : AppCompatActivity() {

    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        questionsRecyclerView = findViewById(R.id.questions_recycler_view)

        val questionRepository = QuestionRepository()
        val questions = questionRepository.getQuestions()

        reviewAdapter = ReviewAdapter(questions)
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