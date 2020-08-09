package com.alamkanak.weekview

import android.graphics.Canvas
import android.util.SparseArray

internal class TimeColumnDrawer<T>(
    private val viewState: ViewState,
    private val cache: WeekViewCache<T>
) : Drawer {

    init {
        cacheTimeLabels()
    }

    // TODO Duplication
    private fun cacheTimeLabels() = with(viewState) {
        for (hour in displayedHours) {
            val textLayout = timeFormatter(hour).toTextLayout(timeTextPaint, width = Int.MAX_VALUE)
            cache.timeLabelLayouts.put(hour, textLayout)
        }
    }

    override fun draw(canvas: Canvas) = with(viewState) {
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

            val x = timeTextWidth + timeColumnPadding
            var y = topMargin - timeTextHeight / 2

            // If the hour separator is shown in the time column, move the time label below it
            if (showTimeColumnHourSeparator) {
                y += timeTextHeight / 2 + hourSeparatorPaint.strokeWidth + timeColumnPadding
            }

            val textLayout = cache.timeLabelLayouts[hour]
            canvas.withTranslation(x, y) {
                textLayout.draw(this)
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
}

private operator fun <E> SparseArray<E>.contains(key: Int): Boolean = indexOfKey(key) >= 0
