package com.alamkanak.weekview

import android.graphics.Canvas
import com.alamkanak.weekview.Constants.HOURS_PER_DAY

private class TimeColumnDrawer(
        private val config: WeekViewConfig
) {

    private val drawingConfig: WeekViewDrawingConfig = config.drawingConfig

    fun drawTimeColumn(canvas: Canvas) {
        var top = (drawingConfig.headerHeight
                + (config.headerRowPadding * 2).toFloat()
                + config.headerRowBottomLineWidth.toFloat())
        val bottom = WeekView.getViewHeight()

        // Draw the background color for the time column.
        canvas.drawRect(0f, top, drawingConfig.timeColumnWidth,
                bottom.toFloat(), drawingConfig.timeColumnBackgroundPaint)

        canvas.restore()
        canvas.save()

        canvas.clipRect(0f, top, drawingConfig.timeColumnWidth, bottom.toFloat())

        // The original header height
        val headerHeight = top

        for (i in 1..HOURS_PER_DAY) {
            val headerBottomMargin = drawingConfig.headerMarginBottom
            val heightOfHour = (config.hourHeight * i).toFloat()
            top = headerHeight + drawingConfig.currentOrigin.y + heightOfHour + headerBottomMargin

            // Draw the text if its y position is not outside of the visible area. The pivot point
            // of the text is the point at the bottom-right corner.
            val time = drawingConfig.dateTimeInterpreter.interpretTime(i)

            if (top < bottom) {
                val x = drawingConfig.timeTextWidth + config.timeColumnPadding
                val y = top + drawingConfig.timeTextHeight / 2
                canvas.drawText(time, x, y, drawingConfig.timeTextPaint)
            }
        }

        // Draw the vertical time column separator
        if (config.showTimeColumnSeparator) {
            val lineX = drawingConfig.timeColumnWidth - config.timeColumnSeparatorStrokeWidth
            canvas.drawLine(lineX, headerHeight, lineX, bottom.toFloat(), drawingConfig.timeColumnSeparatorPaint)
        }

        canvas.restore()
    }



}
