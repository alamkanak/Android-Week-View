package com.alamkanak.weekview

import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.SparseArray
import java.util.Calendar

internal class MultiLineDayLabelUpdater<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) : Updater {

    override fun isRequired(): Boolean = config.singleLineHeader.not()

    override fun update(drawingContext: DrawingContext) {
        val multiDayLabels = drawingContext
            .dateRangeWithStartPixels
            .map { it.first }
            .map { it to calculateStaticLayoutForDate(it) }

        for ((date, multiDayLabel) in multiDayLabels) {
            val key = date.toEpochDays()
            cache.multiLineDayLabelCache.put(key, multiDayLabel)
        }

        val staticLayout = multiDayLabels
            .map { it.second }
            .maxBy { it.height }

        config.headerTextHeight = staticLayout?.height?.toFloat() ?: 0f
        config.refreshHeaderHeight()
    }

    private fun calculateStaticLayoutForDate(date: Calendar): StaticLayout {
        val key = date.toEpochDays()
        val dayLabel = cache.dayLabelCache.get(key) { provideAndCacheDayLabel(key, date) }

        val textPaint = if (date.isToday) {
            config.todayHeaderTextPaint
        } else {
            config.headerTextPaint
        }

        return buildStaticLayout(dayLabel, TextPaint(textPaint))
    }

    private fun buildStaticLayout(dayLabel: String, textPaint: TextPaint): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(dayLabel, 0, dayLabel.length, textPaint, config.totalDayWidth.toInt())
                .build()
        } else {
            StaticLayout(dayLabel, textPaint, config.totalDayWidth.toInt(),
                Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)
        }
    }

    private fun provideAndCacheDayLabel(key: Int, day: Calendar): String {
        return config.dateTimeInterpreter.interpretDate(day).also {
            cache.dayLabelCache.put(key, it)
        }
    }

    private fun <E> SparseArray<E>.get(key: Int, providerIfEmpty: () -> E): E {
        return get(key) ?: providerIfEmpty.invoke()
    }

}
