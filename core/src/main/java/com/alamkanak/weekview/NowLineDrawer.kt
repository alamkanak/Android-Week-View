package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import com.alamkanak.weekview.Constants.MINUTES_PER_HOUR
import kotlin.math.max

internal class NowLineDrawer(
    private val config: WeekViewConfigWrapper
) : Drawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) {
        if (config.showNowLineDot.not()) {
            return
        }

        val startPixel = drawingContext
            .dateRangeWithStartPixels
            .filter { (date, _) -> date.isToday }
            .map { (_, startPixel) -> startPixel }
            .firstOrNull() ?: return

        val startX = max(startPixel, config.timeColumnWidth)
        drawLine(startX, startPixel, canvas)
    }

    private fun drawLine(startX: Float, startPixel: Float, canvas: Canvas) {
        val top = config.headerHeight + config.currentOrigin.y
        val now = now()

        val portionOfDay = (now.hour - config.minHour) + now.minute / MINUTES_PER_HOUR
        val portionOfDayInPixels = portionOfDay * config.hourHeight
        val verticalOffset = top + portionOfDayInPixels

        val endX = startPixel + config.totalDayWidth
        canvas.drawLine(startX, verticalOffset, endX, verticalOffset, config.nowLinePaint)

        if (config.showNowLineDot) {
            drawDot(startPixel, verticalOffset, canvas)
        }
    }

    private fun drawDot(startPixel: Float, lineStartY: Float, canvas: Canvas) {
        // We use a margin to prevent the dot from sticking on the left side of the screen
        val dotRadius = config.nowDotPaint.strokeWidth
        val dotMargin = 32f
        canvas.drawCircle(startPixel + dotMargin, lineStartY, dotRadius, config.nowDotPaint)
    }

}
