package com.labactivity.lala.UTILS

import android.content.Context

/**
 * Extension functions for recording assessment results
 * Use these in your assessment/quiz activities
 */

/**
 * Record a passed assessment
 * Call this when user successfully completes a quiz/challenge
 */
fun Context.recordAssessmentPassed() {
    AssessmentResultTracker.recordResult(this, passed = true)
}

/**
 * Record a failed assessment
 * Call this when user fails a quiz/challenge
 */
fun Context.recordAssessmentFailed() {
    AssessmentResultTracker.recordResult(this, passed = false)
}

/**
 * Example usage in CompilerActivity or QuizActivity:
 *
 * ```kotlin
 * if (userCodeOutput == correctOutput) {
 *     // User passed!
 *     recordAssessmentPassed()
 *     showSuccessDialog()
 * } else {
 *     // User failed
 *     recordAssessmentFailed()
 *     showFailureDialog()
 * }
 * ```
 */
