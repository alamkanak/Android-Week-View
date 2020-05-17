package com.alamkanak.weekview

import android.graphics.RectF

internal class EventChipBoundsCalculator<T>(
    private val config: WeekViewConfigWrapper
) {

    fun calculateSingleEvent(
        eventChip: EventChip<T>,
        startPixel: Float
    ): RectF {
        val widthPerDay = config.widthPerDay
        val singleVerticalMargin = config.eventMarginVertical / 2

        val minutesFromStart = eventChip.minutesFromStartHour
        val top = calculateDistanceFromTop(minutesFromStart) + singleVerticalMargin

        val bottomMinutesFromStart = minutesFromStart + eventChip.event.durationInMinutes
        val bottom = calculateDistanceFromTop(bottomMinutesFromStart) - singleVerticalMargin

        var left = startPixel + eventChip.relativeStart * widthPerDay
        var right = left + eventChip.relativeWidth * widthPerDay

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

    private fun calculateDistanceFromTop(
        minutesFromStart: Int
    ): Float = with(config) {
        val portionOfDay = minutesFromStart.toFloat() / minutesPerDay
        val pixelsFromTop = hourHeight * hoursPerDay * portionOfDay
        return pixelsFromTop + currentOrigin.y + headerHeight
    }

    fun calculateAllDayEvent(
        eventChip: EventChip<T>,
        startPixel: Float
    ): RectF {
        val top = config.headerTextHeight + config.headerRowPadding * 1.5f
        val height = config.allDayEventTextPaint.textSize + config.eventPaddingVertical * 2
        val bottom = top + height

        val widthPerDay = config.widthPerDay

        var left = startPixel + eventChip.relativeStart * widthPerDay
        var right = left + eventChip.relativeWidth * widthPerDay

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
