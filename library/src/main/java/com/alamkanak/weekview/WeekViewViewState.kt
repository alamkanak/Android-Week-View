package com.alamkanak.weekview

import org.threeten.bp.LocalDate
import java.lang.Math.max
import java.util.*

internal class WeekViewViewState(
        private val configWrapper: WeekViewConfigWrapper
) {

    var scrollToDay: LocalDate? = null
    var scrollToHour: Int? = null

    var isFirstDraw = true
    var areDimensionsInvalid = true

    var firstVisibleDay: LocalDate? = null
    var lastVisibleDay: LocalDate? = null

    var shouldRefreshEvents: Boolean = false
    var requiresPostInvalidateOnAnimation: Boolean = false

    fun update(listener: UpdateListener) {
        val totalHeaderHeight = configWrapper.getTotalHeaderHeight()

        val totalHeight = WeekView.getViewHeight()
        val dynamicHourHeight = ((totalHeight - totalHeaderHeight) / configWrapper.hoursPerDay).toInt()

        configWrapper.effectiveMinHourHeight = max(configWrapper.minHourHeight, dynamicHourHeight)

        areDimensionsInvalid = false
        scrollToDay?.let {
            isFirstDraw = false
            listener.goToDate(it.toCalendar())
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
