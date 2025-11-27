package com.labactivity.lala.SQLCOMPILER

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityAllSqlChallengesBinding
import com.labactivity.lala.UTILS.AnimationUtils.slideUpFadeIn
import com.labactivity.lala.UTILS.AnimationUtils.fadeIn
import com.labactivity.lala.UTILS.AnimationUtils.animateItems
import com.labactivity.lala.SQLCOMPILER.adapters.SQLChallengeAdapter
import com.labactivity.lala.SQLCOMPILER.models.SQLChallenge
import com.labactivity.lala.SQLCOMPILER.models.SQLChallengeProgress
import com.labactivity.lala.SQLCOMPILER.services.FirestoreSQLHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity to display all SQL challenges in a grid layout
 * with filtering capabilities and smooth animations
 */
class AllSQLChallengesActivity : BaseActivity() {

    private lateinit var binding: ActivityAllSqlChallengesBinding
    private lateinit var adapter: SQLChallengeAdapter
    private val sqlHelper = FirestoreSQLHelper.getInstance()

    private var allChallenges: List<SQLChallenge> = emptyList()
    private var filteredChallenges: List<SQLChallenge> = emptyList()
    private var progressMap: Map<String, SQLChallengeProgress> = emptyMap()

    companion object {
        private const val TAG = "AllSQLChallengesActivity"
        private const val GRID_SPAN_COUNT = 2 // 2 columns for tablet/landscape support
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllSqlChallengesBinding.inflate(layoutInflater)
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
        adapter = SQLChallengeAdapter(this)
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
     * Load all SQL challenges from Firestore
     */
    private fun loadChallenges() {
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading all SQL challenges from Firestore...")

                // Fetch challenges and user progress in parallel
                val (challenges, userProgress) = withContext(Dispatchers.IO) {
                    val challengesList = sqlHelper.getAllChallenges()
                    val progressList = sqlHelper.getAllUserProgress()
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
     * Filter challenges based on selected criteria
     */
    private fun filterChallenges(filterType: String) {
        filteredChallenges = when (filterType) {
            "All" -> allChallenges
            "Easy", "Medium", "Hard" -> allChallenges.filter {
                it.difficulty.equals(filterType, ignoreCase = true)
            }
            "In Progress" -> allChallenges.filter { challenge ->
                progressMap[challenge.id]?.status == "in_progress"
            }
            "Completed" -> allChallenges.filter { challenge ->
                progressMap[challenge.id]?.status == "completed"
            }
            else -> allChallenges
        }

        // Update adapter and count
        adapter.setChallengesWithProgress(filteredChallenges, progressMap)
        updateChallengeCount(filteredChallenges.size)

        // Show/hide empty state
        if (filteredChallenges.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            // Re-animate filtered items
            binding.recyclerViewChallenges.post {
                binding.recyclerViewChallenges.animateItems(
                    itemDelay = 60,
                    itemDuration = 300
                )
            }
        }
    }

    /**
     * Update challenge count text
     */
    private fun updateChallengeCount(count: Int) {
        val text = when {
            count == 0 -> "No challenges found"
            count == 1 -> "1 challenge available"
            else -> "$count challenges available"
        }
        binding.textChallengeCount.text = text
    }

    /**
     * Show empty state view
     */
    private fun showEmptyState() {
        binding.emptyStateContainer.visibility = View.VISIBLE
        binding.recyclerViewChallenges.visibility = View.GONE
        binding.emptyStateContainer.fadeIn(duration = 300)
    }

    /**
     * Hide empty state view
     */
    private fun hideEmptyState() {
        binding.emptyStateContainer.visibility = View.GONE
        binding.recyclerViewChallenges.visibility = View.VISIBLE
    }

    /**
     * Animate initial screen load with staggered animations
     */
    private fun animateInitialLoad() {
        // Hide elements initially
        binding.headerContainer.alpha = 0f
        binding.chipGroupFilter.alpha = 0f
        binding.textChallengeCount.alpha = 0f
        binding.recyclerViewChallenges.alpha = 0f

        // Staggered slide-up animations
        binding.headerContainer.slideUpFadeIn(duration = 400, startDelay = 100)
        binding.chipGroupFilter.slideUpFadeIn(duration = 400, startDelay = 200)
        binding.textChallengeCount.slideUpFadeIn(duration = 400, startDelay = 300)
        binding.recyclerViewChallenges.slideUpFadeIn(duration = 400, startDelay = 400)
    }

    override fun onResume() {
        super.onResume()
        // Reload progress when user returns (in case they completed a challenge)
        refreshProgress()
    }

    /**
     * Refresh user progress without reloading all challenges
     */
    private fun refreshProgress() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val userProgress = withContext(Dispatchers.IO) {
                    sqlHelper.getAllUserProgress()
                }
                progressMap = userProgress.associateBy { it.challengeId }
                adapter.updateProgress(progressMap)
                Log.d(TAG, "✅ Progress refreshed")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error refreshing progress", e)
            }
        }
    }

    /**
     * Override back button to simply finish activity without exit dialog
     */
    override fun onBackPressed() {
        finish()
    }
}
