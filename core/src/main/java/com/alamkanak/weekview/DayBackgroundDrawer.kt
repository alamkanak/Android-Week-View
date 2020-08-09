package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import com.alamkanak.weekview.Constants.MINUTES_PER_HOUR
import java.util.Calendar
import kotlin.math.max

internal class DayBackgroundDrawer(
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
        val hourFraction = now.minute / MINUTES_PER_HOUR

        val beforeNow = (hour + hourFraction) * viewState.hourHeight
        canvas.drawRect(startX, startY, endX, startY + beforeNow, pastPaint)
        canvas.drawRect(startX, startY + beforeNow, endX, height, futurePaint)
    }
}
