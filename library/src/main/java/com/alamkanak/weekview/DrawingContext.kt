package com.alamkanak.weekview

import org.threeten.bp.LocalDate
import java.lang.Math.ceil

internal class DrawingContext(
        val dateRange: List<LocalDate>,
        val startPixel: Float
) {

    fun getDateRangeWithStartPixels(config: WeekViewConfigWrapper): List<Pair<LocalDate, Float>> {
        return dateRange.zip(getStartPixels(config))
    }

    fun getStartPixels(config: WeekViewConfigWrapper): List<Float> {
        val results = mutableListOf(startPixel)
        var currentStartPixel = startPixel

        for (day in dateRange) {
            if (config.isSingleDay) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                currentStartPixel += config.eventMarginHorizontal.toFloat()
            }

            // In the next iteration, start from the next day.
            currentStartPixel += config.totalDayWidth
            results.add(currentStartPixel)
        }

        return results
    }

    companion object {

        @JvmStatic
        fun create(config: WeekViewConfigWrapper): DrawingContext {
            val totalDayWidth = config.totalDayWidth
            val leftDaysWithGaps = (ceil((config.currentOrigin.x / totalDayWidth).toDouble()) * -1).toInt()
            val startPixel = (config.currentOrigin.x
                    + totalDayWidth * leftDaysWithGaps
                    + config.timeColumnWidth)

            val start = leftDaysWithGaps + 1
            val end = start + config.numberOfVisibleDays

            // If the user is scrolling, a new view becomes partially visible, so we must add an
            // additional date to the date range
            val isNotScrolling = config.currentOrigin.x % config.totalDayWidth == 0f
            val modifiedEnd = if (isNotScrolling) end - 1 else end

            val dayRange = getDateRange(start, modifiedEnd)
            return DrawingContext(dayRange, startPixel)
        }
    }

}
