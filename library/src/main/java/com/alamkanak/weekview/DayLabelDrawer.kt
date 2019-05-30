package com.alamkanak.weekview

import android.graphics.Canvas
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.SparseArray
import java.util.*

internal class DayLabelDrawer(
    private val config: WeekViewConfigWrapper
) {

    private val dayLabelCache = SparseArray<String>()

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        drawingContext
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                drawLabel(date, startPixel, canvas)
            }
    }

    private fun drawLabel(day: Calendar, startPixel: Float, canvas: Canvas) {
        val key = day.toEpochDays()
        val dayLabel = dayLabelCache.get(key) { provideAndCacheDayLabel(key, day) }

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
            val staticLayout = buildStaticLayout(dayLabel, TextPaint(textPaint))
            config.headerTextHeight = staticLayout.height.toFloat()
            config.refreshHeaderHeight()

            canvas.save()
            canvas.translate(x, config.headerRowPadding.toFloat())
            staticLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun buildStaticLayout(dayLabel: String, textPaint: TextPaint): StaticLayout {
        return if (SDK_INT >= M) {
            StaticLayout.Builder
                .obtain(dayLabel, 0, dayLabel.length, textPaint, config.totalDayWidth.toInt())
                .build()
        } else {
            StaticLayout(dayLabel, textPaint, config.totalDayWidth.toInt(),
                Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false)
        }
    }

    private fun provideAndCacheDayLabel(key: Int, day: Calendar): String {
        return config.dateTimeInterpreter.interpretDate(day).also {
            dayLabelCache.put(key, it)
        }
    }

    internal fun clearLabelCache() {
        dayLabelCache.clear()
    }

    private fun <E> SparseArray<E>.get(key: Int, providerIfEmpty: () -> E): E {
        return get(key) ?: providerIfEmpty.invoke()
    }

}
