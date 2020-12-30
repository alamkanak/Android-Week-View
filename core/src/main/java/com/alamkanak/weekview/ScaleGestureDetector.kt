package com.alamkanak.weekview

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector as AndroidScaleGestureDetector

internal class ScaleGestureDetector(
    context: Context,
    private val viewState: ViewState,
    private val navigator: Navigator
) {

    private val listener = object : AndroidScaleGestureDetector.OnScaleGestureListener {

        override fun onScaleBegin(
            detector: AndroidScaleGestureDetector
        ): Boolean = navigator.isNotRunning

        override fun onScale(detector: AndroidScaleGestureDetector): Boolean {
            val hourHeight = viewState.hourHeight
            viewState.newHourHeight = hourHeight * detector.scaleFactor
            navigator.requestInvalidation()
            return true
        }

        override fun onScaleEnd(detector: AndroidScaleGestureDetector) {
            navigator.requestInvalidation()
        }
    }

    private val detector = AndroidScaleGestureDetector(context, listener)

    fun onTouchEvent(event: MotionEvent) {
        if (navigator.isNotRunning && !viewState.showCompleteDay) {
            detector.onTouchEvent(event)
        }
    }
}
