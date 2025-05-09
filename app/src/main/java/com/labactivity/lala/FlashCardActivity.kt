package com.labactivity.lala

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Collections

class FlashcardActivity : AppCompatActivity() {

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var tvProgress: TextView
    private lateinit var flashcardFront: ConstraintLayout
    private lateinit var flashcardBack: ConstraintLayout
    private lateinit var tvQuestion: TextView
    private lateinit var tvAnswer: TextView
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnShuffle: ImageButton

    // Animation
    private lateinit var frontAnimation: AnimatorSet
    private lateinit var backAnimation: AnimatorSet
    private var isFrontVisible = true
    private var isAnimating = false

    // Flashcard Data
    private var currentCardIndex = 2  // Starting at 3rd card (index 2)
    private var flashcards = mutableListOf<Flashcard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        // Initialize views
        initViews()

        // Set up flashcard data
        loadFlashcards()

        // Set up animations
        setupAnimations()

        // Initialize UI
        updateUI()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvProgress = findViewById(R.id.tvProgress)
        flashcardFront = findViewById(R.id.flashcardFront)
        flashcardBack = findViewById(R.id.flashcardBack)
        tvQuestion = findViewById(R.id.tvQuestion)
        tvAnswer = findViewById(R.id.tvAnswer)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnShuffle = findViewById(R.id.btnShuffle)

