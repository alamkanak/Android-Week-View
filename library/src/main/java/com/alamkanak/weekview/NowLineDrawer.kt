package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import kotlin.math.max

internal class NowLineDrawer(
        private val config: WeekViewConfigWrapper
) {

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        if (config.showNowLine) {
              val startPixel = drawingContext
                .getDateRangeWithStartPixels(config)
                .filter { it.first.isToday }
                .map { it.second }
                .firstOrNull() ?: return

                val startX = max(startPixel, config.timeColumnWidth)
                drawLine(startX, startPixel, canvas)
        }
    }

    private fun drawLine(startX: Float, startPixel: Float, canvas: Canvas) {
        val startY = config.headerHeight + config.currentOrigin.y
        val now = Calendar.getInstance()

        // Draw line
        val portionOfDay = (now.get(HOUR_OF_DAY) - config.minHour) + now.get(MINUTE) / 60.0f
        val beforeNow = portionOfDay * config.hourHeight
        val lineStartY = startY + beforeNow
        val lineStopX = startPixel + config.widthPerDay
        canvas.drawLine(startX, lineStartY, lineStopX, lineStartY, config.nowLinePaint)

        if (config.showNowLineDot) {
            drawDot(startPixel, lineStartY, canvas)
        }
    }

    private fun drawDot(startPixel: Float, lineStartY: Float, canvas: Canvas) {
        // Draw dot at the beginning of the line
        val dotRadius = config.nowDotPaint.strokeWidth
        val dotMargin = 32f

        // We use startPixel to prevent the dot from sticking on the left side of the screen
        canvas.drawCircle(startPixel + dotMargin, lineStartY, dotRadius, config.nowDotPaint)
    }

}
