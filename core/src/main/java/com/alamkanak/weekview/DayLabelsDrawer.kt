package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.Calendar

internal class DayLabelsDrawer<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) : Drawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas
    ) {
        val left = config.timeColumnWidth
        val top = 0f
        val right = canvas.width.toFloat()
        val bottom = config.getTotalHeaderHeight()

        canvas.drawInRect(left, top, right, bottom) {
            drawingContext.dateRangeWithStartPixels.forEach { (date, startPixel) ->
                drawLabel(date, startPixel, this)
            }
        }
    }

    private fun drawLabel(day: Calendar, startPixel: Float, canvas: Canvas) {
        val key = day.toEpochDays()
        val textLayout = cache.dateLabelLayouts[key]

        canvas.withTranslation(
            x = startPixel + config.widthPerDay / 2,
            y = config.headerRowPadding.toFloat()
        ) {
            textLayout.draw(this)
        }
    }
}
