package com.alamkanak.weekview

import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.Calendar

internal class Navigator(
    private val viewState: ViewState,
    private val listener: NavigationListener
) {

    private val animator = ValueAnimator()

    val isNotRunning: Boolean
        get() = !animator.isRunning

    fun scrollHorizontallyBy(distance: Float) {
        viewState.currentOrigin.x -= distance
        viewState.currentOrigin.x = viewState.currentOrigin.x.coerceIn(
            minimumValue = viewState.minX,
            maximumValue = viewState.maxX
        )
        listener.onHorizontalScrollPositionChanged()
    }

    fun scrollVerticallyBy(distance: Float) {
        viewState.currentOrigin.y -= distance
        listener.onVerticalScrollPositionChanged()
    }

    fun scrollHorizontallyTo(date: Calendar, onFinished: () -> Unit = {}) {
        val destinationOffset = viewState.getXOriginForDate(date)
        val adjustedDestinationOffset = destinationOffset.coerceIn(
            minimumValue = viewState.minX,
            maximumValue = viewState.maxX
        )
        scrollHorizontallyTo(offset = adjustedDestinationOffset, onFinished = onFinished)

//        animator.animate(
//            fromValue = viewState.currentOrigin.x,
//            toValue = adjustedDestinationOffset,
//            onUpdate = {
//                viewState.currentOrigin.x = it
//                listener.onHorizontalScrollPositionChanged()
//            },
//            onEnd = {
//                listener.onHorizontalScrollingFinished()
//                onFinished()
//            }
//        )
    }

    fun scrollHorizontallyTo(offset: Float, onFinished: () -> Unit = {}) {
        animator.animate(
            fromValue = viewState.currentOrigin.x,
            toValue = offset,
            onUpdate = {
                viewState.currentOrigin.x = it
                listener.onHorizontalScrollPositionChanged()
            },
            onEnd = {
                if (Build.VERSION.SDK_INT > 25) {
                    listener.onHorizontalScrollingFinished()
                } else {
                    // Delay calling the listener to avoid navigator.isNotRunning still
                    // being false on API 25 and below.
                    // See: https://github.com/thellmund/Android-Week-View/issues/227
                    Handler(Looper.getMainLooper()).post {
                        listener.onHorizontalScrollingFinished()
                    }
                }
            }
        )
    }

    fun scrollVerticallyTo(offset: Float) {
        val dayHeight = viewState.hourHeight * viewState.hoursPerDay
        val viewHeight = viewState.viewHeight

        val minY = (dayHeight + viewState.headerHeight - viewHeight) * -1
        val maxY = 0f

        val finalOffset = offset.coerceIn(
            minimumValue = minY,
            maximumValue = maxY
        )

        animator.animate(
            fromValue = viewState.currentOrigin.y,
            toValue = finalOffset,
            onUpdate = {
                viewState.currentOrigin.y = it
                listener.onVerticalScrollPositionChanged()
            }
        )
    }

    fun stop() {
        animator.stop()
    }

    fun requestInvalidation() {
        listener.requestInvalidation()
    }

    internal interface NavigationListener {
        fun onHorizontalScrollPositionChanged()
        fun onHorizontalScrollingFinished()
        fun onVerticalScrollPositionChanged()
        fun requestInvalidation()
    }
}
