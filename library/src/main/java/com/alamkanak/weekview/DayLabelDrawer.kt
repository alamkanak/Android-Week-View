package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.*

internal class DayLabelDrawer(
        private val config: WeekViewConfig
) {

    private val drawingConfig: WeekViewDrawingConfig = config.drawingConfig

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        drawingContext
                .getDateRangeWithStartPixels(config)
                .forEach { (date, startPixel) ->
                    drawLabel(date, startPixel, canvas)
                }
    }

    private fun drawLabel(day: Calendar, startPixel: Float, canvas: Canvas) {
        val dayLabel = drawingConfig.dateTimeInterpreter.interpretDate(day)

        val x = startPixel + drawingConfig.widthPerDay / 2
        val y = drawingConfig.headerTextHeight + config.headerRowPadding

        val textPaint = if (day.isToday) {
            drawingConfig.todayHeaderTextPaint
        } else {
            drawingConfig.headerTextPaint
        }
        canvas.drawText(dayLabel, x, y, textPaint)
    }

}
