package com.labactivity.lala.FLASHCARD

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityFlashcardBinding
import java.util.Collections

class FlashcardActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityFlashcardBinding

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
        binding = ActivityFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up flashcard data
        loadFlashcards()

        // Set up animations
        setupAnimations()

        // Initialize UI
        updateUI()

        // Set up click listeners
        setupClickListeners()
    }

    private fun loadFlashcards() {
        // Sample flashcard data - replace with your actual data source
        flashcards = mutableListOf(
            Flashcard("What is Python?", "Python is a high-level, interpreted programming language known for its readability and versatility."),
            Flashcard("What are Python decorators?", "Decorators are a design pattern in Python that allow a user to add new functionality to an existing object without modifying its structure."),
            Flashcard("Explain list comprehension in Python", "List comprehension is a concise way to create lists in Python. It consists of brackets containing an expression followed by a for clause, then zero or more for or if clauses."),
            Flashcard("What is PEP 8?", "PEP 8 is the style guide for Python code, providing coding conventions for the Python code comprising the standard library in the main Python distribution."),
            Flashcard("What is the difference between lists and tuples?", "Lists are mutable while tuples are immutable. Lists use square brackets [] while tuples use parentheses ()."),
            // Other flashcards commented out...
           // Flashcard("What are Python modules?", "Modules are Python files with a .py extension that implement a set of functions. They can be imported and used in other Python programs."),
           // Flashcard("What is __init__ in Python?", "__init__ is a special method in Python classes, called a constructor, that is automatically called when a new instance of a class is created."),
           // Flashcard("What is a lambda function?", "A lambda function is a small anonymous function defined with the lambda keyword. It can take any number of arguments but can only have one expression."),
           // Flashcard("What is a dictionary in Python?", "A dictionary is an unordered collection of key-value pairs. In Python, dictionaries are defined within braces {} with each item being a pair separated by a colon."),
           // Flashcard("What is the Global Interpreter Lock (GIL)?", "The GIL is a mutex that protects access to Python objects, preventing multiple threads from executing Python bytecode at once."),
           // Flashcard("What is the difference between == and is?", "== checks if the values of two objects are equal, while 'is' checks if two variables point to the same object in memory."),
           // Flashcard("What are Python generators?", "Generators are functions that return an iterable generator object. They use the yield statement to return values one at a time, pausing the function's execution between calls."),
           Flashcard("What is exception handling in Python?", "Exception handling in Python is done using try-except blocks, allowing you to handle runtime errors gracefully."),
            Flashcard("What is the difference between append() and extend()?", "append() adds a single element to the end of a list, while extend() adds multiple elements from an iterable to the end of a list."),
            Flashcard("What is a context manager in Python?", "A context manager is an object that defines the methods __enter__() and __exit__(). It's typically used with the 'with' statement to ensure resources are properly managed."),
           Flashcard("What is a virtual environment in Python?", "A virtual environment is a self-contained directory tree that contains a Python installation for a particular version of Python, plus a number of additional packages."),
            Flashcard("What is duck typing in Python?", "Duck typing is a programming concept where the type or class of an object is less important than the methods it defines. If it walks like a duck and quacks like a duck, then it's a duck."),
            Flashcard("What is *args and **kwargs?", "*args allows a function to accept any number of positional arguments. **kwargs allows a function to accept any number of keyword arguments."),
            Flashcard("What is the difference between deep and shallow copy?", "A shallow copy creates a new object but keeps references to the original object's elements. A deep copy creates a new object and recursively copies all the elements from the original object."),
        )

        Log.d("FlashcardDebug", "Flashcards loaded: ${flashcards.size} cards")
    }

    private fun setupAnimations() {
        // Load animations for card flipping
        val scale = resources.displayMetrics.density
        binding.flashcardFront.cameraDistance = 8000 * scale
        binding.flashcardBack.cameraDistance = 8000 * scale

        frontAnimation = AnimatorInflater.loadAnimator(this, R.animator.card_flip_front) as AnimatorSet
        backAnimation = AnimatorInflater.loadAnimator(this, R.animator.card_flip_back) as AnimatorSet

        Log.d("FlashcardDebug", "Animations setup complete.")
    }

    private fun setupClickListeners() {
        // Back button click listener
        binding.btnBack.setOnClickListener {
            Log.d("FlashcardDebug", "Back button clicked")
            finish()
        }

        // Flashcard flip listeners
        binding.flashcardFront.setOnClickListener {
            if (!isAnimating) {
                flipCard()
            }
        }

        binding.flashcardBack.setOnClickListener {
            if (!isAnimating) {
                flipCard()
            }
        }

        // Navigation buttons
        binding.btnPrevious.setOnClickListener {
            if (currentCardIndex > 0) {
                currentCardIndex--
                resetCard()
                updateUI()
                Log.d("FlashcardDebug", "Navigated to previous card: $currentCardIndex")
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentCardIndex < flashcards.size - 1) {
                currentCardIndex++
                resetCard()
                updateUI()
                Log.d("FlashcardDebug", "Navigated to next card: $currentCardIndex")
            }
        }

        binding.btnShuffle.setOnClickListener {
            // Shuffle the flashcards
            Collections.shuffle(flashcards)
            currentCardIndex = 0
            resetCard()
            updateUI()
            Log.d("FlashcardDebug", "Flashcards shuffled")
        }
    }

    private fun flipCard() {
        if (isAnimating) return
        isAnimating = true

        Log.d("FlashcardDebug", "Flipping card, isFrontVisible: $isFrontVisible")

        if (isFrontVisible) {
            // We're showing the front, need to flip to back

            // Make sure both cards are in the correct starting position
            binding.flashcardFront.visibility = View.VISIBLE
            binding.flashcardBack.visibility = View.VISIBLE

            // Set up animations
            frontAnimation.setTarget(binding.flashcardFront)
            backAnimation.setTarget(binding.flashcardBack)

            // Remove any existing listeners to prevent multiple callbacks
            clearAnimationListeners()

            // Start animations together
            frontAnimation.start()
            backAnimation.start()

            // Add completion listener to the last animation that will finish
            frontAnimation.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    isAnimating = false
                }
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    binding.flashcardFront.visibility = View.GONE
                    isFrontVisible = false
                    isAnimating = false
                    Log.d("FlashcardDebug", "Flip to back complete")
                }
            })
        } else {
            // We're showing the back, need to flip to front

            // Make sure both cards are in the correct starting position
            binding.flashcardFront.visibility = View.VISIBLE
            binding.flashcardBack.visibility = View.VISIBLE

            // Set up animations (note the reversed targets)
            frontAnimation.setTarget(binding.flashcardBack)
            backAnimation.setTarget(binding.flashcardFront)

            // Remove any existing listeners
            clearAnimationListeners()

            // Start animations
            frontAnimation.start()
            backAnimation.start()

            // Add completion listener
            frontAnimation.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    isAnimating = false
                }
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    binding.flashcardBack.visibility = View.GONE
                    isFrontVisible = true
                    isAnimating = false
                    Log.d("FlashcardDebug", "Flip to front complete")
                }
            })
        }
    }

    private fun clearAnimationListeners() {
        // Remove all existing listeners from animations
        frontAnimation.removeAllListeners()
        backAnimation.removeAllListeners()
    }

    private fun resetCard() {
        // Cancel any ongoing animations
        frontAnimation.cancel()
        backAnimation.cancel()
        isAnimating = false

        // Reset card to show front
        binding.flashcardFront.visibility = View.VISIBLE
        binding.flashcardBack.visibility = View.GONE
        isFrontVisible = true
    }

    private fun updateUI() {
        // Update progress indicator
        binding.tvProgress.text = "${currentCardIndex + 1}/${flashcards.size}"

        // Update question and answer text
        val currentCard = flashcards[currentCardIndex]
        binding.tvQuestion.text = currentCard.question
        binding.tvAnswer.text = currentCard.answer

        // Update navigation button states
        binding.btnPrevious.isEnabled = currentCardIndex > 0
        binding.btnNext.isEnabled = currentCardIndex < flashcards.size - 1
    }

    // Data class for flashcards
    data class Flashcard(val question: String, val answer: String)
}