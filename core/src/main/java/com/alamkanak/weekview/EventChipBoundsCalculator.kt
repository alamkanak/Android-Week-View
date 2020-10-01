package com.alamkanak.weekview

import android.graphics.RectF

internal class EventChipBoundsCalculator(
    private val viewState: ViewState
) {

    fun calculateSingleEvent(
        eventChip: EventChip,
        startPixel: Float
    ): RectF {
        val drawableWidth = viewState.drawableDayWidth

        val minutesFromStart = eventChip.minutesFromStartHour
        val top = calculateDistanceFromTop(minutesFromStart)

        val bottomMinutesFromStart = minutesFromStart + eventChip.event.durationInMinutes
        val bottom = calculateDistanceFromTop(bottomMinutesFromStart) - viewState.eventMarginVertical

        var left = startPixel + eventChip.relativeStart * drawableWidth
        var right = left + eventChip.relativeWidth * drawableWidth

        if (left > startPixel) {
            left += viewState.overlappingEventGap / 2
        }

        if (right < startPixel + drawableWidth) {
            right -= viewState.overlappingEventGap / 2
        }

        val hasNoOverlaps = (right == startPixel + drawableWidth)
        if (viewState.isSingleDay && hasNoOverlaps) {
            right -= viewState.singleDayHorizontalPadding * 2
        }

        return RectF(left, top, right, bottom)
    }

    private fun calculateDistanceFromTop(
        minutesFromStart: Int
    ): Float = with(viewState) {
        val portionOfDay = minutesFromStart.toFloat() / minutesPerDay
        val pixelsFromTop = hourHeight * hoursPerDay * portionOfDay
        return pixelsFromTop + currentOrigin.y + headerHeight
    }

    fun calculateAllDayEvent(
        index: Int,
        eventChip: EventChip,
        startPixel: Float
    ): RectF {
        val padding = viewState.headerPadding
        val dayWidth = viewState.drawableDayWidth

        val dateLabelHeight = padding + viewState.dateLabelHeight + padding
        val chipHeight = viewState.allDayEventTextPaint.textSize + viewState.eventPaddingVertical * 2

        val top = if (viewState.arrangeAllDayEventsVertically) {
            val previousChipsHeight = index * (eventChip.bounds.height() + viewState.eventMarginVertical)
            dateLabelHeight + previousChipsHeight
        } else {
            dateLabelHeight
        }

        var left = if (viewState.arrangeAllDayEventsVertically) {
            startPixel
        } else {
            startPixel + eventChip.relativeStart * dayWidth
        }

        var right = if (viewState.arrangeAllDayEventsVertically) {
            left + dayWidth
        } else {
            left + eventChip.relativeWidth * dayWidth
        }

        val isLeftMostColumn = left == startPixel
        val isRightMostColumn = right == startPixel + dayWidth

        if (!isLeftMostColumn) {
            left += viewState.overlappingEventGap / 2f
        }

        if (!isRightMostColumn) {
            right -= viewState.overlappingEventGap / 2f
        }

        val bottom = top + chipHeight

        if (viewState.isSingleDay && isRightMostColumn) {
            right -= viewState.singleDayHorizontalPadding * 2
        }

        return RectF(left, top, right, bottom)
    }
}
