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
        val bottom = viewState.viewHeight.toFloat()
        val bounds = viewState.timeColumnBounds

        // Draw background
        canvas.drawRect(bounds, timeColumnBackgroundPaint)

        val hourLines = FloatArray(hoursPerDay * 4)

        for (hour in displayedHours) {
            val heightOfHour = hourHeight * (hour - minHour)
            val topMargin = headerHeight + currentOrigin.y + heightOfHour

            val isOutsideVisibleArea = topMargin > bottom
            if (isOutsideVisibleArea) {
                continue
            }

            var y = topMargin - timeColumnTextHeight / 2

            // If the hour separator is shown in the time column, move the time label below it
            if (showTimeColumnHourSeparators) {
                y += timeColumnTextHeight / 2 + hourSeparatorPaint.strokeWidth + timeColumnPadding
            }

            val label = timeLabelLayouts[hour]
            val x = if (viewState.isLtr) {
                bounds.right - viewState.timeColumnPadding
            } else {
                bounds.left + viewState.timeColumnPadding
            }

            canvas.withTranslation(x, y) {
                label.draw(this)
            }

            if (showTimeColumnHourSeparators && hour > 0) {
                val j = hour - 1
                hourLines[j * 4] = x
                hourLines[j * 4 + 1] = topMargin
                hourLines[j * 4 + 2] = x + timeColumnWidth
                hourLines[j * 4 + 3] = topMargin
            }
        }

        // Draw the vertical time column separator
        if (showTimeColumnSeparator) {
            val lineX = if (isLtr) {
                timeColumnWidth - timeColumnSeparatorPaint.strokeWidth / 2
            } else {
                viewWidth - timeColumnWidth
            }
            canvas.drawLine(lineX, headerHeight, lineX, bottom, timeColumnSeparatorPaint)
        }

        // Draw the hour separator inside the time column
        if (showTimeColumnHourSeparators) {
            canvas.drawLines(hourLines, hourSeparatorPaint)
        }
    }

    private fun updateTimeLabels() = with(viewState) {
        timeLabelLayouts.clear()

        val textLayouts = mutableListOf<StaticLayout>()

        for (hour in displayedHours) {
            val textLayout = timeFormatter(hour).toTextLayout(timeColumnTextPaint, width = Int.MAX_VALUE)
            textLayouts += textLayout
            timeLabelLayouts.put(hour, textLayout)
        }

        val maxLineLength = textLayouts.maxOf { it.maxLineLength }
        val maxLineHeight = textLayouts.maxOf { it.height }

        updateTimeColumnBounds(
            lineLength = maxLineLength,
            lineHeight = maxLineHeight.toFloat()
        )
    }
}
