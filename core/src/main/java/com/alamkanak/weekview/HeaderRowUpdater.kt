package com.alamkanak.weekview

import android.text.StaticLayout
import android.util.SparseArray
import java.util.Calendar

internal class HeaderRowUpdater<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>,
    private val eventsCacheWrapper: EventsCacheWrapper<T>
) : Updater {

    private var previousHorizontalOrigin: Float? = null
    private val previousAllDayEventIds = mutableSetOf<Long>()

    private val eventsCache: EventsCache<T>
        get() = eventsCacheWrapper.get()

    override fun isRequired(drawingContext: DrawingContext): Boolean {
        val didScrollHorizontally = previousHorizontalOrigin != config.currentOrigin.x
        val currentTimeColumnWidth = config.timeTextWidth + config.timeColumnPadding * 2
        val didTimeColumnChange = currentTimeColumnWidth != config.timeColumnWidth
        val allDayEvents = eventsCache[drawingContext.dateRange]
            .filter { it.isAllDay }
            .map { it.id }
            .toSet()
        val didEventsChange = allDayEvents.hashCode() != previousAllDayEventIds.hashCode()
        return (didScrollHorizontally || didTimeColumnChange || didEventsChange).also {
            previousAllDayEventIds.clear()
            previousAllDayEventIds += allDayEvents
        }
    }

    override fun update(drawingContext: DrawingContext) {
        val dateLabels = updateDateLabels(drawingContext)
        updateHeaderHeight(drawingContext, dateLabels)
    }

    private fun updateDateLabels(drawingContext: DrawingContext): List<StaticLayout> {
        val textLayouts = drawingContext.dateRange.map { date ->
            date.toEpochDays() to calculateStaticLayoutForDate(date)
        }.toMap()

        cache.dateLabelLayouts.clear()
        cache.dateLabelLayouts += textLayouts

        return textLayouts.values.toList()
    }

    private fun updateHeaderHeight(
        drawingContext: DrawingContext,
        dateLabels: List<StaticLayout>
    ) {
        val maximumLayoutHeight = dateLabels.map { it.height.toFloat() }.max() ?: 0f
        config.headerTextHeight = maximumLayoutHeight
        drawingContext.refreshHeaderHeight()
    }

    private fun DrawingContext.refreshHeaderHeight() {
        val visibleEvents = eventsCache[dateRange].filter { it.isAllDay }
        config.hasEventInHeader = visibleEvents.isNotEmpty()
        config.refreshHeaderHeight()
    }

    private fun calculateStaticLayoutForDate(date: Calendar): StaticLayout {
        val dayLabel = config.dateFormatter(date)
        return dayLabel.toTextLayout(
            textPaint = if (date.isToday) config.todayHeaderTextPaint else config.headerTextPaint,
            width = config.totalDayWidth.toInt()
        )
    }

    private operator fun <E> SparseArray<E>.plusAssign(elements: Map<Int, E>) {
        elements.entries.forEach { put(it.key, it.value) }
    }
}
