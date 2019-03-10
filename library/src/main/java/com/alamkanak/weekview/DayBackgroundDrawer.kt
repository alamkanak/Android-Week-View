package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import kotlin.math.max

internal class DayBackgroundDrawer(
        private val config: WeekViewConfig
) {

    private val drawConfig: WeekViewDrawingConfig = config.drawingConfig

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        drawingContext
                .getDateRangeWithStartPixels(config)
                .forEach { (date, startPixel) ->
                    val startX = max(startPixel, drawConfig.timeColumnWidth)
                    drawDayBackground(date, startX, startPixel, canvas)
                }
    }

    private fun drawDayBackground(day: Calendar, startX: Float, startPixel: Float, canvas: Canvas) {
        if (drawConfig.widthPerDay + startPixel - startX <= 0) {
            return
        }

        val height = WeekView.getViewHeight().toFloat()

        if (config.showDistinctPastFutureColor) {
            val useWeekendColor = day.isWeekend && config.showDistinctWeekendColor
            val pastPaint = drawConfig.getPastBackgroundPaint(useWeekendColor)
            val futurePaint = drawConfig.getFutureBackgroundPaint(useWeekendColor)

            val startY = drawConfig.headerHeight + drawConfig.currentOrigin.y
            val endX = startPixel + drawConfig.widthPerDay

            when {
                day.isToday -> drawPastAndFutureRect(startX, startY, endX, pastPaint, futurePaint, height, canvas)
                day.isBeforeToday -> canvas.drawRect(startX, startY, endX, height, pastPaint)
                else -> canvas.drawRect(startX, startY, endX, height, futurePaint)
            }
        } else {
            val todayPaint = drawConfig.getTodayBackgroundPaint(day.isToday)
            val right = startPixel + drawConfig.widthPerDay
            canvas.drawRect(startX, drawConfig.headerHeight, right, height, todayPaint)
        }
    }

    private fun drawPastAndFutureRect(startX: Float, startY: Float, endX: Float, pastPaint: Paint,
                                      futurePaint: Paint, height: Float, canvas: Canvas) {
        val now = Calendar.getInstance()
        val beforeNow = (now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f) * config.hourHeight
        canvas.drawRect(startX, startY, endX, startY + beforeNow, pastPaint)
        canvas.drawRect(startX, startY + beforeNow, endX, height, futurePaint)
    }

}
