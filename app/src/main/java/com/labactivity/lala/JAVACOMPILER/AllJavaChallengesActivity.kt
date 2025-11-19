package com.labactivity.lala.JAVACOMPILER

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityAllJavaChallengesBinding
import com.labactivity.lala.UTILS.AnimationUtils.slideUpFadeIn
import com.labactivity.lala.UTILS.AnimationUtils.fadeIn
import com.labactivity.lala.UTILS.AnimationUtils.animateItems
import com.labactivity.lala.JAVACOMPILER.adapters.JavaChallengeAdapter
import com.labactivity.lala.JAVACOMPILER.models.JavaChallenge
import com.labactivity.lala.JAVACOMPILER.models.JavaChallengeProgress
import com.labactivity.lala.JAVACOMPILER.services.FirestoreJavaHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity to display all Java challenges in a grid layout
 * with filtering capabilities and smooth animations
 */
class AllJavaChallengesActivity : BaseActivity() {

    private lateinit var binding: ActivityAllJavaChallengesBinding
    private lateinit var adapter: JavaChallengeAdapter
    private val javaHelper = FirestoreJavaHelper.getInstance()

    private var allChallenges: List<JavaChallenge> = emptyList()
    private var filteredChallenges: List<JavaChallenge> = emptyList()
    private var progressMap: Map<String, JavaChallengeProgress> = emptyMap()

    companion object {
        private const val TAG = "AllJavaChallengesActivity"
        private const val GRID_SPAN_COUNT = 2 // 2 columns for tablet/landscape support
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllJavaChallengesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFilterChips()
        animateInitialLoad()
        loadChallenges()
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

        binding.recyclerViewChallenges.layoutManager = gridLayoutManager
        adapter = JavaChallengeAdapter(this)
        binding.recyclerViewChallenges.adapter = adapter
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
     * Load all Java challenges from Firestore
     */
    private fun loadChallenges() {
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading all Java challenges from Firestore...")

                // Fetch challenges and user progress in parallel
                val (challenges, userProgress) = withContext(Dispatchers.IO) {
                    val challengesList = javaHelper.getAllChallenges()
                    val progressList = javaHelper.getAllUserProgress()
                    Pair(challengesList, progressList)
                }

                Log.d(TAG, "✅ Loaded ${challenges.size} challenges and ${userProgress.size} progress records")

                // Build progress map
                progressMap = userProgress.associateBy { it.challengeId }

                allChallenges = challenges
                filteredChallenges = challenges

                // Update UI
                updateChallengeCount(challenges.size)
                adapter.setChallengesWithProgress(challenges, progressMap)

                binding.progressBar.visibility = View.GONE

                // Show empty state if no challenges
                if (challenges.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                    // Animate items after they're loaded
                    binding.recyclerViewChallenges.post {
                        binding.recyclerViewChallenges.animateItems(
                            itemDelay = 60,
                            itemDuration = 300
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading challenges", e)
                binding.progressBar.visibility = View.GONE
                updateChallengeCount(0)
                showEmptyState()
            }
        }
    }

    /**
     * Filter challenges based on chip selection
     */
    private fun filterChallenges(filterType: String) {
        Log.d(TAG, "Filtering challenges by: $filterType")

        filteredChallenges = when (filterType) {
            "All" -> allChallenges
            "Easy" -> allChallenges.filter { it.difficulty == "Easy" }
            "Medium" -> allChallenges.filter { it.difficulty == "Medium" }
            "Hard" -> allChallenges.filter { it.difficulty == "Hard" }
            "Available" -> allChallenges.filter { progressMap[it.id]?.status != "completed" }
            "Completed" -> allChallenges.filter { progressMap[it.id]?.status == "completed" }
            else -> allChallenges
        }

        // Update UI
        updateChallengeCount(filteredChallenges.size)
        adapter.setChallengesWithProgress(filteredChallenges, progressMap)

        // Show/hide empty state
        if (filteredChallenges.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }

    /**
     * Update challenge count text
     */
    private fun updateChallengeCount(count: Int) {
        binding.textChallengeCount.text = when {
            count == 0 -> "No challenges found"
            count == 1 -> "1 challenge available"
            else -> "$count challenges available"
        }
    }

    /**
     * Show empty state view
     */
    private fun showEmptyState() {
        binding.emptyStateContainer.visibility = View.VISIBLE
        binding.recyclerViewChallenges.visibility = View.GONE
    }

    /**
     * Hide empty state view
     */
    private fun hideEmptyState() {
        binding.emptyStateContainer.visibility = View.GONE
        binding.recyclerViewChallenges.visibility = View.VISIBLE
    }

    /**
     * Animate UI elements on initial load
     */
    private fun animateInitialLoad() {
        // Hide elements initially
        binding.headerContainer.alpha = 0f
        binding.chipGroupFilter.alpha = 0f
        binding.textChallengeCount.alpha = 0f
        binding.recyclerViewChallenges.alpha = 0f

        // Animate elements with staggered delays
        binding.headerContainer.slideUpFadeIn(duration = 400, startDelay = 50)
        binding.chipGroupFilter.slideUpFadeIn(duration = 400, startDelay = 100)
        binding.textChallengeCount.fadeIn(duration = 300, startDelay = 150)
        binding.recyclerViewChallenges.slideUpFadeIn(duration = 400, startDelay = 200)
    }

    /**
     * Refresh challenges when activity resumes
     */
    override fun onResume() {
        super.onResume()
        // Reload challenges to reflect any progress updates
        if (::adapter.isInitialized) {
            loadChallenges()
        }
    }
}
