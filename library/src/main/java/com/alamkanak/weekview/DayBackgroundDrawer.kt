package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import java.util.*
import kotlin.math.max

internal class DayBackgroundDrawer(
    private val config: WeekViewConfigWrapper
) {

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        drawingContext
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                val startX = max(startPixel, config.timeColumnWidth)
                drawDayBackground(date, startX, startPixel, canvas)
            }
    }

    private fun drawDayBackground(
        day: Calendar,
        startX: Float,
        startPixel: Float,
        canvas: Canvas
    ) {
        if (config.widthPerDay + startPixel - startX <= 0) {
            return
        }

        val height = WeekView.height.toFloat()

        if (config.showDistinctPastFutureColor) {
            val useWeekendColor = day.isWeekend && config.showDistinctWeekendColor
            val pastPaint = config.getPastBackgroundPaint(useWeekendColor)
            val futurePaint = config.getFutureBackgroundPaint(useWeekendColor)

            val startY = config.headerHeight + config.currentOrigin.y
            val endX = startPixel + config.widthPerDay

            when {
                day.isToday -> drawPastAndFutureRect(startX, startY, endX, pastPaint, futurePaint, height, canvas)
                day.isBeforeToday -> canvas.drawRect(startX, startY, endX, height, pastPaint)
                else -> canvas.drawRect(startX, startY, endX, height, futurePaint)
            }
        } else {
            val todayPaint = config.getTodayBackgroundPaint(day.isToday)
            val right = startPixel + config.widthPerDay
            canvas.drawRect(startX, config.headerHeight, right, height, todayPaint)
        }
    }

    private fun drawPastAndFutureRect(startX: Float, startY: Float, endX: Float, pastPaint: Paint,
                                      futurePaint: Paint, height: Float, canvas: Canvas) {
        val now = now()
        val beforeNow = (now.hour + now.minute / 60.0f) * config.hourHeight
        canvas.drawRect(startX, startY, endX, startY + beforeNow, pastPaint)
        canvas.drawRect(startX, startY + beforeNow, endX, height, futurePaint)
    }

}
