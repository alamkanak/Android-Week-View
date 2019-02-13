package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import kotlin.math.max

internal class DayBackgroundDrawer(
        private val config: WeekViewConfig
) {

    private val drawConfig: WeekViewDrawingConfig = config.drawingConfig

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        var startPixel = drawingContext.startPixel

        for (day in drawingContext.dayRange) {
            val startX = max(startPixel, drawConfig.timeColumnWidth)
            drawDayBackground(day, startX, startPixel, canvas)

            if (config.isSingleDay) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel += config.eventMarginHorizontal.toFloat()
            }

            // In the next iteration, start from the next day.
            startPixel += config.totalDayWidth
        }
    }

    private fun drawDayBackground(day: Calendar, startX: Float, startPixel: Float, canvas: Canvas) {
        if (drawConfig.widthPerDay + startPixel - startX <= 0) {
            return
        }

        val height = WeekView.getViewHeight()
        val isToday = day.isToday

        if (config.showDistinctPastFutureColor) {
            val useWeekendColor = day.isWeekend && config.showDistinctWeekendColor

            val pastPaint = drawConfig.getPastBackgroundPaint(useWeekendColor)
            val futurePaint = drawConfig.getFutureBackgroundPaint(useWeekendColor)

            val startY = drawConfig.headerHeight + drawConfig.currentOrigin.y
            val endX = startPixel + drawConfig.widthPerDay

            if (isToday) {
                val now = Calendar.getInstance()
                val beforeNow = (now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f) * config.hourHeight
                canvas.drawRect(startX, startY, endX, startY + beforeNow, pastPaint)
                canvas.drawRect(startX, startY + beforeNow, endX, height.toFloat(), futurePaint)
            } else if (day.isBeforeToday) {
                canvas.drawRect(startX, startY, endX, height.toFloat(), pastPaint)
            } else {
                canvas.drawRect(startX, startY, endX, height.toFloat(), futurePaint)
            }
        } else {
            val todayPaint = drawConfig.getTodayBackgroundPaint(isToday)
            val right = startPixel + drawConfig.widthPerDay
            canvas.drawRect(startX, drawConfig.headerHeight, right, height.toFloat(), todayPaint)
        }
    }

}
