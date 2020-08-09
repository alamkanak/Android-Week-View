package com.alamkanak.weekview

import android.text.StaticLayout
import android.util.SparseArray
import java.util.Calendar

internal class HeaderRowUpdater<T>(
    private val viewState: ViewState,
    private val cache: WeekViewCache<T>,
    private val eventsCacheWrapper: EventsCacheWrapper<T>
) : Updater {

    private var previousHorizontalOrigin: Float? = null
    private val previousAllDayEventIds = mutableSetOf<Long>()

    private val eventsCache: EventsCache<T>
        get() = eventsCacheWrapper.get()

    override fun isRequired(): Boolean {
        val didScrollHorizontally = previousHorizontalOrigin != viewState.currentOrigin.x
        val currentTimeColumnWidth = viewState.timeTextWidth + viewState.timeColumnPadding * 2
        val didTimeColumnChange = currentTimeColumnWidth != viewState.timeColumnWidth
        val allDayEvents = eventsCache[viewState.dateRange]
            .filter { it.isAllDay }
            .map { it.id }
            .toSet()
        val didEventsChange = allDayEvents.hashCode() != previousAllDayEventIds.hashCode()
        return (didScrollHorizontally || didTimeColumnChange || didEventsChange).also {
            previousAllDayEventIds.clear()
            previousAllDayEventIds += allDayEvents
        }
    }

    override fun update() {
        val dateLabels = updateDateLabels(viewState)
        updateHeaderHeight(viewState, dateLabels)
    }

    private fun updateDateLabels(state: ViewState): List<StaticLayout> {
        val textLayouts = state.dateRange.map { date ->
            date.toEpochDays() to calculateStaticLayoutForDate(date)
        }.toMap()

        cache.dateLabelLayouts.clear()
        cache.dateLabelLayouts += textLayouts

        return textLayouts.values.toList()
    }

    private fun updateHeaderHeight(
        state: ViewState,
        dateLabels: List<StaticLayout>
    ) {
        val maximumLayoutHeight = dateLabels.map { it.height.toFloat() }.max() ?: 0f
        state.headerTextHeight = maximumLayoutHeight
        refreshHeaderHeight()
    }

    private fun refreshHeaderHeight() {
        val visibleEvents = eventsCache[viewState.dateRange].filter { it.isAllDay }
        viewState.hasEventInHeader = visibleEvents.isNotEmpty()
        viewState.refreshHeaderHeight()
    }

    private fun calculateStaticLayoutForDate(date: Calendar): StaticLayout {
        val dayLabel = viewState.dateFormatter(date)
        return dayLabel.toTextLayout(
            textPaint = if (date.isToday) viewState.todayHeaderTextPaint else viewState.headerTextPaint,
            width = viewState.totalDayWidth.toInt()
        )
    }

    private operator fun <E> SparseArray<E>.plusAssign(elements: Map<Int, E>) {
        elements.entries.forEach { put(it.key, it.value) }
    }
}
