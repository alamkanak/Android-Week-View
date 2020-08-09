package com.alamkanak.weekview

import android.graphics.Canvas
import com.alamkanak.weekview.Constants.MINUTES_PER_HOUR
import kotlin.math.max

internal class NowLineDrawer(
    private val viewState: ViewState
) : Drawer {

    override fun draw(canvas: Canvas) {
        if (viewState.showNowLine.not()) {
            return
        }

        val startPixel = viewState
            .dateRangeWithStartPixels
            .filter { (date, _) -> date.isToday }
            .map { (_, startPixel) -> startPixel }
            .firstOrNull() ?: return

        canvas.drawLine(startPixel)
    }

    private fun Canvas.drawLine(startPixel: Float) {
        val top = viewState.headerHeight + viewState.currentOrigin.y
        val now = now()

        val portionOfDay = (now.hour - viewState.minHour) + now.minute / MINUTES_PER_HOUR
        val portionOfDayInPixels = portionOfDay * viewState.hourHeight
        val verticalOffset = top + portionOfDayInPixels

        val startX = max(startPixel, viewState.timeColumnWidth)
        val endX = startPixel + viewState.totalDayWidth
        drawLine(startX, verticalOffset, endX, verticalOffset, viewState.nowLinePaint)

        if (viewState.showNowLineDot) {
            drawDot(startPixel, verticalOffset)
        }
    }

    private fun Canvas.drawDot(startPixel: Float, lineStartY: Float) {
        val dotRadius = viewState.nowDotPaint.strokeWidth
        val actualStartPixel = max(startPixel, viewState.timeColumnWidth)

        val fullLineWidth = viewState.totalDayWidth
        val actualEndPixel = startPixel + fullLineWidth

        val currentlyDisplayedWidth = actualEndPixel - actualStartPixel
        val currentlyDisplayedPortion = currentlyDisplayedWidth / fullLineWidth

        val adjustedRadius = currentlyDisplayedPortion * dotRadius
        drawCircle(actualStartPixel, lineStartY, adjustedRadius, viewState.nowDotPaint)
    }
}
