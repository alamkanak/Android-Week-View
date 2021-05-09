package com.alamkanak.weekview

import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

    fun scrollHorizontallyTo(date: Calendar, onFinished: () -> Unit = {}) {
        val destinationOffset = viewState.getXOriginForDate(date)
        val adjustedDestinationOffset = destinationOffset.coerceIn(
            minimumValue = viewState.minX,
            maximumValue = viewState.maxX
        )
        scrollHorizontallyTo(offset = adjustedDestinationOffset, onFinished = onFinished)
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
                afterNavigationFinishes {
                    listener.onHorizontalScrollingFinished()
                    onFinished()
                }
            }
        )
    }

    fun scrollVerticallyBy(distance: Float) {
        val currentVerticalOffset = abs(viewState.currentOrigin.y)
        val isScrollingDown = distance > 0
        val isScrollingUp = distance < 0

        if (currentVerticalOffset == 0f && isScrollingUp) {
            // Trying to scroll up when already at the top
            return
        }

        val maxY = viewState.dayHeight - (viewState.headerHeight + viewState.calendarGridBounds.height())

        if (currentVerticalOffset == maxY && isScrollingDown) {
            // Trying to scroll down when already at the bottom
            return
        }

        val maxScrollDistance = if (isScrollingUp) {
            max(distance, currentVerticalOffset * -1f)
        } else {
            min(distance, maxY - currentVerticalOffset)
        }

        viewState.currentOrigin.y -= maxScrollDistance
        listener.onVerticalScrollPositionChanged(distance = maxScrollDistance)
    }

    fun notifyVerticalScrollingFinished() {
        listener.onVerticalScrollingFinished()
    }

    fun scrollVerticallyTo(offset: Float) {
        val dayHeight = viewState.hourHeight * viewState.hoursPerDay
        val viewHeight = viewState.viewHeight

        val minY = (dayHeight + viewState.headerHeight - viewHeight) * -1
        val maxY = 0f

        val finalOffset = offset.coerceIn(
            minimumValue = minY,
            maximumValue = max(minY, maxY)
        )

        animator.animate(
            fromValue = viewState.currentOrigin.y,
            toValue = finalOffset,
            onUpdate = { newOffset ->
                val currentOffset = viewState.currentOrigin.y
                viewState.currentOrigin.y = newOffset

                val distance = abs(newOffset) - abs(currentOffset)
                listener.onVerticalScrollPositionChanged(distance = distance)
            },
            onEnd = {
                afterNavigationFinishes {
                    listener.onVerticalScrollingFinished()
                }
            }
        )
    }

    fun stop() {
        animator.stop()
    }

    fun requestInvalidation() {
        listener.requestInvalidation()
    }

    private fun afterNavigationFinishes(block: () -> Unit) {
        if (Build.VERSION.SDK_INT > 25) {
            block()
        } else {
            // Delay calling the listener to avoid navigator.isNotRunning still
            // being false on API 25 and below.
            // See: https://github.com/thellmund/Android-Week-View/issues/227
            Handler(Looper.getMainLooper()).post(block)
        }
    }

    internal interface NavigationListener {
        fun onHorizontalScrollPositionChanged()
        fun onHorizontalScrollingFinished()
        fun onVerticalScrollPositionChanged(distance: Float)
        fun onVerticalScrollingFinished()
        fun requestInvalidation()
    }
}
