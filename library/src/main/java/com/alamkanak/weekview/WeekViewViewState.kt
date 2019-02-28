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
        val drawingConfig = config.drawingConfig
        val totalHeaderHeight = drawingConfig.getTotalHeaderHeight(config)

        val totalHeight = WeekView.getViewHeight()
        val dynamicHourHeight = ((totalHeight - totalHeaderHeight) / config.hoursPerDay).toInt()

        config.effectiveMinHourHeight = max(config.minHourHeight, dynamicHourHeight)

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
        scrollToHour = null
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
