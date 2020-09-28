package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.StaticLayout
import androidx.collection.ArrayMap
import java.util.Calendar
import kotlin.math.max

internal class CalendarRenderer(
    viewState: ViewState,
    eventChipsCache: EventChipsCache
) : Renderer {

    private val singleEventLabels = ArrayMap<String, StaticLayout>()
    private val eventsUpdater = SingleEventsUpdater(viewState, eventChipsCache, singleEventLabels)

    // Be careful when changing the order of the drawers, as that might cause
    // views to incorrectly draw over each other
    private val drawers = listOf(
        DayBackgroundDrawer(viewState),
        BackgroundGridDrawer(viewState),
        SingleEventsDrawer(viewState, eventChipsCache, singleEventLabels),
        NowLineDrawer(viewState)
    )

    override fun render(canvas: Canvas) {
        if (eventsUpdater.isRequired()) {
            eventsUpdater.update()
        }

        for (drawer in drawers) {
            drawer.draw(canvas)
        }
    }
}

private class SingleEventsUpdater(
    private val viewState: ViewState,
    private val chipsCache: EventChipsCache,
    private val eventLabels: ArrayMap<String, StaticLayout>
) : Updater {

    private val boundsCalculator = EventChipBoundsCalculator(viewState)
    private val textFitter = TextFitter(viewState)

    override fun isRequired() = true

    override fun update() {
        chipsCache.clearSingleEventsCache()

        for ((date, startPixel) in viewState.dateRangeWithStartPixels) {
            // If we use a horizontal margin in the day view, we need to offset the start pixel.
            val modifiedStartPixel = when {
                viewState.isSingleDay -> startPixel + viewState.singleDayHorizontalPadding.toFloat()
                else -> startPixel
            }

            val eventChips = chipsCache.normalEventChipsByDate(date).filter {
                it.event.isWithin(viewState.minHour, viewState.maxHour)
            }

            eventChips.calculateBounds(startPixel = modifiedStartPixel)
            eventChips.calculateTextLayouts()
        }
    }

    private fun List<EventChip>.calculateBounds(startPixel: Float) {
        for (eventChip in this) {
            val chipRect = boundsCalculator.calculateSingleEvent(eventChip, startPixel)
            if (chipRect.isValid) {
                eventChip.bounds.set(chipRect)
            } else {
                eventChip.bounds.setEmpty()
            }
        }
    }

    private fun List<EventChip>.calculateTextLayouts() {
        for (eventChip in this) {
            val bounds = eventChip.bounds
            val horizontalPadding = viewState.eventPaddingHorizontal * 2
            val verticalPadding = viewState.eventPaddingVertical * 2

            val availableWidth = bounds.width() - horizontalPadding
            val availableHeight = bounds.height() - verticalPadding
            if (availableHeight <= 0 || availableWidth <= 0) {
                eventChip.setEmpty()
                continue
            }

            val isNotCached = eventChip.id !in eventLabels
            val didAvailableAreaChange = eventChip.didAvailableAreaChange(
                availableWidth = availableWidth,
                availableHeight = availableHeight
            )

            if (isNotCached || didAvailableAreaChange) {
                eventLabels[eventChip.id] = textFitter.fit(eventChip = eventChip)
                eventChip.updateAvailableArea(availableWidth, availableHeight)
            }
        }
    }

    private val RectF.isValid: Boolean
        get() {
            val hasCorrectWidth = left < right && left < viewState.viewWidth
            val hasCorrectHeight = top < viewState.viewHeight
            val isNotHiddenByChrome = right > viewState.timeColumnWidth && bottom > viewState.headerHeight
            return hasCorrectWidth && hasCorrectHeight && isNotHiddenByChrome
        }
}

private class DayBackgroundDrawer(
    private val viewState: ViewState
) : Drawer {

    override fun draw(canvas: Canvas) {
        canvas.drawInBounds(viewState.calendarGridBounds) {
            viewState.dateRangeWithStartPixels.forEach { (date, startPixel) ->
                drawDayBackground(date, startPixel, canvas)
            }
        }
    }

    /**
     * Draws a day's background color in the corresponding bounds.
     *
     * @param day The [Calendar] indicating the date
     * @param startPixel The x-coordinate on which to start drawing the background
     * @param canvas The [Canvas] on which to draw the background
     */
    private fun drawDayBackground(
        day: Calendar,
        startPixel: Float,
        canvas: Canvas
    ) {
        val endPixel = startPixel + viewState.dayWidth
        val isCompletelyHiddenByTimeColumn = endPixel <= viewState.timeColumnWidth
        if (isCompletelyHiddenByTimeColumn) {
            return
        }

        val actualStartPixel = max(startPixel, viewState.timeColumnWidth)
        val height = viewState.viewHeight.toFloat()

        // If not specified, this will use the normal day background.
        val pastPaint = viewState.getPastBackgroundPaint(isWeekend = day.isWeekend)
        val futurePaint = viewState.getFutureBackgroundPaint(isWeekend = day.isWeekend)

        val startY = viewState.headerHeight + viewState.currentOrigin.y
        val endX = startPixel + viewState.dayWidth

        when {
            day.isToday -> drawPastAndFutureRect(actualStartPixel, startY, endX, pastPaint, futurePaint, height, canvas)
            day.isBeforeToday -> canvas.drawRect(actualStartPixel, startY, endX, height, pastPaint)
            else -> canvas.drawRect(actualStartPixel, startY, endX, height, futurePaint)
        }
    }

    private fun drawPastAndFutureRect(
        startX: Float,
        startY: Float,
        endX: Float,
        pastPaint: Paint,
        futurePaint: Paint,
        height: Float,
        canvas: Canvas
    ) {
        val now = now()
        val hour = now.hour - viewState.minHour
        val hourFraction = now.minute / 60f

        val beforeNow = (hour + hourFraction) * viewState.hourHeight
        canvas.drawRect(startX, startY, endX, startY + beforeNow, pastPaint)
        canvas.drawRect(startX, startY + beforeNow, endX, height, futurePaint)
    }
}

