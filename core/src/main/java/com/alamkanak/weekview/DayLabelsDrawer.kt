package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.Calendar

internal class DayLabelsDrawer(
    private val viewState: ViewState,
    private val cache: WeekViewCache
) : Drawer {

    override fun draw(canvas: Canvas) {
        val left = viewState.timeColumnWidth
        val top = 0f
        val right = canvas.width.toFloat()
        val bottom = viewState.getTotalHeaderHeight()

        canvas.drawInRect(left, top, right, bottom) {
            viewState.dateRangeWithStartPixels.forEach { (date, startPixel) ->
                drawLabel(date, startPixel, this)
            }
        }
    }

    private fun drawLabel(day: Calendar, startPixel: Float, canvas: Canvas) {
        val key = day.toEpochDays()
        val textLayout = cache.dateLabelLayouts[key]

        canvas.withTranslation(
            x = startPixel + viewState.widthPerDay / 2,
            y = viewState.headerRowPadding.toFloat()
        ) {
            textLayout.draw(this)
        }
    }
}
