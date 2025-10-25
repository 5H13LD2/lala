package com.labactivity.lala.UTILS

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Animation utilities for creating smooth, professional UI interactions
 * Design System: Modern Material Design with smooth transitions
 */
object AnimationUtils {

    // ==============================================
    // FADE ANIMATIONS
    // ==============================================

    /**
     * Fade in a view with smooth alpha transition
     * @param duration Animation duration in milliseconds (default: 300ms)
     * @param startDelay Optional delay before animation starts
     */
    fun View.fadeIn(duration: Long = 300, startDelay: Long = 0) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(startDelay)
            .setInterpolator(DecelerateInterpolator())
            .setListener(null)
            .start()
    }

    /**
     * Fade out a view and optionally hide it
     * @param duration Animation duration in milliseconds (default: 300ms)
     * @param hideOnComplete If true, sets visibility to GONE after animation
     */
    fun View.fadeOut(duration: Long = 300, hideOnComplete: Boolean = true) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (hideOnComplete) visibility = View.GONE
                }
            })
            .start()
    }

    // ==============================================
    // SLIDE ANIMATIONS
    // ==============================================

    /**
     * Slide view up from bottom with fade in
     * Perfect for cards, dialogs, and content sections
     */
    fun View.slideUpFadeIn(duration: Long = 400, startDelay: Long = 0) {
        alpha = 0f
        translationY = 100f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setStartDelay(startDelay)
            .setInterpolator(DecelerateInterpolator())
            .setListener(null)
            .start()
    }

    /**
     * Slide view down and fade out
     */
    fun View.slideDownFadeOut(duration: Long = 300, hideOnComplete: Boolean = true) {
        animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (hideOnComplete) visibility = View.GONE
                }
            })
            .start()
    }

    // ==============================================
    // SCALE ANIMATIONS
    // ==============================================

    /**
     * Scale and fade in animation (great for popup elements)
     */
    fun View.scaleIn(duration: Long = 300, startDelay: Long = 0) {
        alpha = 0f
        scaleX = 0.7f
        scaleY = 0.7f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setStartDelay(startDelay)
            .setInterpolator(OvershootInterpolator())
            .setListener(null)
            .start()
    }

    /**
     * Button press animation - subtle scale down effect
     * Use this for click feedback on buttons and cards
     */
    fun View.animatePress(onComplete: (() -> Unit)? = null) {
        animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction { onComplete?.invoke() }
                    .start()
            }
            .start()
    }

    // ==============================================
    // RECYCLERVIEW ANIMATIONS
    // ==============================================

    /**
     * Staggered fade-in animation for RecyclerView items
     * Each item animates in sequence with a slight delay
     *
     * Usage: Call this in your Fragment/Activity after setting up the adapter
     * Example: recyclerView.animateItems()
     */
    fun RecyclerView.animateItems(
        itemDelay: Long = 80,
        itemDuration: Long = 300
    ) {
        layoutManager?.let { layoutManager ->
            val childCount = layoutManager.childCount
            for (i in 0 until childCount) {
                val child = layoutManager.getChildAt(i) ?: continue
                child.alpha = 0f
                child.translationY = 50f

                child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(itemDuration)
                    .setStartDelay(i * itemDelay)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    /**
     * Attach staggered animation to new RecyclerView items as they're added
     */
    fun RecyclerView.enableItemAnimations() {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val childCount = recyclerView.childCount
                for (i in 0 until childCount) {
                    val child = recyclerView.getChildAt(i)
                    if (!ViewCompat.hasTransientState(child)) {
                        // Only animate if not already animated
                        if (child.alpha < 1f) {
                            child.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(300)
                                .setInterpolator(DecelerateInterpolator())
                                .start()
                        }
                    }
                }
            }
        })
    }

    // ==============================================
    // PULSE ANIMATION
    // ==============================================

    /**
     * Pulse animation for attention-grabbing elements
     * Great for notifications, badges, or important buttons
     */
    fun View.pulse(duration: Long = 1000, repeatCount: Int = ValueAnimator.INFINITE) {
        val scaleUp = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.1f).apply {
            this.duration = duration / 2
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleDown = ObjectAnimator.ofFloat(this, "scaleX", 1.1f, 1f).apply {
            this.duration = duration / 2
            interpolator = AccelerateDecelerateInterpolator()
        }

        scaleUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                scaleDown.start()
            }
        })

        scaleDown.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (repeatCount == ValueAnimator.INFINITE || scaleUp.repeatCount < repeatCount) {
                    scaleUp.start()
                }
            }
        })

        // Apply same animation to Y scale
        ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.1f, 1f).apply {
            this.duration = duration
            this.repeatCount = repeatCount
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        scaleUp.start()
    }

    // ==============================================
    // CONTAINER ANIMATIONS
    // ==============================================

    /**
     * Animate all children of a ViewGroup with staggered delays
     * Perfect for section headers, card contents, etc.
     */
    fun ViewGroup.animateChildren(
        childDelay: Long = 100,
        childDuration: Long = 300
    ) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.slideUpFadeIn(
                duration = childDuration,
                startDelay = i * childDelay
            )
        }
    }

    // ==============================================
    // CARD ANIMATIONS
    // ==============================================

    /**
     * Card click animation with elevation change simulation
     * Combines scale and alpha for a "press" effect
     */
    fun View.animateCardPress(onClick: () -> Unit) {
        setOnClickListener {
            animatePress {
                onClick()
            }
        }
    }
}
