package com.labactivity.lala.TECHNICALREVIEWER

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.FLASHCARD.Flashcard
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityFlashcardBinding

class TechnicalFlashcardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlashcardBinding
    private lateinit var flashcards: List<Flashcard>
    private var currentIndex = 0
    private var isFrontShowing = true

    private lateinit var frontAnimation: AnimatorSet
    private lateinit var backAnimation: AnimatorSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topicTitle = intent.getStringExtra("TOPIC_TITLE") ?: "Technical Interview"
        val topicId = intent.getIntExtra("TOPIC_ID", 0)

        binding.tvTitle.text = topicTitle
        flashcards = getFlashcardsForTopic(topicId)

        setupFlashcardUI()
        setupClickListeners()
        setupAnimations()
        updateUI()
    }

    private fun getFlashcardsForTopic(topicId: Int): List<Flashcard> {
        return when (topicId) {
            1 -> listOf(
                Flashcard("What is a variable?", "A variable is a container for storing data values that can be changed during program execution."),
                Flashcard("Difference between val and var?", "'val' is immutable, 'var' is mutable."),
                Flashcard("What is a function?", "Reusable block of code that performs a task."),
                Flashcard("What is an if statement?", "Conditional code execution based on a boolean expression."),
                Flashcard("What is a loop?", "Repeats a block of code while a condition is met.")
            )
            2 -> listOf(
                Flashcard("What is an array?", "Fixed-size structure storing elements of same type."),
                Flashcard("What is a linked list?", "Nodes connected linearly with references to the next node."),
                Flashcard("What is a stack?", "LIFO structure: Last-In-First-Out."),
                Flashcard("What is a queue?", "FIFO structure: First-In-First-Out."),
                Flashcard("What is binary search?", "Efficient search by dividing sorted data in half repeatedly.")
            )
            else -> listOf(Flashcard("Sample Question", "Sample Answer"))
        }
    }

    private fun setupFlashcardUI() {
        updateProgressText()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        binding.flashcardFront.setOnClickListener { flipCard() }
        binding.flashcardBack.setOnClickListener { flipCard() }

        binding.btnPrevious.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                resetCard()
                updateUI()
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentIndex < flashcards.size - 1) {
                currentIndex++
                resetCard()
                updateUI()
            }
        }

        binding.btnShuffle.setOnClickListener {
            flashcards = flashcards.shuffled()
            currentIndex = 0
            resetCard()
            updateUI()
        }
    }

    private fun setupAnimations() {
        val scale = applicationContext.resources.displayMetrics.density
        binding.flashcardFront.cameraDistance = 8000 * scale
        binding.flashcardBack.cameraDistance = 8000 * scale

        frontAnimation = AnimatorInflater.loadAnimator(applicationContext,
            R.animator.card_flip_front
        ) as AnimatorSet
        backAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.card_flip_back) as AnimatorSet
    }

    private fun flipCard() {
        if (isFrontShowing) {
            frontAnimation.setTarget(binding.flashcardFront)
            backAnimation.setTarget(binding.flashcardBack)
            frontAnimation.start()
            backAnimation.start()
            binding.flashcardFront.visibility = View.GONE
            binding.flashcardBack.visibility = View.VISIBLE
        } else {
            frontAnimation.setTarget(binding.flashcardBack)
            backAnimation.setTarget(binding.flashcardFront)
            backAnimation.start()
            frontAnimation.start()
            binding.flashcardFront.visibility = View.VISIBLE
            binding.flashcardBack.visibility = View.GONE
        }
        isFrontShowing = !isFrontShowing
    }

    private fun resetCard() {
        if (!isFrontShowing) {
            isFrontShowing = true
            binding.flashcardFront.visibility = View.VISIBLE
            binding.flashcardBack.visibility = View.GONE
        }
    }

    private fun updateUI() {
        val current = flashcards[currentIndex]
        binding.tvQuestion.text = current.question
        binding.tvAnswer.text = current.answer
        updateProgressText()
        binding.btnPrevious.isEnabled = currentIndex > 0
        binding.btnNext.isEnabled = currentIndex < flashcards.size - 1
    }

    private fun updateProgressText() {
        binding.tvProgress.text = "${currentIndex + 1}/${flashcards.size}"
    }
}
