package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint

internal class HeaderRowDrawer<T>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper
) : Drawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) {
        val width = view.width

        canvas.save()

        val headerBackground = config.headerBackgroundPaint

        // Hide everything in the top left corner
        val topLeftCornerWidth = config.timeTextWidth + config.timeColumnPadding * 2
        canvas.clipRect(0f, 0f, topLeftCornerWidth, config.headerHeight)
        canvas.drawRect(0f, 0f, topLeftCornerWidth, config.headerHeight, headerBackground)

        canvas.restore()
        canvas.save()

        // Clip to paint header row only.
        val headerRowBottomLine = if (config.showHeaderRowBottomLine) {
            config.headerRowBottomLinePaint.strokeWidth
        } else {
            0f
        }
        val topLeftCornerHeight = config.headerHeight - headerRowBottomLine * 1.5f
        canvas.clipRect(config.timeColumnWidth, 0f, width.toFloat(), topLeftCornerHeight)
        canvas.drawRect(0f, 0f, width.toFloat(), topLeftCornerHeight, headerBackground)

        canvas.restore()
        canvas.save()

        if (config.showHeaderRowBottomLine) {
            drawHeaderBottomLine(width, canvas, paint)
        }
    }

    private fun drawHeaderBottomLine(width: Int, canvas: Canvas, paint: Paint) {
        val headerRowBottomLineWidth = config.headerRowBottomLinePaint.strokeWidth
        val topMargin = config.headerHeight - headerRowBottomLineWidth

        paint.strokeWidth = headerRowBottomLineWidth
        paint.color = config.headerRowBottomLinePaint.color

        canvas.drawLine(0f, topMargin, width.toFloat(), topMargin, paint)
    }
}
