package com.alamkanak.weekview

import android.graphics.Canvas
import com.alamkanak.weekview.Constants.HOURS_PER_DAY
import kotlin.math.max

internal class BackgroundGridDrawer(
        private val config: WeekViewConfig
) {

    private val drawConfig: WeekViewDrawingConfig = config.drawingConfig

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        val size = drawingContext.dayRange.size

        var startPixel = drawingContext.startPixel
        var hourLines: FloatArray

        for (i in 0 until size) {
            val startX = max(startPixel, drawConfig.timeColumnWidth)
            hourLines = createHourLines()
            drawGrid(hourLines, startX, startPixel, canvas)

            if (config.isSingleDay) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel += config.eventMarginHorizontal.toFloat()
            }

            // In the next iteration, start from the next day.
            startPixel += config.totalDayWidth
        }
    }

    private fun createHourLines(): FloatArray {
        val drawConfig = config.drawingConfig
        val height = WeekView.getViewHeight()
        val headerHeight = (drawConfig.headerHeight
                + (config.headerRowPadding * 2).toFloat()
                + drawConfig.headerMarginBottom)
        var lineCount = ((height - headerHeight) / config.hourHeight).toInt() + 1
        lineCount *= (config.numberOfVisibleDays + 1)
        return FloatArray(lineCount * 4)
    }

    private fun drawGrid(hourLines: FloatArray, startX: Float, startPixel: Float, canvas: Canvas) {
        if (config.showHourSeparator) {
            drawHourLines(hourLines, startX, startPixel, canvas)
        }

        if (config.showDaySeparator) {
            drawDaySeparators(startPixel, canvas)
        }
    }

    private fun drawDaySeparators(startPixel: Float, canvas: Canvas) {
        val days = config.numberOfVisibleDays
        val widthPerDay = config.totalDayWidth

        val top = (drawConfig.headerHeight
                + (config.headerRowPadding * 2).toFloat()
                + drawConfig.headerMarginBottom)
        val height = WeekView.getViewHeight()

        for (i in 0 until days) {
            val start = startPixel + widthPerDay * (i + 1)
            canvas.drawLine(start, top, start, top + height, drawConfig.daySeparatorPaint)
        }
    }

    private fun drawHourLines(hourLines: FloatArray,
                              startX: Float, startPixel: Float, canvas: Canvas) {
        val height = WeekView.getViewHeight()
        val headerHeight = drawConfig.headerHeight + config.headerRowPadding * 2

        var i = 0
        for (hour in 1 until HOURS_PER_DAY) {
            val heightOfHour = (config.hourHeight * hour).toFloat()
            val top = headerHeight + drawConfig.currentOrigin.y + heightOfHour

            val widthPerDay = config.totalDayWidth
            val separatorWidth = config.hourSeparatorStrokeWidth.toFloat()

            val isNotHiddenByHeader = top > headerHeight - separatorWidth
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
