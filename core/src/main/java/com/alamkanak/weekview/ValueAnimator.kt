package com.alamkanak.weekview

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
        onUpdate: (Float) -> Unit
    ) {
        valueAnimator?.cancel()

        valueAnimator = AndroidValueAnimator.ofFloat(fromValue, toValue).apply {
            setDuration(duration)
            interpolator = DecelerateInterpolator()

            addUpdateListener {
                val value = it.animatedValue as Float
                onUpdate(value)
            }

            start()
        }
    }

    fun stop() {
        valueAnimator?.cancel()
    }
}