        Log.d("FlashcardDebug", "btnBack initialized: $btnBack")
        Log.d("FlashcardDebug", "flashcardFront initialized: $flashcardFront")
        Log.d("FlashcardDebug", "tvQuestion initialized: $tvQuestion")
        Log.d("FlashcardDebug", "tvAnswer initialized: $tvAnswer")
    }

    private fun loadFlashcards() {
        // Sample flashcard data - replace with your actual data source
        flashcards = mutableListOf(
            Flashcard("What is Python?", "Python is a high-level, interpreted programming language known for its readability and versatility."),
            Flashcard("What are Python decorators?", "Decorators are a design pattern in Python that allow a user to add new functionality to an existing object without modifying its structure."),
            Flashcard("Explain list comprehension in Python", "List comprehension is a concise way to create lists in Python. It consists of brackets containing an expression followed by a for clause, then zero or more for or if clauses."),
            Flashcard("What is PEP 8?", "PEP 8 is the style guide for Python code, providing coding conventions for the Python code comprising the standard library in the main Python distribution."),
            Flashcard("What is the difference between lists and tuples?", "Lists are mutable while tuples are immutable. Lists use square brackets [] while tuples use parentheses ()."),
           // Flashcard("What are Python modules?", "Modules are Python files with a .py extension that implement a set of functions. They can be imported and used in other Python programs."),
           // Flashcard("What is __init__ in Python?", "__init__ is a special method in Python classes, called a constructor, that is automatically called when a new instance of a class is created."),
          //  Flashcard("What is a lambda function?", "A lambda function is a small anonymous function defined with the lambda keyword. It can take any number of arguments but can only have one expression."),
          //  Flashcard("What is a dictionary in Python?", "A dictionary is an unordered collection of key-value pairs. In Python, dictionaries are defined within braces {} with each item being a pair separated by a colon."),
           // Flashcard("What is the Global Interpreter Lock (GIL)?", "The GIL is a mutex that protects access to Python objects, preventing multiple threads from executing Python bytecode at once."),
           // Flashcard("What is the difference between == and is?", "== checks if the values of two objects are equal, while 'is' checks if two variables point to the same object in memory."),
            //Flashcard("What are Python generators?", "Generators are functions that return an iterable generator object. They use the yield statement to return values one at a time, pausing the function's execution between calls."),
           // Flashcard("What is exception handling in Python?", "Exception handling in Python is done using try-except blocks, allowing you to handle runtime errors gracefully."),
          //  Flashcard("What is the difference between append() and extend()?", "append() adds a single element to the end of a list, while extend() adds multiple elements from an iterable to the end of a list."),
          //  Flashcard("What is a context manager in Python?", "A context manager is an object that defines the methods __enter__() and __exit__(). It's typically used with the 'with' statement to ensure resources are properly managed."),
          //  Flashcard("What is a virtual environment in Python?", "A virtual environment is a self-contained directory tree that contains a Python installation for a particular version of Python, plus a number of additional packages."),
           // Flashcard("What is duck typing in Python?", "Duck typing is a programming concept where the type or class of an object is less important than the methods it defines. If it walks like a duck and quacks like a duck, then it's a duck."),
           // Flashcard("What is *args and **kwargs?", "*args allows a function to accept any number of positional arguments. **kwargs allows a function to accept any number of keyword arguments."),
           // Flashcard("What is the difference between deep and shallow copy?", "A shallow copy creates a new object but keeps references to the original object's elements. A deep copy creates a new object and recursively copies all the elements from the original object."),
           // Flashcard("What is the purpose of __name__ == '__main__' in Python?", "This condition checks whether the Python script is being run directly or being imported as a module. Code under this condition runs only when the script is executed directly.")
        )

        Log.d("FlashcardDebug", "Flashcards loaded: ${flashcards.size} cards")
    }

    private fun setupAnimations() {
        // Load animations for card flipping
        val scale = resources.displayMetrics.density
        flashcardFront.cameraDistance = 8000 * scale
        flashcardBack.cameraDistance = 8000 * scale

        frontAnimation = AnimatorInflater.loadAnimator(this, R.animator.card_flip_front) as AnimatorSet
        backAnimation = AnimatorInflater.loadAnimator(this, R.animator.card_flip_back) as AnimatorSet

        Log.d("FlashcardDebug", "Animations setup complete.")
    }

    private fun setupClickListeners() {
        // Back button click listener
        btnBack.setOnClickListener {
            Log.d("FlashcardDebug", "Back button clicked")
            finish()
        }

        // Flashcard flip listeners
        flashcardFront.setOnClickListener {
            Log.d("FlashcardDebug", "=== FRONT CLICK START ===")
            Log.d("FlashcardDebug", "Front view clicked")
            Log.d("FlashcardDebug", "Current state - isFrontVisible: $isFrontVisible")
            Log.d("FlashcardDebug", "Front visibility: ${flashcardFront.visibility}")
            Log.d("FlashcardDebug", "Back visibility: ${flashcardBack.visibility}")
            flipCard()
            Log.d("FlashcardDebug", "=== FRONT CLICK END ===")
        }

        flashcardBack.setOnClickListener {
            Log.d("FlashcardDebug", "=== BACK CLICK START ===")
            Log.d("FlashcardDebug", "Back view clicked")
            Log.d("FlashcardDebug", "Current state - isFrontVisible: $isFrontVisible")
            Log.d("FlashcardDebug", "Front visibility: ${flashcardFront.visibility}")
            Log.d("FlashcardDebug", "Back visibility: ${flashcardBack.visibility}")
            flipCard()
            Log.d("FlashcardDebug", "=== BACK CLICK END ===")
        }

        // Navigation buttons
        btnPrevious.setOnClickListener {
            if (currentCardIndex > 0) {
                currentCardIndex--
                resetCard()
                updateUI()
                Log.d("FlashcardDebug", "Navigated to previous card: $currentCardIndex")
            }
        }

        btnNext.setOnClickListener {
            if (currentCardIndex < flashcards.size - 1) {
                currentCardIndex++
                resetCard()
                updateUI()
                Log.d("FlashcardDebug", "Navigated to next card: $currentCardIndex")
            }
        }

        btnShuffle.setOnClickListener {
            // Shuffle the flashcards
            Collections.shuffle(flashcards)
            currentCardIndex = 0
            resetCard()
            updateUI()
            Log.d("FlashcardDebug", "Flashcards shuffled")
        }
    }

    private fun flipCard() {
        Log.d("FlashcardDebug", "=== FLIP CARD START ===")
        Log.d("FlashcardDebug", "Current state - isFrontVisible: $isFrontVisible")
        Log.d("FlashcardDebug", "Front visibility: ${flashcardFront.visibility}")
        Log.d("FlashcardDebug", "Back visibility: ${flashcardBack.visibility}")
        
        // Prevent multiple animations
        if (isAnimating) {
            Log.d("FlashcardDebug", "Animation already in progress, ignoring flip request")
            return
        }
        
        // Cancel any ongoing animations
        frontAnimation.cancel()
        backAnimation.cancel()
        Log.d("FlashcardDebug", "Cancelled any ongoing animations")
        
        isAnimating = true
        
        if (isFrontVisible) {
            Log.d("FlashcardDebug", "Flipping from FRONT to BACK")
            // Front is visible, animate to show back
            frontAnimation.setTarget(flashcardFront)
            backAnimation.setTarget(flashcardBack)

            // Set animation listeners before starting animations
            frontAnimation.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {
                    Log.d("FlashcardDebug", "Front animation STARTED")
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    Log.d("FlashcardDebug", "Front animation CANCELLED")
                    isAnimating = false
                }
                override fun onAnimationRepeat(animation: android.animation.Animator) {
                    Log.d("FlashcardDebug", "Front animation REPEATED")
                }
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    Log.d("FlashcardDebug", "Front animation ENDED")
                    // Make back view visible after front animation ends
                    flashcardBack.visibility = View.VISIBLE
                    flashcardFront.visibility = View.GONE
                    isFrontVisible = false
                    isAnimating = false
                    Log.d("FlashcardDebug", "Set front view to GONE, back view to VISIBLE, isFrontVisible = false")
                }
            })

            // Start only front animation first
            frontAnimation.start()
            Log.d("FlashcardDebug", "Started front animation")
        } else {
            Log.d("FlashcardDebug", "Flipping from BACK to FRONT")
            // Back is visible, animate to show front
            frontAnimation.setTarget(flashcardBack)
            backAnimation.setTarget(flashcardFront)

            // Set animation listeners before starting animations
            frontAnimation.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {
                    Log.d("FlashcardDebug", "Back animation STARTED")
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    Log.d("FlashcardDebug", "Back animation CANCELLED")
                    isAnimating = false
                }
                override fun onAnimationRepeat(animation: android.animation.Animator) {
                    Log.d("FlashcardDebug", "Back animation REPEATED")
                }
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    Log.d("FlashcardDebug", "Back animation ENDED")
                    // Make front view visible after back animation ends
                    flashcardFront.visibility = View.VISIBLE
                    flashcardBack.visibility = View.GONE
                    isFrontVisible = true
                    isAnimating = false
                    Log.d("FlashcardDebug", "Set back view to GONE, front view to VISIBLE, isFrontVisible = true")
                }
            })

            // Start only back animation first
            frontAnimation.start()
            Log.d("FlashcardDebug", "Started back animation")
        }
        Log.d("FlashcardDebug", "=== FLIP CARD END ===")
    }

    private fun resetCard() {
        Log.d("FlashcardDebug", "=== RESET CARD START ===")
        Log.d("FlashcardDebug", "Current state - isFrontVisible: $isFrontVisible")
        Log.d("FlashcardDebug", "Front visibility before reset: ${flashcardFront.visibility}")
        Log.d("FlashcardDebug", "Back visibility before reset: ${flashcardBack.visibility}")
        
        // Cancel any ongoing animations
        frontAnimation.cancel()
        backAnimation.cancel()
        isAnimating = false
        
        // Reset card to show front
        flashcardFront.visibility = View.VISIBLE
        flashcardBack.visibility = View.GONE
        isFrontVisible = true
        Log.d("FlashcardDebug", "Reset card to front view")
        
        Log.d("FlashcardDebug", "Front visibility after reset: ${flashcardFront.visibility}")
        Log.d("FlashcardDebug", "Back visibility after reset: ${flashcardBack.visibility}")
        Log.d("FlashcardDebug", "=== RESET CARD END ===")
    }

    private fun updateUI() {
        Log.d("FlashcardDebug", "Updating UI, current index: $currentCardIndex")
        // Update progress indicator
        tvProgress.text = "${currentCardIndex + 1}/${flashcards.size}"

        // Update question and answer text
        val currentCard = flashcards[currentCardIndex]
        tvQuestion.text = currentCard.question
        tvAnswer.text = currentCard.answer

        // Update navigation button states
        btnPrevious.isEnabled = currentCardIndex > 0
        btnNext.isEnabled = currentCardIndex < flashcards.size - 1
    }

    // Data class for flashcards
    data class Flashcard(val question: String, val answer: String)
}
