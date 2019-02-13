package com.alamkanak.weekview

import java.lang.Math.ceil
import java.util.*

class DrawingContext(
        val dateRange: List<Calendar>,
        val startPixel: Float
) {

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
