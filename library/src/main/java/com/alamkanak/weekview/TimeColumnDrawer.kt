package com.alamkanak.weekview

import android.graphics.Canvas

private class TimeColumnDrawer(
        private val config: WeekViewConfig
) {

    private val drawingConfig: WeekViewDrawingConfig = config.drawingConfig

    fun drawTimeColumn(canvas: Canvas) {
        var top = drawingConfig.headerHeight
        val bottom = WeekView.getViewHeight()

        // Draw the background color for the time column.
        canvas.drawRect(0f, top, drawingConfig.timeColumnWidth,
                bottom.toFloat(), drawingConfig.timeColumnBackgroundPaint)

        canvas.restore()
        canvas.save()

        canvas.clipRect(0f, top, drawingConfig.timeColumnWidth, bottom.toFloat())

        val startHour = config.startHour
        val hourLines = FloatArray(config.hoursPerDay * 4)
        val hourStep = config.timeColumnHoursInterval

        for (i in startHour until config.hoursPerDay step hourStep) {
            val heightOfHour = (config.hourHeight * i)
            top = drawingConfig.headerHeight + drawingConfig.currentOrigin.y + heightOfHour

            // Draw the text if its y position is not outside of the visible area. The pivot point
            // of the text is the point at the bottom-right corner.
            val time = drawingConfig.dateTimeInterpreter.interpretTime(i + config.minHour)

            if (top < bottom) {
                val x = drawingConfig.timeTextWidth + config.timeColumnPadding
                var y = top + drawingConfig.timeTextHeight / 2

                // If the hour separator is shown in the time column, move the time label below it
                if (config.showTimeColumnHourSeparator) {
                    y += drawingConfig.timeTextHeight / 2 + config.hourSeparatorStrokeWidth + config.timeColumnPadding
                }

                canvas.drawText(time, x, y, drawingConfig.timeTextPaint)

                if (config.showTimeColumnHourSeparator && i > 0) {
                    val j = i-1
                    val  yHoursLines = top
                    hourLines[j * 4] = 0f
                    hourLines[j * 4 + 1] = yHoursLines
                    hourLines[j * 4 + 2] = drawingConfig.timeColumnWidth
                    hourLines[j * 4 + 3] = yHoursLines
                }
            }
        }

        // Draw the vertical time column separator
        if (config.showTimeColumnSeparator) {
            val lineX = drawingConfig.timeColumnWidth - config.timeColumnSeparatorStrokeWidth
            canvas.drawLine(lineX, drawingConfig.headerHeight, lineX, bottom.toFloat(), drawingConfig.timeColumnSeparatorPaint)
        }

        // Draw the hour separator inside the time column
        if (config.showTimeColumnHourSeparator) {
            canvas.drawLines(hourLines, drawingConfig.hourSeparatorPaint)
        }

        canvas.restore()
    }

}
