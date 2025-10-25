package com.labactivity.lala.UTILS

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.R

/**
 * Testing activity for streak and assessment tracking features
 * This is for development/testing purposes only
 *
 * To use: Add this activity to AndroidManifest.xml and launch it
 */
class StreakTestingActivity : AppCompatActivity() {

    private lateinit var textStreakInfo: TextView
    private lateinit var textAssessmentInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create UI programmatically for testing
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            var padding = 32
        }

        textStreakInfo = TextView(this).apply {
            textSize = 16f
            setPadding(0, 16, 0, 16)
        }

        textAssessmentInfo = TextView(this).apply {
            textSize = 16f
            setPadding(0, 16, 0, 16)
        }

        layout.addView(TextView(this).apply {
            text = "Streak & Assessment Testing"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        })

        layout.addView(textStreakInfo)
        layout.addView(textAssessmentInfo)

        // Buttons for testing
        layout.addView(Button(this).apply {
            text = "Record Passed Assessment"
            setOnClickListener {
                recordAssessmentPassed()
                refreshInfo()
            }
        })

        layout.addView(Button(this).apply {
            text = "Record Failed Assessment"
            setOnClickListener {
                recordAssessmentFailed()
                refreshInfo()
            }
        })

        layout.addView(Button(this).apply {
            text = "Reset Streak"
            setOnClickListener {
                StreakManager.resetStreak(this@StreakTestingActivity)
                refreshInfo()
            }
        })

        layout.addView(Button(this).apply {
            text = "Clear All Results"
            setOnClickListener {
                AssessmentResultTracker.clearResults(this@StreakTestingActivity)
                refreshInfo()
            }
        })

        layout.addView(Button(this).apply {
            text = "Refresh"
            setOnClickListener { refreshInfo() }
        })

        setContentView(layout)
        refreshInfo()
    }

    private fun refreshInfo() {
        val streak = StreakManager.getCurrentStreak(this)
        val longest = StreakManager.getLongestStreak(this)
        val totalDays = StreakManager.getTotalDaysActive(this)

        textStreakInfo.text = """
            Current Streak: $streak days
            Longest Streak: $longest days
            Total Active Days: $totalDays
        """.trimIndent()

        val totalAssessments = AssessmentResultTracker.getTotalAssessmentsTaken(this)
        val passedAssessments = AssessmentResultTracker.getTotalPassedAssessments(this)

        textAssessmentInfo.text = """
            Total Assessments: $totalAssessments
            Passed: $passedAssessments
            Failed: ${totalAssessments - passedAssessments}
        """.trimIndent()
    }
}
