package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.RectF
import android.text.StaticLayout
import android.util.SparseArray

internal class TimeColumnRenderer(
    private val viewState: ViewState
) : Renderer, TimeFormatterDependent {

    private val timeLabelLayouts = SparseArray<StaticLayout>()
    private val bounds = RectF()

    private val textHorizontalOffset: Float
        get() = if (viewState.isLtr) {
            bounds.right - viewState.timeColumnPadding
        } else {
            bounds.left + viewState.timeColumnPadding
        }

    init {
        updateTimeLabels()
    }

    override fun onSizeChanged(width: Int, height: Int) {
        updateBounds()
        updateTimeLabels()
    }

    private fun updateBounds() {
        val startX = if (viewState.isLtr) 0f else viewState.viewWidth - viewState.timeColumnWidth
        val endX = startX + viewState.timeColumnWidth
        val startY = viewState.headerHeight
        val endY = viewState.viewHeight.toFloat()
        bounds.set(startX, startY, endX, endY)
    }

    override fun onTimeFormatterChanged(formatter: TimeFormatter) {
        updateTimeLabels()
    }

    override fun render(canvas: Canvas) = with(viewState) {
        // var topMargin = headerHeight
        val bottom = viewState.viewHeight.toFloat()

        // Draw background
        canvas.drawRect(bounds, timeColumnBackgroundPaint)
        // canvas.drawRect(0f, topMargin, timeColumnWidth, bottom, timeColumnBackgroundPaint)

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
            val x = textHorizontalOffset // timeColumnWidth - timeColumnPadding

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
                timeColumnWidth - timeColumnSeparatorPaint.strokeWidth
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
