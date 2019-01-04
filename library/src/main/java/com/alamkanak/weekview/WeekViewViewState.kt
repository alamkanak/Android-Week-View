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

    fun update(config: WeekViewConfig, listener: UpdateListener) {
        if (!areDimensionsInvalid) {
            return
        }

        val totalHeaderHeight = (config.drawingConfig.headerHeight
                + (config.headerRowPadding * 2).toFloat()
                + config.drawingConfig.headerMarginBottom)

        val height = WeekView.getViewHeight()

        config.effectiveMinHourHeight = max(
                config.minHourHeight,
                ((height - totalHeaderHeight) / Constants.HOURS_PER_DAY).toInt()
        )

        areDimensionsInvalid = false
        scrollToDay?.let {
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
