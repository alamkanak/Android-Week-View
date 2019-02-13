package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import kotlin.math.max

private class NowLineDrawer(
        private val config: WeekViewConfig
) {

    private val drawConfig: WeekViewDrawingConfig = config.drawingConfig

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        var startPixel = drawingContext.startPixel

        for (day in drawingContext.dayRange) {
            val isSameDay = day.isToday
            val startX = max(startPixel, drawConfig.timeColumnWidth)

            if (config.isSingleDay) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel += config.eventMarginHorizontal
            }

            // Draw the line at the current time.
            if (config.showNowLine && isSameDay) {
                drawLine(startX, startPixel, canvas)
            }

            // In the next iteration, start from the next day.
            startPixel += config.totalDayWidth
        }
    }

    private fun drawLine(startX: Float, startPixel: Float, canvas: Canvas) {
        val startY = drawConfig.headerHeight + drawConfig.currentOrigin.y
        val now = Calendar.getInstance()

        // Draw line
        val portionOfDay = now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f
        val beforeNow = portionOfDay * config.hourHeight
        val lineStartY = startY + beforeNow
        val lineStopX = startPixel + drawConfig.widthPerDay
        canvas.drawLine(startX, lineStartY, lineStopX, lineStartY, drawConfig.nowLinePaint)

        if (config.showNowLineDot) {
            // Draw dot at the beginning of the line
            val dotRadius = drawConfig.nowDotPaint.strokeWidth
            val dotMargin = 32f

            // We use startPixel to prevent the dot from sticking on the left side of the screen
            canvas.drawCircle(startPixel + dotMargin, lineStartY, dotRadius, drawConfig.nowDotPaint)
        }

    }

}
