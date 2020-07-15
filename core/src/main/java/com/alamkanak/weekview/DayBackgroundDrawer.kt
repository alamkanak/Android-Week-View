package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import com.alamkanak.weekview.Constants.MINUTES_PER_HOUR
import java.util.Calendar
import kotlin.math.max

internal class DayBackgroundDrawer(
    private val view: WeekView<*>,
    private val config: WeekViewConfigWrapper
) : Drawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas
    ) {
        drawingContext.dateRangeWithStartPixels.forEach { (date, startPixel) ->
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
        val endPixel = startPixel + config.widthPerDay
        val isCompletelyHiddenByTimeColumn = endPixel <= config.timeColumnWidth
        if (isCompletelyHiddenByTimeColumn) {
            return
        }

        val actualStartPixel = max(startPixel, config.timeColumnWidth)
        val height = view.height.toFloat()

        if (config.showDistinctPastFutureColor) {
            val useWeekendColor = day.isWeekend && config.showDistinctWeekendColor
            val pastPaint = config.getPastBackgroundPaint(useWeekendColor)
            val futurePaint = config.getFutureBackgroundPaint(useWeekendColor)

            val startY = config.headerHeight + config.currentOrigin.y
            val endX = startPixel + config.widthPerDay

            when {
                day.isToday -> drawPastAndFutureRect(actualStartPixel, startY, endX, pastPaint, futurePaint, height, canvas)
                day.isBeforeToday -> canvas.drawRect(actualStartPixel, startY, endX, height, pastPaint)
                else -> canvas.drawRect(actualStartPixel, startY, endX, height, futurePaint)
            }
        } else {
            val todayPaint = config.getDayBackgroundPaint(day.isToday)
            val right = startPixel + config.widthPerDay
            canvas.drawRect(actualStartPixel, config.headerHeight, right, height, todayPaint)
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
        val hour = now.hour - config.minHour
        val hourFraction = now.minute / MINUTES_PER_HOUR

        val beforeNow = (hour + hourFraction) * config.hourHeight
        canvas.drawRect(startX, startY, endX, startY + beforeNow, pastPaint)
        canvas.drawRect(startX, startY + beforeNow, endX, height, futurePaint)
    }
}
