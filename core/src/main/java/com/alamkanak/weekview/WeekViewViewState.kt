package com.alamkanak.weekview

import java.lang.Math.max
import java.util.Calendar

internal class WeekViewViewState(
    private val configWrapper: WeekViewConfigWrapper,
    private val listener: Listener
) {

    var scrollToDay: Calendar? = null
    var scrollToHour: Int? = null

    private var isFirstDraw = true
    var areDimensionsInvalid = true

    var firstVisibleDay: Calendar? = null
    var lastVisibleDay: Calendar? = null

    var shouldRefreshEvents: Boolean = false

    fun update(viewHeight: Int) {
        val totalHeaderHeight = configWrapper.getTotalHeaderHeight().toInt()
        val dynamicHourHeight = (viewHeight - totalHeaderHeight) / configWrapper.hoursPerDay

        if (areDimensionsInvalid) {
            configWrapper.effectiveMinHourHeight = max(configWrapper.minHourHeight, dynamicHourHeight)

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

        if (isFirstDraw) {
            configWrapper.moveCurrentOriginIfFirstDraw()
            isFirstDraw = false
        }
    }

    fun invalidate() {
        areDimensionsInvalid = false
    }

    internal interface Listener {
        fun goToDate(date: Calendar)
        fun goToHour(hour: Int)
    }

}
