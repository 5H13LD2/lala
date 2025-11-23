package com.labactivity.lala.DAILYPROBLEMPAGE

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DailyProblemViewModel(
    private val repository: DailyProblemRepository = DailyProblemRepository()
) : ViewModel() {

    private val _activeProblem = MutableStateFlow<DailyProblem?>(null)
    val activeProblem: StateFlow<DailyProblem?> = _activeProblem.asStateFlow()

    private val _userProgress = MutableStateFlow<DailyProblemProgress?>(null)
    val userProgress: StateFlow<DailyProblemProgress?> = _userProgress.asStateFlow()

    private val _timeRemaining = MutableStateFlow<TimeRemaining>(TimeRemaining())
    val timeRemaining: StateFlow<TimeRemaining> = _timeRemaining.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isProblemExpired = MutableStateFlow(false)
    val isProblemExpired: StateFlow<Boolean> = _isProblemExpired.asStateFlow()

    private var countdownJob: Job? = null

    init {
        fetchActiveDailyProblem()
    }

    fun fetchActiveDailyProblem() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getActiveDailyProblems().collect { result ->
                _isLoading.value = false
                result.onSuccess { problems ->
                    if (problems.isNotEmpty()) {
                        val problem = problems.first() // Get the latest problem
                        _activeProblem.value = problem
                        _isProblemExpired.value = false
                        startCountdown(problem.expiredAt)
                        fetchUserProgress(problem.problemId)
                    } else {
                        _activeProblem.value = null
                        _isProblemExpired.value = true
                        stopCountdown()
                    }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to fetch daily problem"
                }
            }
        }
    }

    private fun fetchUserProgress(problemId: String) {
        viewModelScope.launch {
            repository.getUserProgress(problemId).onSuccess { progress ->
                _userProgress.value = progress
            }
        }
    }

    fun submitSolution(
        problemId: String,
        courseId: String,
        code: String,
        status: String,
        score: Int = 0,
        executionTime: Long = 0,
        testCasesPassed: Int = 0,
        totalTestCases: Int = 0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.submitProblemSolution(
                problemId = problemId,
                courseId = courseId,
                code = code,
                status = status,
                score = score,
                executionTime = executionTime,
                testCasesPassed = testCasesPassed,
                totalTestCases = totalTestCases
            ).onSuccess {
                fetchUserProgress(problemId)
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to submit solution"
            }
            _isLoading.value = false
        }
    }

    private fun startCountdown(expiredAt: Timestamp?) {
        stopCountdown() // Stop any existing countdown

        if (expiredAt == null) return

        countdownJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val expiryTime = expiredAt.toDate().time
                val difference = expiryTime - currentTime

                if (difference <= 0) {
                    // Problem expired
                    _isProblemExpired.value = true
                    _timeRemaining.value = TimeRemaining()
                    _activeProblem.value = null
                    break
                }

                val hours = TimeUnit.MILLISECONDS.toHours(difference)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(difference) % 60

                _timeRemaining.value = TimeRemaining(
                    hours = hours.toInt(),
                    minutes = minutes.toInt(),
                    seconds = seconds.toInt()
                )

                delay(1000) // Update every second
            }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Load a specific problem by ID (for EditorFragment)
     */
    fun loadProblemById(problemId: String, callback: (DailyProblem?) -> Unit) {
        viewModelScope.launch {
            repository.getDailyProblemById(problemId).onSuccess { problem ->
                callback(problem)
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load problem"
                callback(null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCountdown()
    }
}
