package com.alamkanak.weekview

import java.util.Calendar
import kotlin.math.ceil

internal class DrawingContext {

    private var startPixel = 0f
    private val dateRange = mutableListOf<Calendar>()
    private val startPixels = mutableListOf<Float>()

    val dateRangeWithStartPixels = mutableListOf<Pair<Calendar, Float>>()

    fun update(config: WeekViewConfigWrapper) {
        val totalDayWidth = config.totalDayWidth
        val originX = config.currentOrigin.x

        val daysFromOrigin = ceil(originX / totalDayWidth).toInt() * (-1)
        startPixel = config.timeColumnWidth + originX + totalDayWidth * daysFromOrigin

        val start = daysFromOrigin + 1
        val end = start + config.numberOfVisibleDays

        // If the user is scrolling, a new view becomes partially visible, so we must add an
        // additional date to the date range
        val isNotScrolling = originX % config.totalDayWidth == 0f
        val modifiedEnd = if (isNotScrolling) end - 1 else end

        dateRange.clear()
        dateRange += getDateRange(start, modifiedEnd)

        updateStartPixels(config, startPixel)

        dateRangeWithStartPixels.clear()
        dateRangeWithStartPixels += dateRange.zip(startPixels)
    }

    private fun updateStartPixels(config: WeekViewConfigWrapper, startPixel: Float) {
        startPixels.clear()

        val singleDayMargin = config.eventMarginHorizontal.toFloat()

        startPixels += dateRange.indices
            .map { index -> startPixel + index * config.totalDayWidth }
            .map {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                if (config.isSingleDay) it + singleDayMargin else it
            }
    }

}
