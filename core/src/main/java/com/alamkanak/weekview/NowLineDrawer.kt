package com.alamkanak.weekview

import android.graphics.Canvas
import com.alamkanak.weekview.Constants.MINUTES_PER_HOUR
import kotlin.math.max

internal class NowLineDrawer(
    private val config: WeekViewConfigWrapper
) : Drawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas
    ) {
        if (config.showNowLine.not()) {
            return
        }

        val startPixel = drawingContext
            .dateRangeWithStartPixels
            .filter { (date, _) -> date.isToday }
            .map { (_, startPixel) -> startPixel }
            .firstOrNull() ?: return

        canvas.drawLine(startPixel)
    }

    private fun Canvas.drawLine(startPixel: Float) {
        val top = config.headerHeight + config.currentOrigin.y
        val now = now()

        val portionOfDay = (now.hour - config.minHour) + now.minute / MINUTES_PER_HOUR
        val portionOfDayInPixels = portionOfDay * config.hourHeight
        val verticalOffset = top + portionOfDayInPixels

        val startX = max(startPixel, config.timeColumnWidth)
        val endX = startPixel + config.totalDayWidth
        drawLine(startX, verticalOffset, endX, verticalOffset, config.nowLinePaint)

        if (config.showNowLineDot) {
            drawDot(startPixel, verticalOffset)
        }
    }

    private fun Canvas.drawDot(startPixel: Float, lineStartY: Float) {
        val dotRadius = config.nowDotPaint.strokeWidth
        val actualStartPixel = max(startPixel, config.timeColumnWidth)

        val fullLineWidth = config.totalDayWidth
        val actualEndPixel = startPixel + fullLineWidth

        val currentlyDisplayedWidth = actualEndPixel - actualStartPixel
        val currentlyDisplayedPortion = currentlyDisplayedWidth / fullLineWidth

        val adjustedRadius = currentlyDisplayedPortion * dotRadius
        drawCircle(actualStartPixel, lineStartY, adjustedRadius, config.nowDotPaint)
    }
}
