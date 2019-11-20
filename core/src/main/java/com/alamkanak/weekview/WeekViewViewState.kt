package com.alamkanak.weekview

import java.util.Calendar
import kotlin.math.max

internal class WeekViewViewState(
    private val config: WeekViewConfigWrapper,
    private val listener: Listener
) {

    var scrollToDate: Calendar? = null
    var scrollToHour: Int? = null

    private var isFirstDraw = true
    var areDimensionsInvalid = true

    var firstVisibleDate: Calendar? = null
    var lastVisibleDate: Calendar? = null

    fun update(viewHeight: Int) {
        val totalHeaderHeight = config.getTotalHeaderHeight().toInt()
        val dynamicHourHeight = (viewHeight - totalHeaderHeight) / config.hoursPerDay

        if (areDimensionsInvalid) {
            config.effectiveMinHourHeight = max(config.minHourHeight, dynamicHourHeight)

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
            config.moveCurrentOriginIfFirstDraw()
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
