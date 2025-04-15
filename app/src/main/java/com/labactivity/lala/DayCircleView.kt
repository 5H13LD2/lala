package com.labactivity.lala

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class DayCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var isChecked = false
    private val checkmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    fun setChecked(checked: Boolean) {
        if (isChecked != checked) {
            isChecked = checked
            refreshDrawableState()
            invalidate()
        }
    }

    fun isChecked(): Boolean = isChecked

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isChecked) {
            // Draw checkmark
            val width = width.toFloat()
            val height = height.toFloat()

            val startX = width * 0.3f
            val startY = height * 0.5f
            val middleX = width * 0.45f
            val middleY = height * 0.65f
            val endX = width * 0.7f
            val endY = height * 0.35f

            val checkPath = Path().apply {
                moveTo(startX, startY)
                lineTo(middleX, middleY)
                lineTo(endX, endY)
            }

            canvas.drawPath(checkPath, checkmarkPaint)
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            mergeDrawableStates(drawableState, intArrayOf(android.R.attr.state_checked))
        }
        return drawableState
    }
}