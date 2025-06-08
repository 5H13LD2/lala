package com.labactivity.lala

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query



class LeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvCourseTitle: TextView

    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var firestore: FirebaseFirestore

    // Course ID - can be passed as argument or set as constant
    private var courseId: String = "python_course"
    private var courseName: String = "Python Programming"

    companion object {
        private const val TAG = "LeaderboardFragment"
        private const val ARG_COURSE_ID = "course_id"
        private const val ARG_COURSE_NAME = "course_name"

        fun newInstance(courseId: String, courseName: String): LeaderboardFragment {
            val fragment = LeaderboardFragment()
            val args = Bundle().apply {
                putString(ARG_COURSE_ID, courseId)
                putString(ARG_COURSE_NAME, courseName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get arguments if provided
        arguments?.let {
            courseId = it.getString(ARG_COURSE_ID, "python_course")
            courseName = it.getString(ARG_COURSE_NAME, "Python Programming")
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        loadLeaderboard()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewLeaderboard)
        progressBar = view.findViewById(R.id.progressBar)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvCourseTitle = view.findViewById(R.id.tvCourseTitle)

        // Set course title
        tvCourseTitle.text = courseName
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter()
        recyclerView.apply {
            adapter = leaderboardAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun loadLeaderboard() {
        showLoading(true)

        firestore.collection("leaderboard")
            .document(courseId)
            .collection("users")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val leaderboardItems = mutableListOf<LeaderboardItem>()

                documents.forEachIndexed { index, document ->
                    val username = document.getString("username") ?: "Unknown User"
                    val score = document.getLong("score")?.toInt() ?: 0
                    val rank = index + 1

                    leaderboardItems.add(
                        LeaderboardItem.fromFirestore(username, score, rank)
                    )
                }

                showLoading(false)

                if (leaderboardItems.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showLeaderboard(leaderboardItems)
                }

                Log.d(TAG, "Loaded ${leaderboardItems.size} leaderboard entries")
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                showError(true)
                Log.e(TAG, "Error loading leaderboard", exception)
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        tvErrorMessage.visibility = View.GONE
        tvEmptyState.visibility = View.GONE
    }

    private fun showError(show: Boolean) {
        tvErrorMessage.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        tvEmptyState.visibility = View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        tvEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        tvErrorMessage.visibility = View.GONE
    }

    private fun showLeaderboard(items: List<LeaderboardItem>) {
        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        tvErrorMessage.visibility = View.GONE
        tvEmptyState.visibility = View.GONE

        leaderboardAdapter.submitList(items)
    }

    fun refreshLeaderboard() {
        loadLeaderboard()
    }
}