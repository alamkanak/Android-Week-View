package com.alamkanak.weekview

import android.graphics.Canvas
import android.text.StaticLayout
import android.util.SparseArray

internal class TimeColumnRenderer(
    private val viewState: ViewState
) : Renderer, TimeFormatterDependent {

    private val timeLabelLayouts = SparseArray<StaticLayout>()

    init {
        updateTimeLabels()
    }

    override fun onSizeChanged(width: Int, height: Int) {
        updateTimeLabels()
    }

    override fun onTimeFormatterChanged(formatter: TimeFormatter) {
        updateTimeLabels()
    }

    override fun render(canvas: Canvas) = with(viewState) {
        var topMargin = headerHeight
        val bottom = viewState.viewHeight.toFloat()

        canvas.drawRect(0f, topMargin, timeColumnWidth, bottom, timeColumnBackgroundPaint)

        val hourLines = FloatArray(hoursPerDay * 4)

        for (hour in displayedHours) {
            val heightOfHour = hourHeight * (hour - minHour)
            topMargin = headerHeight + currentOrigin.y + heightOfHour

            val isOutsideVisibleArea = topMargin > bottom
            if (isOutsideVisibleArea) {
                continue
            }

            var y = topMargin - timeTextHeight / 2

            // If the hour separator is shown in the time column, move the time label below it
            if (showTimeColumnHourSeparator) {
                y += timeTextHeight / 2 + hourSeparatorPaint.strokeWidth + timeColumnPadding
            }

            val label = timeLabelLayouts[hour]
            val x = timeColumnWidth - timeColumnPadding

            canvas.withTranslation(x, y) {
                label.draw(this)
            }

            if (showTimeColumnHourSeparator && hour > 0) {
                val j = hour - 1
                hourLines[j * 4] = 0f
                hourLines[j * 4 + 1] = topMargin
                hourLines[j * 4 + 2] = timeColumnWidth
                hourLines[j * 4 + 3] = topMargin
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
    }

    private fun updateTimeLabels() = with(viewState) {
        var maxLineLength = 0f
        var maxLineHeight = 0

        timeLabelLayouts.clear()
        for (hour in displayedHours) {
            val textLayout = timeFormatter(hour).toTextLayout(timeTextPaint, width = Int.MAX_VALUE)
            maxLineLength = textLayout.maxLineLength
            maxLineHeight = textLayout.height
            timeLabelLayouts.put(hour, textLayout)
        }

        updateTimeColumnBounds(
            lineLength = maxLineLength,
            lineHeight = maxLineHeight.toFloat()
        )
    }
}
