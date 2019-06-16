package com.alamkanak.weekview

import android.graphics.Canvas
import android.util.SparseArray

internal class TimeColumnDrawer(
    private val view: WeekView<*>,
    private val config: WeekViewConfigWrapper
) {
    private val timeLabelCache = SparseArray<String>()

    init {
        cacheTimeLabels()
    }

    private fun cacheTimeLabels() {
        for (hour in config.startHour until config.hoursPerDay step config.timeColumnHoursInterval) {
            timeLabelCache.put(hour, config.dateTimeInterpreter.interpretTime(hour + config.minHour))
        }
    }

    fun drawTimeColumn(canvas: Canvas) {
        var topMargin = config.headerHeight
        val bottom = view.height.toFloat()

        // Draw the background color for the time column.
        canvas.drawRect(0f, topMargin, config.timeColumnWidth, bottom, config.timeColumnBackgroundPaint)
        canvas.restore()
        canvas.save()
        canvas.clipRect(0f, topMargin, config.timeColumnWidth, bottom)

        val startHour = config.startHour
        val hourLines = FloatArray(config.hoursPerDay * 4)
        val hourStep = config.timeColumnHoursInterval

        for (hour in startHour until config.hoursPerDay step hourStep) {
            val heightOfHour = (config.hourHeight * hour)
            topMargin = config.headerHeight + config.currentOrigin.y + heightOfHour

            // Draw the text if its y position is not outside of the visible area. The pivot point
            // of the text is the point at the bottom-right corner.
            if (topMargin < bottom) {
                val x = config.timeTextWidth + config.timeColumnPadding
                var y = topMargin + config.timeTextHeight / 2

                // If the hour separator is shown in the time column, move the time label below it
                if (config.showTimeColumnHourSeparator) {
                    y += config.timeTextHeight / 2 + config.hourSeparatorPaint.strokeWidth + config.timeColumnPadding
                }

                canvas.drawText(timeLabelCache[hour], x, y, config.timeTextPaint)

                if (config.showTimeColumnHourSeparator && hour > 0) {
                    val j = hour - 1
                    hourLines[j * 4] = 0f
                    hourLines[j * 4 + 1] = topMargin
                    hourLines[j * 4 + 2] = config.timeColumnWidth
                    hourLines[j * 4 + 3] = topMargin
                }
            }
        }

        // Draw the vertical time column separator
        if (config.showTimeColumnSeparator) {
            val lineX = config.timeColumnWidth - config.timeColumnSeparatorStrokeWidth
            canvas.drawLine(lineX, config.headerHeight, lineX, bottom, config.timeColumnSeparatorPaint)
        }

        // Draw the hour separator inside the time column
        if (config.showTimeColumnHourSeparator) {
            canvas.drawLines(hourLines, config.hourSeparatorPaint)
        }

        canvas.restore()
    }

    fun clearLabelCache() {
        timeLabelCache.clear()
        cacheTimeLabels()
    }

}
