package com.alamkanak.weekview

import android.graphics.RectF

internal class EventChipRectCalculator<T>(
    private val config: WeekViewConfigWrapper
) {

    fun calculateSingleEvent(
        eventChip: EventChip<T>,
        startPixel: Float
    ): RectF {
        val widthPerDay = config.widthPerDay

        val top = calculateVerticalDistanceFromTopOrBottom(eventChip.top)
        val bottom = calculateVerticalDistanceFromTopOrBottom(eventChip.bottom)

        var left = startPixel + eventChip.left * widthPerDay
        var right = left + eventChip.width * widthPerDay

        if (left > startPixel) {
            left += config.overlappingEventGap / 2
        }

        if (right < startPixel + widthPerDay) {
            right -= config.overlappingEventGap / 2
        }

        val hasNoOverlaps = (right == startPixel + widthPerDay)
        if (config.isSingleDay && hasNoOverlaps) {
            right -= config.eventMarginHorizontal * 2
        }

        return RectF(left, top, right, bottom)
    }

    private fun calculateVerticalDistanceFromTopOrBottom(
        value: Float
    ): Float = with(config) {
        val pixelsFromTop = hourHeight * hoursPerDay * value / minutesPerDay
        return pixelsFromTop + currentOrigin.y + headerHeight - eventMarginVertical
    }

    fun calculateAllDayEvent(
        eventChip: EventChip<T>,
        startPixel: Float
    ): RectF {
        val top = config.headerTextHeight + config.headerRowPadding * 1.5f
        val height = config.allDayEventTextPaint.textSize + config.eventPadding
        val bottom = top + height

        val widthPerDay = config.widthPerDay

        var left = startPixel + eventChip.left * widthPerDay
        var right = left + eventChip.width * widthPerDay

        if (left > startPixel) {
            left += config.overlappingEventGap / 2f
        }

        if (right < startPixel + widthPerDay) {
            right -= config.overlappingEventGap / 2f
        }

        val hasNoOverlaps = (right == startPixel + widthPerDay)
        if (config.isSingleDay && hasNoOverlaps) {
            right -= config.eventMarginHorizontal * 2
        }

        return RectF(left, top, right, bottom)
    }
}
