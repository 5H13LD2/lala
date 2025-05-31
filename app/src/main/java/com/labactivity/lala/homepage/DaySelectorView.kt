package com.labactivity.lala.homepage

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ViewDaySelectorBinding

class DaySelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewDaySelectorBinding
    private val dayViews = arrayOfNulls<DayCircleView>(7)
    private val dayLabels = arrayOf("M", "T", "W", "T", "F", "S", "S")

    private var onDaySelectedListener: ((Int, Boolean) -> Unit)? = null

    init {
        orientation = VERTICAL

        // Inflating the merge layout
        binding = ViewDaySelectorBinding.inflate(LayoutInflater.from(context), this)

        // Create day views programmatically
        val container = getChildAt(0) as LinearLayout

        for (i in 0 until 7) {
            val dayView = DayCircleView(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 4, 4, 4)
                }
                text = dayLabels[i]
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.day_circle_selector)
                setTextColor(resources.getColorStateList(R.color.day_text_color_selector, null))

                // Set fixed dimensions
                minimumWidth = resources.getDimensionPixelSize(R.dimen.day_circle_size)
                minimumHeight = resources.getDimensionPixelSize(R.dimen.day_circle_size)
            }

            dayView.setOnClickListener {
                val newState = !dayView.isChecked()
                dayView.setChecked(newState)
                onDaySelectedListener?.invoke(i, newState)
            }

            container.addView(dayView)
            dayViews[i] = dayView
        }
    }

    fun setDayChecked(dayIndex: Int, checked: Boolean) {
        if (dayIndex in 0..6) {
            dayViews[dayIndex]?.setChecked(checked)
        }
    }

    fun isDayChecked(dayIndex: Int): Boolean {
        return if (dayIndex in 0..6) {
            dayViews[dayIndex]?.isChecked() ?: false
        } else false
    }

    fun setOnDaySelectedListener(listener: (dayIndex: Int, isChecked: Boolean) -> Unit) {
        onDaySelectedListener = listener
    }
}