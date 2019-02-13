package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.*

internal class DayLabelDrawer(
        private val config: WeekViewConfig
) {

    private val drawingConfig: WeekViewDrawingConfig = config.drawingConfig

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        var startPixel = drawingContext.startPixel

        for (day in drawingContext.dayRange) {
            drawLabel(day, startPixel, canvas)

            if (config.isSingleDay) {
                startPixel += config.eventMarginHorizontal.toFloat()
            }

            startPixel += config.totalDayWidth
        }
    }

    private fun drawLabel(day: Calendar, startPixel: Float, canvas: Canvas) {
        val dayLabel = drawingConfig.dateTimeInterpreter.interpretDate(day)

        val x = startPixel + drawingConfig.widthPerDay / 2

        val textPaint = if (day.isToday) {
            drawingConfig.todayHeaderTextPaint
        } else {
            drawingConfig.headerTextPaint
        }

        val y = config.headerRowPadding - textPaint.ascent()

        canvas.drawText(dayLabel, x, y, textPaint)
    }

}
