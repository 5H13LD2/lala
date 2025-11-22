package com.labactivity.lala.DAILYPROBLEMPAGE

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityProblemOfDayBinding

/**
 * Activity for Daily Problem of the Day
 * Displays 3 tabs: Problem, Editor, Solution
 */
class ProblemOfDayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProblemOfDayBinding
    private var problemId: String = ""
    private var courseId: String = ""
    private var compilerType: String = ""

    companion object {
        const val EXTRA_PROBLEM_ID = "PROBLEM_ID"
        const val EXTRA_COURSE_ID = "COURSE_ID"
        const val EXTRA_COMPILER_TYPE = "COMPILER_TYPE"
        private const val TAG = "ProblemOfDayActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProblemOfDayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get extras from intent
        problemId = intent.getStringExtra(EXTRA_PROBLEM_ID) ?: ""
        courseId = intent.getStringExtra(EXTRA_COURSE_ID) ?: ""
        compilerType = intent.getStringExtra(EXTRA_COMPILER_TYPE) ?: ""

        if (problemId.isEmpty()) {
            Log.e(TAG, "Problem ID is missing!")
            finish()
            return
        }

        setupViewPager()
        setupBackButton()

        Log.d(TAG, "Loaded problem: $problemId, compiler: $compilerType")
    }

    private fun setupViewPager() {
        val adapter = ProblemPagerAdapter(this, problemId, courseId, compilerType)
        binding.viewPager.adapter = adapter

        // Link TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Problem"
                1 -> "Editor"
                2 -> "Solution"
                else -> ""
            }
        }.attach()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * ViewPager2 Adapter for the 3 fragments
     */
    private class ProblemPagerAdapter(
        activity: AppCompatActivity,
        private val problemId: String,
        private val courseId: String,
        private val compilerType: String
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProblemFragment.newInstance(problemId, courseId, compilerType)
                1 -> EditorFragment.newInstance(problemId, courseId, compilerType)
                2 -> SolutionFragment.newInstance(problemId, courseId, compilerType)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}
