package com.alamkanak.weekview

import android.graphics.Canvas

internal class HeaderRowDrawer<T>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper
) : Drawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas
    ) {
        val width = view.width.toFloat()
        canvas.drawRect(0f, 0f, width, config.headerHeight, config.headerBackgroundPaint)

        if (config.showHeaderRowBottomLine) {
            val top = config.headerHeight - config.headerRowBottomLineWidth
            canvas.drawLine(0f, top, width, top, config.headerRowBottomLinePaint)
        }
    }
}
