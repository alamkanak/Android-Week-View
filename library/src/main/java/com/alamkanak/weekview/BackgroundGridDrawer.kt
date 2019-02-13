package com.alamkanak.weekview

import android.graphics.Canvas
import com.alamkanak.weekview.Constants.HOURS_PER_DAY
import kotlin.math.max

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
        val overallLines = (linesPerDay * (config.numberOfVisibleDays + 1)).toInt()

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

        val top = drawConfig.getTotalHeaderHeight(config)
        val height = WeekView.getViewHeight()

        for (day in 1..days) {
            val start = startPixel + widthPerDay * (day)
            canvas.drawLine(start, top, start, top + height, drawConfig.daySeparatorPaint)
        }
    }

    private fun drawHourLines(startX: Float, startPixel: Float, canvas: Canvas) {
        val height = WeekView.getViewHeight()
        val headerHeight = drawConfig.getTotalHeaderHeight(config)

        val hourStep = config.timeColumnHoursInterval

        for (hour in hourStep until HOURS_PER_DAY step hourStep) {
            val heightOfHour = config.hourHeight * hour
            val top = headerHeight + drawConfig.currentOrigin.y + heightOfHour

            val widthPerDay = config.totalDayWidth
            val separatorWidth = config.hourSeparatorStrokeWidth

            val isNotHiddenByHeader = top > headerHeight - separatorWidth
            val isWithinVisibleRange = top < height
            val isVisibleHorizontally = startPixel + widthPerDay - startX > 0

            if (isNotHiddenByHeader && isWithinVisibleRange && isVisibleHorizontally) {
                hourLines[hour * 4] = startX
                hourLines[hour * 4 + 1] = top
                hourLines[hour * 4 + 2] = startPixel + widthPerDay
                hourLines[hour * 4 + 3] = top
            }
        }

        canvas.drawLines(hourLines, drawConfig.hourSeparatorPaint)
    }

}
