package com.alamkanak.weekview

import android.graphics.Canvas
import kotlin.math.max
import kotlin.math.roundToInt

internal class BackgroundGridDrawer(
        private val config: WeekViewConfigWrapper
) {
    
    private lateinit var hourLines: FloatArray

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        val startPixels = drawingContext.getStartPixels(config)

        for (startPixel in startPixels) {
            val startX = max(startPixel, config.timeColumnWidth)
            hourLines = createHourLines()
            drawGrid(startX, startPixel, canvas)
        }
    }

    private fun createHourLines(): FloatArray {
        val height = WeekView.getViewHeight()

        val headerHeight = config.getTotalHeaderHeight()
        val gridHeight = height - headerHeight.toInt()

        val linesPerDay = (gridHeight / config.hourHeight) + 1
        val overallLines = linesPerDay.roundToInt() * (config.numberOfVisibleDays + 1)

        return FloatArray(overallLines * 4) // 4 lines make a cube in the grid
    }

    private fun drawGrid(startX: Float, startPixel: Float, canvas: Canvas) {
        if (config.showHourSeparators) {
            drawHourLines(startX, startPixel, canvas)
        }

        if (config.showDaySeparators) {
            drawDaySeparators(startPixel, canvas)
        }
    }

    private fun drawDaySeparators(startPixel: Float, canvas: Canvas) {
        val days = config.numberOfVisibleDays
        val widthPerDay = config.totalDayWidth

        val top = config.headerHeight
        val height = WeekView.getViewHeight()

        for (i in 0 until days) {
            val start = startPixel + widthPerDay * (i + 1)
            canvas.drawLine(start, top, start, top + height, config.daySeparatorPaint)
        }
    }

    private fun drawHourLines(startX: Float, startPixel: Float, canvas: Canvas) {
        val height = WeekView.getViewHeight()
        val hourStep = config.timeColumnHoursInterval

        var i = 0
        for (hour in hourStep until config.hoursPerDay step hourStep) {
            val heightOfHour = (config.hourHeight * hour)
            val top = config.headerHeight + config.currentOrigin.y + heightOfHour

            val widthPerDay = config.totalDayWidth
            val separatorWidth = config.hourSeparatorPaint.strokeWidth

            val isNotHiddenByHeader = top > config.headerHeight - separatorWidth
            val isWithinVisibleRange = top < height
            val isVisibleHorizontally = startPixel + widthPerDay - startX > 0

            if (isNotHiddenByHeader && isWithinVisibleRange && isVisibleHorizontally) {
                hourLines[i * 4] = startX
                hourLines[i * 4 + 1] = top
                hourLines[i * 4 + 2] = startPixel + widthPerDay
                hourLines[i * 4 + 3] = top
                i++
            }
        }

        canvas.drawLines(hourLines, config.hourSeparatorPaint)
    }

}
