package com.labactivity.lala

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var questionCounterTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var timeUpMessageTextView: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var nextButton: Button

    private val questionRepository = QuestionRepository()
    private val questions = questionRepository.getQuestions()
    private var currentQuestionIndex = 0
    private val userAnswers = IntArray(questions.size) { -1 } // -1 means unanswered

    private var countDownTimer: CountDownTimer? = null
    private val startTimeInMillis: Long = 225000 // 3:45 in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        initializeViews()
        startTimer()
        displayQuestion()

        nextButton.setOnClickListener {
            saveAnswer()
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                displayQuestion()
            } else {
                finishQuiz()
            }
        }
    }

    private fun initializeViews() {
        questionTextView = findViewById(R.id.question_text)
        questionCounterTextView = findViewById(R.id.question_counter)
        timerTextView = findViewById(R.id.timer_text)
        timeUpMessageTextView = findViewById(R.id.time_up_message)
        optionsRadioGroup = findViewById(R.id.answer_options)
        nextButton = findViewById(R.id.next_button)
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(startTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
                timerTextView.text = timeLeftFormatted

                if (millisUntilFinished < 60000) {
                    timerTextView.setTextColor(Color.parseColor("#FF4500")) // Orange
                } else {
                    timerTextView.setTextColor(Color.parseColor("#1E90FF")) // Blue
                }
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                showTimeUpMessage()
                finishQuiz()
            }
        }.start()
    }

    private fun showTimeUpMessage() {
        timeUpMessageTextView.visibility = View.VISIBLE
    }

    private fun displayQuestion() {
        val question = questions[currentQuestionIndex]
        questionTextView.text = question.text
        questionCounterTextView.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"

        val option1 = findViewById<RadioButton>(R.id.option_1)
        val option2 = findViewById<RadioButton>(R.id.option_2)
        val option3 = findViewById<RadioButton>(R.id.option_3)
        val option4 = findViewById<RadioButton>(R.id.option_4)

        option1.text = question.options[0]
        option2.text = question.options[1]
        option3.text = question.options[2]
        option4.text = question.options[3]

        optionsRadioGroup.clearCheck()

        if (userAnswers[currentQuestionIndex] != -1) {
            when (userAnswers[currentQuestionIndex]) {
                0 -> option1.isChecked = true
                1 -> option2.isChecked = true
                2 -> option3.isChecked = true
                3 -> option4.isChecked = true
            }
        }

        if (currentQuestionIndex == questions.size - 1) {
            nextButton.text = "Finish"
        } else {
            nextButton.text = "Next"
        }
    }

    private fun saveAnswer() {
        val selectedId = optionsRadioGroup.checkedRadioButtonId
        when (selectedId) {
            R.id.option_1 -> userAnswers[currentQuestionIndex] = 0
            R.id.option_2 -> userAnswers[currentQuestionIndex] = 1
            R.id.option_3 -> userAnswers[currentQuestionIndex] = 2
            R.id.option_4 -> userAnswers[currentQuestionIndex] = 3
        }
    }

    private fun finishQuiz() {
        countDownTimer?.cancel()

        var score = 0
        for (i in questions.indices) {
            if (userAnswers[i] == questions[i].correctAnswerIndex) {
                score++
            }
        }

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("TOTAL", questions.size)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}