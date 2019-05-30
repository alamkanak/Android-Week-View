package com.alamkanak.weekview

import java.lang.Math.ceil
import java.util.*

internal class DrawingContext {

    var startPixel = 0f
    val dateRange = mutableListOf<Calendar>()
    val startPixels = mutableListOf<Float>()
    val dateRangeWithStartPixels = mutableListOf<Pair<Calendar, Float>>()

    fun update(config: WeekViewConfigWrapper) {
        val totalDayWidth = config.totalDayWidth
        val leftDaysWithGaps = (ceil((config.currentOrigin.x / totalDayWidth).toDouble()) * -1).toInt()

        startPixel = (config.currentOrigin.x
            + totalDayWidth * leftDaysWithGaps
            + config.timeColumnWidth)

        val start = leftDaysWithGaps + 1
        val end = start + config.numberOfVisibleDays

        // If the user is scrolling, a new view becomes partially visible, so we must add an
        // additional date to the date range
        val isNotScrolling = config.currentOrigin.x % config.totalDayWidth == 0f
        val modifiedEnd = if (isNotScrolling) end - 1 else end

        dateRange.clear()
        dateRange += getDateRange(start, modifiedEnd)

        updateStartPixels(config, startPixel)

        dateRangeWithStartPixels.clear()
        dateRangeWithStartPixels += dateRange.zip(startPixels)
    }

    private fun updateStartPixels(config: WeekViewConfigWrapper, startPixel: Float) {
        startPixels.clear()
        startPixels += startPixel

        var currentStartPixel = startPixel

        for (day in dateRange) {
            if (config.isSingleDay) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                currentStartPixel += config.eventMarginHorizontal.toFloat()
            }

            // In the next iteration, start from the next day.
            currentStartPixel += config.totalDayWidth
            startPixels.add(currentStartPixel)
        }
    }

}