private class BackgroundGridDrawer(
    private val viewState: ViewState
) : Drawer {

    override fun draw(canvas: Canvas) {
        canvas.drawInBounds(viewState.calendarGridBounds) {
            if (viewState.showHourSeparators) {
                drawHourLines()
            }

            if (viewState.showDaySeparators) {
                drawDaySeparators()
            }
        }
    }

    private fun Canvas.drawDaySeparators() {
        for (startPixel in viewState.startPixels) {
            if (viewState.showTimeColumnSeparator && startPixel == viewState.timeColumnWidth) {
                continue
            }

            drawVerticalLine(
                horizontalOffset = startPixel,
                startY = viewState.headerHeight,
                endY = viewState.headerHeight + viewState.viewHeight,
                paint = viewState.daySeparatorPaint
            )
        }
    }

    private fun Canvas.drawHourLines() {
        for (hour in viewState.displayedHours) {
            drawHourLine(hour)
        }
    }

    private fun Canvas.drawHourLine(hour: Int) {
        val heightOfHour = (viewState.hourHeight * (hour - viewState.minHour))
        val verticalOffset = viewState.headerHeight + viewState.currentOrigin.y + heightOfHour

        drawHorizontalLine(
            verticalOffset = verticalOffset,
            startX = viewState.timeColumnWidth,
            endX = viewState.viewWidth.toFloat(),
            paint = viewState.hourSeparatorPaint
        )
    }
}

private class SingleEventsDrawer(
    private val viewState: ViewState,
    private val chipsCache: EventChipsCache,
    private val eventLabels: ArrayMap<String, StaticLayout>
) : Drawer {

    private val eventChipDrawer = EventChipDrawer(viewState)

    override fun draw(canvas: Canvas) {
        canvas.drawInBounds(viewState.calendarGridBounds) {
            for (date in viewState.dateRange) {
                drawEventsForDate(date)
            }
        }
    }

    private fun Canvas.drawEventsForDate(date: Calendar) {
        val eventChips = chipsCache.normalEventChipsByDate(date).filterNot { it.bounds.isEmpty }
        for (eventChip in eventChips) {
            val textLayout = eventLabels[eventChip.id] ?: continue
            eventChipDrawer.draw(eventChip, canvas = this, textLayout)
        }
    }
}

private class NowLineDrawer(
    private val viewState: ViewState
) : Drawer {

    override fun draw(canvas: Canvas) {
        if (viewState.showNowLine.not()) {
            return
        }

        val startPixel = viewState
            .dateRangeWithStartPixels
            .filter { (date, _) -> date.isToday }
            .map { (_, startPixel) -> startPixel }
            .firstOrNull() ?: return

        canvas.drawLine(startPixel)
    }

    private fun Canvas.drawLine(startPixel: Float) {
        val top = viewState.headerHeight + viewState.currentOrigin.y
        val now = now()

        val portionOfDay = (now.hour - viewState.minHour) + now.minute / 60f
        val portionOfDayInPixels = portionOfDay * viewState.hourHeight
        val verticalOffset = top + portionOfDayInPixels

        val startX = max(startPixel, viewState.timeColumnWidth)
        val endX = startPixel + viewState.dayWidth
        drawLine(startX, verticalOffset, endX, verticalOffset, viewState.nowLinePaint)

        if (viewState.showNowLineDot) {
            drawDot(startPixel, verticalOffset)
        }
    }

    private fun Canvas.drawDot(startPixel: Float, lineStartY: Float) {
        val dotRadius = viewState.nowDotPaint.strokeWidth
        val actualStartPixel = max(startPixel, viewState.timeColumnWidth)

        val fullLineWidth = viewState.dayWidth
        val actualEndPixel = startPixel + fullLineWidth

        val currentlyDisplayedWidth = actualEndPixel - actualStartPixel
        val currentlyDisplayedPortion = currentlyDisplayedWidth / fullLineWidth

        val adjustedRadius = currentlyDisplayedPortion * dotRadius
        drawCircle(actualStartPixel, lineStartY, adjustedRadius, viewState.nowDotPaint)
    }
}
