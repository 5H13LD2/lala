package com.labactivity.lala.PYTHONASSESMENT

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityAllAssessmentsBinding
import com.labactivity.lala.UTILS.AnimationUtils.slideUpFadeIn
import com.labactivity.lala.UTILS.AnimationUtils.fadeIn
import com.labactivity.lala.UTILS.AnimationUtils.animateItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity to display all technical assessments in a grid layout
 * with filtering capabilities and smooth animations
 */
class AllAssessmentsActivity : BaseActivity() {

    private lateinit var binding: ActivityAllAssessmentsBinding
    private lateinit var adapter: TechnicalAssessmentAdapter
    private val assessmentService = TechnicalAssessmentService()

    private var allChallenges: List<Challenge> = emptyList()
    private var filteredChallenges: List<Challenge> = emptyList()

    companion object {
        private const val TAG = "AllAssessmentsActivity"
        private const val GRID_SPAN_COUNT = 2 // 2 columns for tablet/landscape support
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllAssessmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFilterChips()
        animateInitialLoad()
        loadAssessments()
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

        // Make skeleton items span full width during loading
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.isLoading) GRID_SPAN_COUNT else 1
            }
        }

        binding.recyclerViewAssessments.layoutManager = gridLayoutManager
        adapter = TechnicalAssessmentAdapter(this)
        binding.recyclerViewAssessments.adapter = adapter
    }

    /**
     * Setup filter chips for difficulty and status filtering
     */
    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                // If no chip selected, show all
                filterChallenges("All")
            } else {
                val selectedChip = findViewById<Chip>(checkedIds.first())
                filterChallenges(selectedChip.text.toString())
            }
        }
    }

    /**
     * Load all assessments from Firestore
     */
    private fun loadAssessments() {
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading all challenges from Firestore...")

                val challenges = withContext(Dispatchers.IO) {
                    assessmentService.getChallengesForUser()
                }

                Log.d(TAG, "✅ Loaded ${challenges.size} challenges")

                allChallenges = challenges
                filteredChallenges = challenges

                // Update UI
                updateAssessmentCount(challenges.size)
                adapter.setChallenges(challenges)

                binding.progressBar.visibility = View.GONE

                // Show empty state if no challenges
                if (challenges.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                    // Animate items after they're loaded
                    binding.recyclerViewAssessments.post {
                        binding.recyclerViewAssessments.animateItems(
                            itemDelay = 60,
                            itemDuration = 300
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading challenges", e)
                binding.progressBar.visibility = View.GONE
                updateAssessmentCount(0)
                showEmptyState()
            }
        }
    }

    /**
     * Filter challenges based on selected criteria
     */
    private fun filterChallenges(filterType: String) {
        filteredChallenges = when (filterType) {
            "All" -> allChallenges
            "Easy", "Medium", "Hard" -> allChallenges.filter {
                it.difficulty.equals(filterType, ignoreCase = true)
            }
            "Available" -> allChallenges.filter { it.status == "available" }
            "Completed" -> allChallenges.filter { it.status == "taken" }
            else -> allChallenges
        }

        // Update adapter and count
        adapter.setChallenges(filteredChallenges)
        updateAssessmentCount(filteredChallenges.size)

        // Show/hide empty state
        if (filteredChallenges.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            // Re-animate filtered items
            binding.recyclerViewAssessments.post {
                binding.recyclerViewAssessments.animateItems(
                    itemDelay = 60,
                    itemDuration = 300
                )
            }
        }
    }

    /**
     * Update assessment count text
     */
    private fun updateAssessmentCount(count: Int) {
        val text = when {
            count == 0 -> "No assessments found"
            count == 1 -> "1 assessment available"
            else -> "$count assessments available"
        }
        binding.textAssessmentCount.text = text
    }

    /**
     * Show empty state view
     */
    private fun showEmptyState() {
        binding.emptyStateContainer.visibility = View.VISIBLE
        binding.recyclerViewAssessments.visibility = View.GONE
        binding.emptyStateContainer.fadeIn(duration = 300)
    }

    /**
     * Hide empty state view
     */
    private fun hideEmptyState() {
        binding.emptyStateContainer.visibility = View.GONE
        binding.recyclerViewAssessments.visibility = View.VISIBLE
    }

    /**
     * Animate initial screen load with staggered animations
     */
    private fun animateInitialLoad() {
        // Hide elements initially
        binding.headerContainer.alpha = 0f
        binding.chipGroupFilter.alpha = 0f
        binding.textAssessmentCount.alpha = 0f
        binding.recyclerViewAssessments.alpha = 0f

        // Staggered slide-up animations
        binding.headerContainer.slideUpFadeIn(duration = 400, startDelay = 100)
        binding.chipGroupFilter.slideUpFadeIn(duration = 400, startDelay = 200)
        binding.textAssessmentCount.slideUpFadeIn(duration = 400, startDelay = 300)
        binding.recyclerViewAssessments.slideUpFadeIn(duration = 400, startDelay = 400)
    }
}
