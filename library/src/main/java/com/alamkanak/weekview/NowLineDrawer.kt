package com.alamkanak.weekview

import android.graphics.Canvas
import kotlin.math.max

internal class NowLineDrawer(
    private val config: WeekViewConfigWrapper
) {

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        if (config.showNowLineDot.not()) {
            return
        }

        val startPixel = drawingContext
            .dateRangeWithStartPixels
            .filter { it.first.isToday }
            .map { it.second }
            .firstOrNull() ?: return

        val startX = max(startPixel, config.timeColumnWidth)
        drawLine(startX, startPixel, canvas)
    }

    private fun drawLine(startX: Float, startPixel: Float, canvas: Canvas) {
        val startY = config.headerHeight + config.currentOrigin.y
        val now = now()

        val portionOfDay = (now.hour - config.minHour) + now.minute / 60.0f
        val beforeNow = portionOfDay * config.hourHeight
        val lineStartY = startY + beforeNow
        val lineStopX = startPixel + config.widthPerDay
        canvas.drawLine(startX, lineStartY, lineStopX, lineStartY, config.nowLinePaint)

        if (config.showNowLineDot) {
            drawDot(startPixel, lineStartY, canvas)
        }
    }

    private fun drawDot(startPixel: Float, lineStartY: Float, canvas: Canvas) {
        // We use a margin to prevent the dot from sticking on the left side of the screen
        val dotRadius = config.nowDotPaint.strokeWidth
        val dotMargin = 32f
        canvas.drawCircle(startPixel + dotMargin, lineStartY, dotRadius, config.nowDotPaint)
    }

}
