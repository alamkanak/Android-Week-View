package com.alamkanak.weekview

import java.util.Calendar
import kotlin.math.ceil

internal class DrawingContext(
    private val config: WeekViewConfigWrapper
) {

    private var startPixel = 0f

    val startPixels = mutableListOf<Float>()
    val dateRange = mutableListOf<Calendar>()
    val dateRangeWithStartPixels = mutableListOf<Pair<Calendar, Float>>()

    fun update() {
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

        updateStartPixels()

        dateRangeWithStartPixels.clear()
        dateRangeWithStartPixels += dateRange.zip(startPixels)
    }

    private fun updateStartPixels() {
        startPixels.clear()
        startPixels += dateRange.indices.map {
            index -> startPixel + index * config.totalDayWidth
        }
    }
}
