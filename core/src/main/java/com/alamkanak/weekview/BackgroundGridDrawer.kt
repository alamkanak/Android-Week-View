package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.roundToInt

internal class BackgroundGridDrawer(
    private val view: WeekView<*>,
    private val config: WeekViewConfigWrapper
) : Drawer {

    private lateinit var hourLines: FloatArray

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) {
        drawingContext.dateRangeWithStartPixels.forEach { (_, startPixel) ->
            val startX = max(startPixel, config.timeColumnWidth)
            drawGrid(startX, startPixel, canvas)
        }
    }

    private fun createHourLines(): FloatArray {
        val headerHeight = config.getTotalHeaderHeight()
        val gridHeight = view.height - headerHeight.toInt()
        val linesPerDay = (gridHeight / config.hourHeight) + 1
        val overallLines = linesPerDay.roundToInt() * (config.numberOfVisibleDays + 1)
        return FloatArray(overallLines * 4) // 4 lines make a cube in the grid
    }

    private fun drawGrid(startX: Float, startPixel: Float, canvas: Canvas) {
        if (config.showHourSeparators) {
            hourLines = createHourLines()
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

        for (i in 0 until days) {
            val start = startPixel + widthPerDay * (i + 1)
            canvas.drawLine(start, top, start, top + view.height, config.daySeparatorPaint)
        }
    }

    private fun drawHourLines(startX: Float, startPixel: Float, canvas: Canvas) {
        val hourStep = config.timeColumnHoursInterval
        var lineIndex = 0

        for (hour in hourStep until config.hoursPerDay step hourStep) {
            val heightOfHour = (config.hourHeight * hour)
            val top = config.headerHeight + config.currentOrigin.y + heightOfHour

            val widthPerDay = config.totalDayWidth
            val separatorWidth = config.hourSeparatorPaint.strokeWidth

            val isNotHiddenByHeader = top > config.headerHeight - separatorWidth
            val isWithinVisibleRange = top < view.height
            val isVisibleHorizontally = startPixel + widthPerDay - startX > 0

            if (isNotHiddenByHeader && isWithinVisibleRange && isVisibleHorizontally) {
                hourLines[lineIndex * 4] = startX
                hourLines[lineIndex * 4 + 1] = top
                hourLines[lineIndex * 4 + 2] = startPixel + widthPerDay
                hourLines[lineIndex * 4 + 3] = top
                lineIndex++
            }
        }

        canvas.drawLines(hourLines, config.hourSeparatorPaint)
    }
}
