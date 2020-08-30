package com.alamkanak.weekview

import android.animation.Animator
import android.animation.ValueAnimator as AndroidValueAnimator
import android.view.animation.DecelerateInterpolator

internal class ValueAnimator {

    private var valueAnimator: AndroidValueAnimator? = null

    val isRunning: Boolean
        get() = valueAnimator?.isStarted ?: false

    fun animate(
        fromValue: Float,
        toValue: Float,
        duration: Long = 300,
        onUpdate: (Float) -> Unit,
        onEnd: () -> Unit = {}
    ) {
        valueAnimator?.cancel()

        valueAnimator = AndroidValueAnimator.ofFloat(fromValue, toValue).apply {
            setDuration(duration)
            interpolator = DecelerateInterpolator()

            addUpdateListener {
                val value = it.animatedValue as Float
                onUpdate(value)
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animator: Animator?) { onEnd() }
                override fun onAnimationStart(animator: Animator?) = Unit
                override fun onAnimationCancel(animator: Animator?) = Unit
                override fun onAnimationRepeat(animator: Animator?) = Unit
            })

            start()
        }
    }

    fun stop() {
        valueAnimator?.cancel()
    }
}
