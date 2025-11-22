package com.labactivity.lala.DAILYPROBLEMPAGE

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.labactivity.lala.databinding.FragmentSolutionBinding
import kotlinx.coroutines.launch

/**
 * Fragment to display the solution and explanation
 */
class SolutionFragment : Fragment() {

    private var _binding: FragmentSolutionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DailyProblemViewModel

    private var problemId: String = ""
    private var courseId: String = ""
    private var compilerType: String = ""

    companion object {
        private const val ARG_PROBLEM_ID = "problem_id"
        private const val ARG_COURSE_ID = "course_id"
        private const val ARG_COMPILER_TYPE = "compiler_type"

        fun newInstance(problemId: String, courseId: String, compilerType: String): SolutionFragment {
            return SolutionFragment().apply {
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
        _binding = FragmentSolutionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = DailyProblemViewModel()

        observeSolution()
    }

    private fun observeSolution() {
        lifecycleScope.launch {
            viewModel.activeProblem.collect { problem ->
                if (problem != null && problem.problemId == problemId) {
                    // Display solution
                    // Note: Solution should only be visible after user has attempted or after deadline

                    // Check if user has attempted
                    viewModel.userProgress.collect { progress ->
                        if (progress != null || viewModel.isProblemExpired.value) {
                            // Show solution
                            displaySolution(problem)
                        } else {
                            // Hide solution until attempt is made
                            binding.tvSolutionCode.text = "Complete the problem first to see the solution!"
                            binding.tvExplanation.text = ""
                        }
                    }
                }
            }
        }
    }

    private fun displaySolution(problem: DailyProblem) {
        // Display solution code (this should be added to DailyProblem model)
        binding.tvSolutionCode.text = "// Solution will be available here\n// Check back after attempting the problem"

        // Display explanation
        binding.tvExplanation.text = buildString {
            append("Approach:\n\n")
            append(problem.description)
            append("\n\n")

            if (problem.hints.isNotEmpty()) {
                append("Hints:\n")
                problem.hints.forEachIndexed { index, hint ->
                    append("${index + 1}. $hint\n")
                }
            }

            append("\n\nTime Complexity: O(n)")
            append("\nSpace Complexity: O(1)")
            append("\n\nTags: ${problem.tags.joinToString(", ")}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
