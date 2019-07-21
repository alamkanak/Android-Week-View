package com.alamkanak.weekview

import java.lang.Math.max
import java.util.Calendar

internal class WeekViewViewState(
    private val configWrapper: WeekViewConfigWrapper,
    private val listener: Listener
) {

    var scrollToDate: Calendar? = null
    var scrollToHour: Int? = null

    private var isFirstDraw = true
    var areDimensionsInvalid = true

    var firstVisibleDate: Calendar? = null
    var lastVisibleDate: Calendar? = null

    fun update(viewHeight: Int) {
        val totalHeaderHeight = configWrapper.getTotalHeaderHeight().toInt()
        val dynamicHourHeight = (viewHeight - totalHeaderHeight) / configWrapper.hoursPerDay

        if (areDimensionsInvalid) {
            configWrapper.effectiveMinHourHeight = max(configWrapper.minHourHeight, dynamicHourHeight)

            areDimensionsInvalid = false
            scrollToDate?.let {
                isFirstDraw = false
                listener.goToDate(it)
            }

            areDimensionsInvalid = false
            scrollToHour?.let {
                listener.goToHour(it)
            }

            scrollToDate = null
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
