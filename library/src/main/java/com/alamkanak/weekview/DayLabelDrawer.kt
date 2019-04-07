package com.alamkanak.weekview

import android.graphics.Canvas
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import org.threeten.bp.LocalDate

internal class DayLabelDrawer(
        private val config: WeekViewConfigWrapper
) {

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        drawingContext
                .getDateRangeWithStartPixels(config)
                .forEach { (date, startPixel) ->
                    drawLabel(date, startPixel, canvas)
                }
    }

    private fun drawLabel(day: LocalDate, startPixel: Float, canvas: Canvas) {
        val dayLabel = config.dateTimeInterpreter.interpretDate(day.toCalendar())
        val x = startPixel + config.widthPerDay / 2

        val textPaint = if (day.isToday) {
            config.todayHeaderTextPaint
        } else {
            config.headerTextPaint
        }

        if (config.singleLineHeader) {
            val y = config.headerRowPadding.toFloat() - textPaint.ascent()
            canvas.drawText(dayLabel, x, y, textPaint)
        } else {
            val multiLineTextPaint = TextPaint(textPaint)
            val staticLayout = buildStaticLayout(dayLabel, multiLineTextPaint)

            config.headerTextHeight = staticLayout.height.toFloat()
            config.refreshHeaderHeight()

            canvas.save()
            canvas.translate(x, config.headerRowPadding.toFloat())
            staticLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun buildStaticLayout(dayLabel: String, textPaint: TextPaint): StaticLayout =
            if (SDK_INT >= M) {
                StaticLayout.Builder
                        .obtain(dayLabel, 0, dayLabel.length, textPaint, config.totalDayWidth.toInt())
                        .build()
            } else {
                StaticLayout(dayLabel, textPaint, config.totalDayWidth.toInt(),
                        Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false)
            }
}
