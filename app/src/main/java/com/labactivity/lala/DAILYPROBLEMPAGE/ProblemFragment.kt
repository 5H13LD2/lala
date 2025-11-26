package com.labactivity.lala.DAILYPROBLEMPAGE

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.labactivity.lala.databinding.FragmentProblemBinding
import kotlinx.coroutines.launch

/**
 * Fragment to display the problem statement and details
 */
class ProblemFragment : Fragment() {

    private var _binding: FragmentProblemBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DailyProblemViewModel

    private var problemId: String = ""
    private var courseId: String = ""
    private var compilerType: String = ""

    private var currentHintIndex = 0
    private var hints: List<String> = emptyList()

    companion object {
        private const val ARG_PROBLEM_ID = "problem_id"
        private const val ARG_COURSE_ID = "course_id"
        private const val ARG_COMPILER_TYPE = "compiler_type"

        fun newInstance(problemId: String, courseId: String, compilerType: String): ProblemFragment {
            return ProblemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROBLEM_ID, problemId)
                    putString(ARG_COURSE_ID, courseId)
                    putString(ARG_COMPILER_TYPE, compilerType)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            problemId = it.getString(ARG_PROBLEM_ID) ?: ""
            courseId = it.getString(ARG_COURSE_ID) ?: ""
            compilerType = it.getString(ARG_COMPILER_TYPE) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get ViewModel from activity
        viewModel = DailyProblemViewModel()

        observeProblem()
        setupClickListeners()
    }

    private fun observeProblem() {
        lifecycleScope.launch {
            viewModel.activeProblem.collect { problem ->
                if (problem != null && problem.problemId == problemId) {
                    // Display problem details
                    binding.tvProblemTitle.text = problem.title
                    binding.tvDescription.text = problem.description
                    binding.tvDifficulty.text = problem.difficulty

                    // Set difficulty color
                    val difficultyColor = when (problem.difficulty.lowercase()) {
                        "easy" -> android.graphics.Color.parseColor("#4CAF50")
                        "medium" -> android.graphics.Color.parseColor("#FF9800")
                        "hard" -> android.graphics.Color.parseColor("#F44336")
                        else -> android.graphics.Color.GRAY
                    }
                    binding.tvDifficulty.setTextColor(difficultyColor)

                    // Display examples/test cases
                    val examplesText = buildString {
                        append("Examples:\n\n")
                        problem.testCases.forEachIndexed { index, testCase ->
                            if (!testCase.isHidden) {
                                append("Example ${index + 1}:\n")
                                append("Input: ${testCase.input}\n")
                                append("Output: ${testCase.expectedOutput}\n\n")
                            }
                        }
                    }
                    binding.tvExamples.text = examplesText

                    // Store hints for progressive reveal
                    hints = problem.hints
                    currentHintIndex = 0
                    updateHintButton()

                    // Show/hide solved/revision badges based on user progress
                    lifecycleScope.launch {
                        viewModel.userProgress.collect { progress ->
                            binding.tvSolved.isVisible = progress?.status == "completed"
                            binding.tvRevision.isVisible = progress?.status == "failed"
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Hint button - reveals hints one at a time
        binding.btnHint.setOnClickListener {
            if (hints.isNotEmpty() && currentHintIndex < hints.size) {
                // Show hints container
                binding.hintsContainer.isVisible = true

                // Build hints text up to current index
                val hintsToShow = hints.take(currentHintIndex + 1)
                val hintsText = hintsToShow.mapIndexed { index, hint ->
                    "${index + 1}. $hint"
                }.joinToString("\n\n")

                binding.tvHints.text = hintsText

                // Move to next hint
                currentHintIndex++

                // Update button text
                updateHintButton()
            }
        }
    }

    private fun updateHintButton() {
        when {
            hints.isEmpty() -> {
                binding.btnHint.isVisible = false
            }
            currentHintIndex >= hints.size -> {
                binding.btnHint.text = "No more hints"
                binding.btnHint.isEnabled = false
                binding.btnHint.alpha = 0.5f
            }
            currentHintIndex == 0 -> {
                binding.btnHint.text = "💡 Show Hint (${hints.size} available)"
                binding.btnHint.isEnabled = true
                binding.btnHint.alpha = 1f
            }
            else -> {
                val remaining = hints.size - currentHintIndex
                binding.btnHint.text = "💡 Next Hint ($remaining remaining)"
                binding.btnHint.isEnabled = true
                binding.btnHint.alpha = 1f
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}