package com.alamkanak.weekview

import java.lang.Math.max
import java.util.*

internal class WeekViewViewState {

    var scrollToDay: Calendar? = null
    var scrollToHour: Int? = null

    var isFirstDraw = true

    @JvmField
    var areDimensionsInvalid = true

    var firstVisibleDay: Calendar? = null
    var lastVisibleDay: Calendar? = null

    var shouldRefreshEvents: Boolean = false

    @JvmField
    var requiresPostInvalidateOnAnimation: Boolean = false

    fun update(config: WeekViewConfig, listener: UpdateListener) {
        val height = WeekView.getViewHeight()

        config.effectiveMinHourHeight = max(
                config.minHourHeight,
                ((height - config.drawingConfig.headerHeight) / Constants.HOURS_PER_DAY).toInt()
        )

        areDimensionsInvalid = false
        scrollToDay?.let {
            isFirstDraw = false
            listener.goToDate(it)
        }

        areDimensionsInvalid = false
        scrollToHour?.let {
            listener.goToHour(it)
        }

        scrollToDay = null
        scrollToHour = -1
        areDimensionsInvalid = false
    }

    fun invalidate() {
        areDimensionsInvalid = false
    }

    internal interface UpdateListener {
        fun goToDate(date: Calendar)
        fun goToHour(hour: Int)
    }

}
