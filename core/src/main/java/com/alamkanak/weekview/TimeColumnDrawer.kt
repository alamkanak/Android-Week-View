package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.util.SparseArray

internal class TimeColumnDrawer(
    private val view: WeekView<*>,
    private val config: WeekViewConfigWrapper
) : CachingDrawer {

    private val timeLabelCache = SparseArray<String>()

    init {
        cacheTimeLabels()
    }

    private fun cacheTimeLabels() = with(config) {
        for (hour in startHour until hoursPerDay step timeColumnHoursInterval) {
            timeLabelCache.put(hour, dateTimeInterpreter.interpretTime(hour + minHour))
        }
    }

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) = with(config) {
        var topMargin = headerHeight
        val bottom = view.height.toFloat()

        // Draw the background color for the time column.
        canvas.drawRect(0f, topMargin, timeColumnWidth, bottom, timeColumnBackgroundPaint)
        canvas.restore()
        canvas.save()
        canvas.clipRect(0f, topMargin, timeColumnWidth, bottom)

        val hourLines = FloatArray(hoursPerDay * 4)
        val hourStep = timeColumnHoursInterval

        for (hour in startHour until hoursPerDay step hourStep) {
            val heightOfHour = (hourHeight * hour)
            topMargin = headerHeight + currentOrigin.y + heightOfHour

            // Draw the text if its y position is not outside of the visible area. The pivot point
            // of the text is the point at the bottom-right corner.
            if (topMargin < bottom) {
                val x = timeTextWidth + timeColumnPadding
                var y = topMargin + timeTextHeight / 2

                // If the hour separator is shown in the time column, move the time label below it
                if (showTimeColumnHourSeparator) {
                    y += timeTextHeight / 2 + hourSeparatorPaint.strokeWidth + timeColumnPadding
                }

                canvas.drawText(timeLabelCache[hour], x, y, timeTextPaint)

                if (showTimeColumnHourSeparator && hour > 0) {
                    val j = hour - 1
                    hourLines[j * 4] = 0f
                    hourLines[j * 4 + 1] = topMargin
                    hourLines[j * 4 + 2] = timeColumnWidth
                    hourLines[j * 4 + 3] = topMargin
                }
            }
        }

        // Draw the vertical time column separator
        if (showTimeColumnSeparator) {
            val lineX = timeColumnWidth - timeColumnSeparatorStrokeWidth
            canvas.drawLine(lineX, headerHeight, lineX, bottom, timeColumnSeparatorPaint)
        }

        // Draw the hour separator inside the time column
        if (showTimeColumnHourSeparator) {
            canvas.drawLines(hourLines, hourSeparatorPaint)
        }

        canvas.restore()
    }

    override fun clear() {
        timeLabelCache.clear()
        cacheTimeLabels()
    }
}
