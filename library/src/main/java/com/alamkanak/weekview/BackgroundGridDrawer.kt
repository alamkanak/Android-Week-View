package com.alamkanak.weekview

import android.graphics.Canvas
import kotlin.math.max
import kotlin.math.roundToInt

internal class BackgroundGridDrawer(
        private val config: WeekViewConfig
) {

    private val drawConfig: WeekViewDrawingConfig = config.drawingConfig
    private lateinit var hourLines: FloatArray

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        val startPixels = drawingContext.getStartPixels(config)

        for (startPixel in startPixels) {
            val startX = max(startPixel, drawConfig.timeColumnWidth)
            hourLines = createHourLines()
            drawGrid(startX, startPixel, canvas)
        }
    }

    private fun createHourLines(): FloatArray {
        val drawConfig = config.drawingConfig
        val height = WeekView.getViewHeight()

        val headerHeight = drawConfig.getTotalHeaderHeight(config)
        val gridHeight = height - headerHeight.toInt()

        val linesPerDay = (gridHeight / config.hourHeight) + 1
        val overallLines = linesPerDay.roundToInt() * (config.numberOfVisibleDays + 1)

        return FloatArray(overallLines * 4) // 4 lines make a cube in the grid
    }

    private fun drawGrid(startX: Float, startPixel: Float, canvas: Canvas) {
        if (config.showHourSeparator) {
            drawHourLines(startX, startPixel, canvas)
        }

        if (config.showDaySeparator) {
            drawDaySeparators(startPixel, canvas)
        }
    }

    private fun drawDaySeparators(startPixel: Float, canvas: Canvas) {
        val days = config.numberOfVisibleDays
        val widthPerDay = config.totalDayWidth

        val top = drawConfig.headerHeight
        val height = WeekView.getViewHeight()

        for (i in 0 until days) {
            val start = startPixel + widthPerDay * (i + 1)
            canvas.drawLine(start, top, start, top + height, drawConfig.daySeparatorPaint)
        }
    }

    private fun drawHourLines(startX: Float, startPixel: Float, canvas: Canvas) {
        val height = WeekView.getViewHeight()
        val hourStep = config.timeColumnHoursInterval

        var i = 0
        for (hour in hourStep until config.hoursPerDay step hourStep) {
            val heightOfHour = (config.hourHeight * hour)
            val top = drawConfig.headerHeight + drawConfig.currentOrigin.y + heightOfHour

            val widthPerDay = config.totalDayWidth
            val separatorWidth = config.hourSeparatorStrokeWidth.toFloat()

            val isNotHiddenByHeader = top > drawConfig.headerHeight - separatorWidth
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

        canvas.drawLines(hourLines, drawConfig.hourSeparatorPaint)
    }

}
