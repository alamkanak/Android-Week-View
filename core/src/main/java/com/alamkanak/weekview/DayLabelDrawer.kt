package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.util.SparseArray
import java.util.Calendar

internal class DayLabelDrawer<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) : CachingDrawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) {
        drawingContext
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                drawLabel(date, startPixel, canvas)
            }
    }

    private fun drawLabel(day: Calendar, startPixel: Float, canvas: Canvas) {
        val key = day.toEpochDays()
        val dayLabel = cache.dayLabelCache.get(key) { provideAndCacheDayLabel(key, day) }

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
            val staticLayout = cache.multiLineDayLabelCache.get(key)
            canvas.save()
            canvas.translate(x, config.headerRowPadding.toFloat())
            staticLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun provideAndCacheDayLabel(key: Int, day: Calendar): String {
        return config.dateTimeInterpreter.interpretDate(day).also {
            cache.dayLabelCache.put(key, it)
        }
    }

    override fun clear() {
        cache.dayLabelCache.clear()
        cache.multiLineDayLabelCache.clear()
    }

    private fun <E> SparseArray<E>.get(key: Int, providerIfEmpty: () -> E): E {
        return get(key) ?: providerIfEmpty.invoke()
    }

}
