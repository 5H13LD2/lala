package com.labactivity.lala.PYTHONASSESMENT

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.FLASHCARD.Flashcard
import com.labactivity.lala.FLASHCARD.FlashcardTopic
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityAllInterviewsBinding
import com.labactivity.lala.UTILS.AnimationUtils.slideUpFadeIn
import com.labactivity.lala.UTILS.AnimationUtils.fadeIn
import com.labactivity.lala.UTILS.AnimationUtils.animateItems

/**
 * Activity to display all technical interview topics in a grid layout
 * with filtering capabilities and smooth animations
 */
class AllInterviewsActivity : BaseActivity() {

    private lateinit var binding: ActivityAllInterviewsBinding
    private lateinit var adapter: TechnicalInterviewAdapter

    private var allTopics: List<FlashcardTopic> = emptyList()
    private var filteredTopics: List<FlashcardTopic> = emptyList()

    companion object {
        private const val TAG = "AllInterviewsActivity"
        private const val GRID_SPAN_COUNT = 2 // 2 columns for tablet/landscape support
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllInterviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFilterChips()
        animateInitialLoad()
        loadInterviewTopics()
    }

    /**
     * Setup toolbar with back navigation
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Setup RecyclerView with GridLayoutManager
     */
    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, GRID_SPAN_COUNT)
        binding.recyclerViewInterviews.layoutManager = gridLayoutManager
    }

    /**
     * Setup filter chips for difficulty filtering
     */
    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                // If no chip selected, show all
                filterTopics("All Topics")
            } else {
                val selectedChip = findViewById<Chip>(checkedIds.first())
                filterTopics(selectedChip.text.toString())
            }
        }
    }

    /**
     * Load all interview topics
     * TODO: Replace with Firebase/API call when backend is ready
     */
    private fun loadInterviewTopics() {
        binding.progressBar.visibility = View.VISIBLE

        // Sample data - replace with actual Firebase/API call
        val topics = listOf(
            FlashcardTopic(
                id = 1,
                title = "Python Basics",
                difficulty = "Easy",
                flashcards = listOf(
                    Flashcard("What is Python?", "Python is a high-level programming language"),
                    Flashcard("What are variables?", "Variables store data values")
                )
            ),
            FlashcardTopic(
                id = 2,
                title = "Data Structures",
                difficulty = "Medium",
                flashcards = listOf(
                    Flashcard("What is a list?", "A list is an ordered collection of items"),
                    Flashcard("What is a dictionary?", "A dictionary stores key-value pairs")
                )
            ),
            FlashcardTopic(
                id = 3,
                title = "Object Oriented Programming",
                difficulty = "Medium",
                flashcards = listOf(
                    Flashcard("What is a class?", "A class is a blueprint for creating objects"),
                    Flashcard("What is inheritance?", "Inheritance allows a class to inherit attributes from another")
                )
            ),
            FlashcardTopic(
                id = 4,
                title = "Algorithms",
                difficulty = "Hard",
                flashcards = listOf(
                    Flashcard("What is Big O notation?", "Big O describes time/space complexity"),
                    Flashcard("What is binary search?", "Binary search finds items in sorted arrays efficiently")
                )
            ),
            FlashcardTopic(
                id = 5,
                title = "File Handling",
                difficulty = "Easy",
                flashcards = listOf(
                    Flashcard("How to open a file?", "Use open() function"),
                    Flashcard("How to read a file?", "Use file.read() method")
                )
            ),
            FlashcardTopic(
                id = 6,
                title = "Exception Handling",
                difficulty = "Medium",
                flashcards = listOf(
                    Flashcard("What is try-except?", "try-except handles runtime errors"),
                    Flashcard("What is finally block?", "finally block always executes")
                )
            ),
            FlashcardTopic(
                id = 7,
                title = "Advanced Python",
                difficulty = "Hard",
                flashcards = listOf(
                    Flashcard("What are decorators?", "Decorators modify function behavior"),
                    Flashcard("What are generators?", "Generators produce values lazily")
                )
            ),
            FlashcardTopic(
                id = 8,
                title = "Web Development",
                difficulty = "Hard",
                flashcards = listOf(
                    Flashcard("What is Django?", "Django is a Python web framework"),
                    Flashcard("What is Flask?", "Flask is a lightweight web framework")
                )
            )
        )

        Log.d(TAG, "✅ Loaded ${topics.size} interview topics")

        allTopics = topics
        filteredTopics = topics

        // Update UI
        updateTopicCount(topics.size)
        adapter = TechnicalInterviewAdapter(this, topics)
        binding.recyclerViewInterviews.adapter = adapter

        binding.progressBar.visibility = View.GONE
        hideEmptyState()

        // Animate items after they're loaded
        binding.recyclerViewInterviews.post {
            binding.recyclerViewInterviews.animateItems(
                itemDelay = 60,
                itemDuration = 300
            )
        }
    }

    /**
     * Filter topics based on selected criteria
     */
    private fun filterTopics(filterType: String) {
        filteredTopics = when (filterType) {
            "All Topics" -> allTopics
            "Easy", "Medium", "Hard" -> allTopics.filter {
                it.difficulty.equals(filterType, ignoreCase = true)
            }
            else -> allTopics
        }

        // Update adapter and count
        adapter = TechnicalInterviewAdapter(this, filteredTopics)
        binding.recyclerViewInterviews.adapter = adapter
        updateTopicCount(filteredTopics.size)

        // Show/hide empty state
        if (filteredTopics.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            // Re-animate filtered items
            binding.recyclerViewInterviews.post {
                binding.recyclerViewInterviews.animateItems(
                    itemDelay = 60,
                    itemDuration = 300
                )
            }
        }
    }

    /**
     * Update topic count text
     */
    private fun updateTopicCount(count: Int) {
        val text = when {
            count == 0 -> "No topics found"
            count == 1 -> "1 topic available"
            else -> "$count topics available"
        }
        binding.textTopicCount.text = text
    }

    /**
     * Show empty state view
     */
    private fun showEmptyState() {
        binding.emptyStateContainer.visibility = View.VISIBLE
        binding.recyclerViewInterviews.visibility = View.GONE
        binding.emptyStateContainer.fadeIn(duration = 300)
    }

    /**
     * Hide empty state view
     */
    private fun hideEmptyState() {
        binding.emptyStateContainer.visibility = View.GONE
        binding.recyclerViewInterviews.visibility = View.VISIBLE
    }

    /**
     * Animate initial screen load with staggered animations
     */
    private fun animateInitialLoad() {
        // Hide elements initially
        binding.headerContainer.alpha = 0f
        binding.chipGroupFilter.alpha = 0f
        binding.textTopicCount.alpha = 0f
        binding.recyclerViewInterviews.alpha = 0f

        // Staggered slide-up animations
        binding.headerContainer.slideUpFadeIn(duration = 400, startDelay = 100)
        binding.chipGroupFilter.slideUpFadeIn(duration = 400, startDelay = 200)
        binding.textTopicCount.slideUpFadeIn(duration = 400, startDelay = 300)
        binding.recyclerViewInterviews.slideUpFadeIn(duration = 400, startDelay = 400)
    }
}
