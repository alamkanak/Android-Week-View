package com.alamkanak.weekview

import android.text.StaticLayout
import android.text.TextPaint
import android.util.SparseArray
import java.util.Calendar

internal class MultiLineDayLabelHeightUpdater<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) : Updater {

    private var previousHorizontalOrigin: Float? = null

    override fun isRequired(drawingContext: DrawingContext): Boolean {
        if (config.singleLineHeader) {
            return false
        }

        val currentTimeColumnWidth = config.timeTextWidth + config.timeColumnPadding * 2
        val didTimeColumnChange = currentTimeColumnWidth != config.timeColumnWidth
        val didScrollHorizontally = previousHorizontalOrigin != config.currentOrigin.x
        val isCacheIncomplete = config.numberOfVisibleDays != cache.allDayEventLayouts.size

        return didTimeColumnChange || didScrollHorizontally || isCacheIncomplete
    }

    override fun update(drawingContext: DrawingContext) {
        previousHorizontalOrigin = config.currentOrigin.x

        val multiDayLabels = drawingContext.dateRange.map {
            it to calculateStaticLayoutForDate(it)
        }

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
        val width = config.totalDayWidth.toInt()
        return TextLayoutBuilder.build(dayLabel, textPaint, width)
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
