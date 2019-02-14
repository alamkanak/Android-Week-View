package com.alamkanak.weekview

import java.lang.Math.ceil
import java.util.*
import java.util.Calendar.DATE

class DrawingContext(
        val dateRange: List<Calendar>,
        val startPixel: Float
) {

    /**
     * Returns the actually visible date range. This can be different from [dateRange] if the user
     * is currently scrolling.
     *
     * @param firstVisibleDate The first visible date
     * @param config The [WeekViewConfig]
     *
     * @return The list of currently visible dates
     */
    fun getVisibleDateRange(firstVisibleDate: Calendar, config: WeekViewConfig): List<Calendar> {
        val result = dateRange as MutableList
        val isScrolling = config.drawingConfig.currentOrigin.x % config.totalDayWidth != 0f
        if (isScrolling) {
            // If the user is scrolling, a new view becomes partially visible
            val lastVisibleDay = firstVisibleDate.clone() as Calendar
            lastVisibleDay.add(DATE, config.numberOfVisibleDays)
            dateRange.add(lastVisibleDay)
        }
        return result
    }

    fun getDateRangeWithStartPixels(config: WeekViewConfig): List<Pair<Calendar, Float>> {
        return dateRange.zip(getStartPixels(config))
    }

    fun getStartPixels(config: WeekViewConfig): List<Float> {
        val results = mutableListOf<Float>()
        results.add(startPixel)

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
        fun create(config: WeekViewConfig): DrawingContext {
            val drawConfig = config.drawingConfig
            val totalDayWidth = config.totalDayWidth
            val leftDaysWithGaps = (ceil((drawConfig.currentOrigin.x / totalDayWidth).toDouble()) * -1).toInt()
            val startPixel = (drawConfig.currentOrigin.x
                    + totalDayWidth * leftDaysWithGaps
                    + drawConfig.timeColumnWidth)

            val start = leftDaysWithGaps + 1
            val end = start + config.numberOfVisibleDays + 1
            val dayRange = DateUtils.getDateRange(start, end)

            return DrawingContext(dayRange, startPixel)
        }
    }

}
