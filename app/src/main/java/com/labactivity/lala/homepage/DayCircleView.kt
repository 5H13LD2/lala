package com.labactivity.lala.homepage

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.labactivity.lala.R
import kotlin.math.sin

/**
 * Enhanced DayCircleView with water-fill animation
 * Shows quiz results with color-coded liquid effects
 */
class DayCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var isChecked = false
    private var dayStatus: DayStatus = DayStatus.NONE
    private var waterFillLevel = 0f  // 0.0 to 1.0
    private var waveOffset = 0f      // For wave animation

    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val checkmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val wavePath = Path()
    private var waveAnimator: ValueAnimator? = null

    init {
        // Start continuous wave animation
        startWaveAnimation()
    }

    /**
     * Set the day status and animate the water fill
     */
    fun setDayStatus(status: DayStatus, fillPercentage: Float = 1f, animate: Boolean = true) {
        dayStatus = status
        val targetFill = when (status) {
            DayStatus.NONE -> 0f
            DayStatus.PASSED -> fillPercentage
            DayStatus.FAILED -> fillPercentage
            DayStatus.MIXED -> fillPercentage
        }

        if (animate) {
            animateWaterFill(targetFill)
        } else {
            waterFillLevel = targetFill
            invalidate()
        }
    }

    fun setChecked(checked: Boolean) {
        if (isChecked != checked) {
            isChecked = checked
            refreshDrawableState()
            invalidate()
        }
    }

    fun isChecked(): Boolean = isChecked

    /**
     * Animate the water filling up
     */
    private fun animateWaterFill(targetLevel: Float) {
        ValueAnimator.ofFloat(waterFillLevel, targetLevel).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                waterFillLevel = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * Start continuous wave animation for the water
     */
    private fun startWaveAnimation() {
        waveAnimator?.cancel()
        waveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                waveOffset = animation.animatedValue as Float
                if (waterFillLevel > 0) {
                    invalidate()
                }
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Draw water fill if there's a status
        if (waterFillLevel > 0 && dayStatus != DayStatus.NONE) {
            drawWaterFill(canvas)
        }

        // Draw text on top
        super.onDraw(canvas)

        // Draw checkmark if checked
        if (isChecked) {
            drawCheckmark(canvas)
        }
    }

    /**
     * Draw animated water fill with wave effect
     */
    private fun drawWaterFill(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val radius = minOf(w, h) / 2f

        // Set water color based on status
        waterPaint.color = when (dayStatus) {
            DayStatus.PASSED -> ContextCompat.getColor(context, R.color.success_green)
            DayStatus.FAILED -> ContextCompat.getColor(context, R.color.error_red)
            DayStatus.MIXED -> Color.parseColor("#FFA726")  // Orange
            else -> Color.TRANSPARENT
        }

        // Calculate water level
        val waterHeight = h * waterFillLevel
        val waterY = h - waterHeight

        // Create circular clip path
        val clipPath = Path().apply {
            addCircle(w / 2f, h / 2f, radius, Path.Direction.CW)
        }

        canvas.save()
        canvas.clipPath(clipPath)

        // Draw wave
        wavePath.reset()
        wavePath.moveTo(0f, h)

        // Wave parameters
        val waveAmplitude = 4f
        val waveLength = w / 2f
        val phaseShift = waveOffset * Math.PI.toFloat() * 2

        // Draw wave curve
        for (x in 0..w.toInt()) {
            val xProgress = x / w
            val y = waterY + sin((xProgress * waveLength + phaseShift).toDouble()).toFloat() * waveAmplitude
            if (x == 0) {
                wavePath.moveTo(x.toFloat(), y)
            } else {
                wavePath.lineTo(x.toFloat(), y)
            }
        }

        // Complete the path
        wavePath.lineTo(w, h)
        wavePath.lineTo(0f, h)
        wavePath.close()

        // Draw the water
        canvas.drawPath(wavePath, waterPaint)
        canvas.restore()
    }

    /**
     * Draw checkmark for current day
     */
    private fun drawCheckmark(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        val startX = w * 0.3f
        val startY = h * 0.5f
        val middleX = w * 0.45f
        val middleY = h * 0.65f
        val endX = w * 0.7f
        val endY = h * 0.35f

        val checkPath = Path().apply {
            moveTo(startX, startY)
            lineTo(middleX, middleY)
            lineTo(endX, endY)
        }

        canvas.drawPath(checkPath, checkmarkPaint)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            mergeDrawableStates(drawableState, intArrayOf(android.R.attr.state_checked))
        }
        return drawableState
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator?.cancel()
    }
}