package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import java.util.Calendar
import kotlin.math.max
import kotlin.math.roundToInt

internal class CalendarRenderer(
    viewState: ViewState,
    eventChipsCache: EventChipsCache
) : Renderer {

    private val eventsUpdater = SingleEventsUpdater(viewState, eventChipsCache)

    // Be careful when changing the order of the drawers, as that might cause
    // views to incorrectly draw over each other
    private val drawers = listOf(
        DayBackgroundDrawer(viewState),
        BackgroundGridDrawer(viewState),
        SingleEventsDrawer(viewState, eventChipsCache),
        NowLineDrawer(viewState),
        HeaderRowDrawer(viewState)
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
    private val chipsCache: EventChipsCache
) : Updater {

    private val boundsCalculator = EventChipBoundsCalculator(viewState)

    override fun isRequired() = true

    override fun update() {
        chipsCache.clearSingleEventsCache()

        viewState
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                // If we use a horizontal margin in the day view, we need to offset the start pixel.
                val modifiedStartPixel = when {
                    viewState.isSingleDay -> startPixel + viewState.eventMarginHorizontal.toFloat()
                    else -> startPixel
                }
                calculateRectsForEventsOnDate(date, modifiedStartPixel)
            }
    }

    private fun calculateRectsForEventsOnDate(
        date: Calendar,
        startPixel: Float
    ) {
        chipsCache.normalEventChipsByDate(date)
            .filter { it.event.isNotAllDay && it.event.isWithin(viewState.minHour, viewState.maxHour) }
            .forEach {
                val chipRect = boundsCalculator.calculateSingleEvent(it, startPixel)
                if (chipRect.isValidSingleEventRect) {
                    it.bounds = chipRect
                } else {
                    it.bounds = null
                }
            }
    }

    private val RectF.isValidSingleEventRect: Boolean
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
        viewState.dateRangeWithStartPixels.forEach { (date, startPixel) ->
            drawDayBackground(date, startPixel, canvas)
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
        val endPixel = startPixel + viewState.widthPerDay
        val isCompletelyHiddenByTimeColumn = endPixel <= viewState.timeColumnWidth
        if (isCompletelyHiddenByTimeColumn) {
            return
        }

        val actualStartPixel = max(startPixel, viewState.timeColumnWidth)
        val height = viewState.viewHeight.toFloat()

        if (viewState.showDistinctPastFutureColor) {
            val useWeekendColor = day.isWeekend && viewState.showDistinctWeekendColor
            val pastPaint = viewState.getPastBackgroundPaint(useWeekendColor)
            val futurePaint = viewState.getFutureBackgroundPaint(useWeekendColor)

            val startY = viewState.headerHeight + viewState.currentOrigin.y
            val endX = startPixel + viewState.widthPerDay

            when {
                day.isToday -> drawPastAndFutureRect(actualStartPixel, startY, endX, pastPaint, futurePaint, height, canvas)
                day.isBeforeToday -> canvas.drawRect(actualStartPixel, startY, endX, height, pastPaint)
                else -> canvas.drawRect(actualStartPixel, startY, endX, height, futurePaint)
            }
        } else {
            val todayPaint = viewState.getDayBackgroundPaint(day.isToday)
            val right = startPixel + viewState.widthPerDay
            canvas.drawRect(actualStartPixel, viewState.headerHeight, right, height, todayPaint)
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

    private lateinit var hourLines: FloatArray

    override fun draw(canvas: Canvas) {
        viewState.startPixels.forEach { startPixel ->
            val startX = max(startPixel, viewState.timeColumnWidth)
            drawGrid(startX, startPixel, canvas)
        }
    }

    private fun createHourLines(): FloatArray {
        val headerHeight = viewState.getTotalHeaderHeight()
        val gridHeight = viewState.viewHeight - headerHeight.toInt()
        val linesPerDay = (gridHeight / viewState.hourHeight) + 1
        val overallLines = linesPerDay.roundToInt() * (viewState.numberOfVisibleDays + 1)
        return FloatArray(overallLines * 4) // 4 lines make a cube in the grid
    }

    private fun drawGrid(startX: Float, startPixel: Float, canvas: Canvas) {
        if (viewState.showHourSeparators) {
            hourLines = createHourLines()
            drawHourLines(startX, startPixel, canvas)
        }

        if (viewState.showDaySeparators) {
            drawDaySeparators(startPixel, canvas)
        }
    }

    private fun drawDaySeparators(startPixel: Float, canvas: Canvas) {
        val days = viewState.numberOfVisibleDays
        val widthPerDay = viewState.totalDayWidth
        val top = viewState.headerHeight

        for (i in 0 until days) {
            val start = startPixel + widthPerDay * (i + 1)
            canvas.drawLine(start, top, start, top + viewState.viewHeight, viewState.daySeparatorPaint)
        }
    }

    private fun drawHourLines(startX: Float, startPixel: Float, canvas: Canvas) {
        val hourStep = viewState.timeColumnHoursInterval
        var lineIndex = 0

        for (hour in hourStep until viewState.hoursPerDay step hourStep) {
            val heightOfHour = (viewState.hourHeight * hour)
            val top = viewState.headerHeight + viewState.currentOrigin.y + heightOfHour

            val widthPerDay = viewState.totalDayWidth
            val separatorWidth = viewState.hourSeparatorPaint.strokeWidth

            val isNotHiddenByHeader = top > viewState.headerHeight - separatorWidth
            val isWithinVisibleRange = top < viewState.viewHeight
            val isVisibleHorizontally = startPixel + widthPerDay - startX > 0

            if (isNotHiddenByHeader && isWithinVisibleRange && isVisibleHorizontally) {
                hourLines[lineIndex * 4] = startX
                hourLines[lineIndex * 4 + 1] = top
                hourLines[lineIndex * 4 + 2] = startPixel + widthPerDay
                hourLines[lineIndex * 4 + 3] = top
                lineIndex++
            }
        }

        canvas.drawLines(hourLines, viewState.hourSeparatorPaint)
    }
}

private class SingleEventsDrawer(
    private val viewState: ViewState,
    private val chipsCache: EventChipsCache
) : Drawer {

    private val eventChipDrawer = EventChipDrawer(viewState)

    override fun draw(canvas: Canvas) {
        for (date in viewState.dateRange) {
            drawEventsForDate(date, canvas)
        }
    }

    private fun drawEventsForDate(
        date: Calendar,
        canvas: Canvas
    ) {
        chipsCache
            .normalEventChipsByDate(date)
            .filter { it.bounds != null }
            .forEach { eventChipDrawer.draw(it, canvas) }
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
        val endX = startPixel + viewState.totalDayWidth
        drawLine(startX, verticalOffset, endX, verticalOffset, viewState.nowLinePaint)

        if (viewState.showNowLineDot) {
            drawDot(startPixel, verticalOffset)
        }
    }

    private fun Canvas.drawDot(startPixel: Float, lineStartY: Float) {
        val dotRadius = viewState.nowDotPaint.strokeWidth
        val actualStartPixel = max(startPixel, viewState.timeColumnWidth)

        val fullLineWidth = viewState.totalDayWidth
        val actualEndPixel = startPixel + fullLineWidth

        val currentlyDisplayedWidth = actualEndPixel - actualStartPixel
        val currentlyDisplayedPortion = currentlyDisplayedWidth / fullLineWidth

        val adjustedRadius = currentlyDisplayedPortion * dotRadius
        drawCircle(actualStartPixel, lineStartY, adjustedRadius, viewState.nowDotPaint)
    }
}

private class HeaderRowDrawer(
    private val viewState: ViewState
) : Drawer {

    override fun draw(canvas: Canvas) {
        val width = viewState.viewWidth.toFloat()

        val backgroundPaint = if (viewState.showHeaderRowBottomShadow) {
            viewState.headerBackgroundPaint.withShadow(
                radius = viewState.headerRowBottomShadowRadius,
                color = viewState.headerRowBottomShadowColor
            )
        } else viewState.headerBackgroundPaint

        canvas.drawRect(0f, 0f, width, viewState.headerHeight, backgroundPaint)

        if (viewState.showWeekNumber) {
            canvas.drawWeekNumber(viewState)
        }

        if (viewState.showHeaderRowBottomLine) {
            val y = viewState.headerHeight - viewState.headerRowBottomLineWidth
            canvas.drawLine(0f, y, width, y, viewState.headerRowBottomLinePaint)
        }
    }

    private fun Canvas.drawWeekNumber(state: ViewState) {
        val weekNumber = state.dateRange.first().weekOfYear.toString()

        val bounds = state.weekNumberBounds
        val textPaint = state.weekNumberTextPaint

        val textHeight = textPaint.textHeight
        val textOffset = (textHeight / 2f).roundToInt() - textPaint.descent().roundToInt()

        val width = textPaint.getTextBounds("52").width() * 2.5f
        val height = textHeight * 1.5f

        val backgroundRect = RectF(
            bounds.centerX() - width / 2f,
            bounds.centerY() - height / 2f,
            bounds.centerX() + width / 2f,
            bounds.centerY() + height / 2f
        )

        drawRect(bounds, state.headerBackgroundPaint)

        val backgroundPaint = state.weekNumberBackgroundPaint
        val radius = state.weekNumberBackgroundCornerRadius.toFloat()
        drawRoundRect(backgroundRect, radius, radius, backgroundPaint)

        drawText(weekNumber, bounds.centerX(), bounds.centerY() + textOffset, textPaint)
    }
}

private val Paint.textHeight: Int
    get() = (descent() - ascent()).roundToInt()

private fun Paint.getTextBounds(text: String): Rect {
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)
    return rect
}

private fun Paint.withShadow(radius: Int, color: Int): Paint {
    return Paint(this).apply {
        setShadowLayer(radius.toFloat(), 0f, 0f, color)
    }
}
