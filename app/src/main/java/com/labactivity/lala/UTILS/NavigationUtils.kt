package com.labactivity.lala.UTILS

import android.app.Activity
import android.content.Intent
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView

// ==============================================
// SAFE CLICK LISTENER (ANTI DOUBLE CLICK)
// ==============================================
class SafeClickListener(
    private var defaultInterval: Int = 1000, // 1 SECOND INTERVAL
    private val onSafeClick: (View) -> Unit
) : View.OnClickListener {

    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTimeClicked >= defaultInterval) {
            lastTimeClicked = currentTime
            onSafeClick(v)
        }
    }
}

// ==============================================
// EXTENSION FUNCTION FOR SAFE CLICK
// ==============================================
fun View.setSafeOnClickListener(interval: Int = 1000, onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener(interval, onSafeClick)
    setOnClickListener(safeClickListener)
}

// ==============================================
// SAFE NAVIGATION FOR BOTTOM NAVIGATION VIEW
// ==============================================
fun BottomNavigationView.setupWithSafeNavigation(
    activity: Activity,
    currentActivityClass: Class<*>,
    navMap: Map<Int, Class<*>>
) {
    setOnItemSelectedListener { item ->
        val targetActivity = navMap[item.itemId]

        if (targetActivity != null) {
            // ✅ HUWAG I-OPEN ULIT KUNG NASA SAME ACTIVITY KA NA
            if (currentActivityClass != targetActivity) {
                activity.startActivity(Intent(activity, targetActivity))
                activity.overridePendingTransition(0, 0) // OPTIONAL: NO ANIMATION
                activity.finish() // OPTIONAL: CLOSE CURRENT
            }
            true
        } else {
            false
        }
    }
}
