package com.alamkanak.weekview

import android.text.StaticLayout
import android.util.SparseArray
import java.util.Calendar

internal class HeaderRowUpdater(
    private val viewState: ViewState,
    private val cache: WeekViewCache,
    private val eventChipsCache: EventChipsCache
) : Updater {

    private var previousHorizontalOrigin: Float? = null
    private val previousAllDayEventIds = mutableSetOf<Long>()

    override fun isRequired(): Boolean {
        val didScrollHorizontally = previousHorizontalOrigin != viewState.currentOrigin.x
        val currentTimeColumnWidth = viewState.timeTextWidth + viewState.timeColumnPadding * 2
        val didTimeColumnChange = currentTimeColumnWidth != viewState.timeColumnWidth
        val allDayEvents = eventChipsCache.allDayEventChipsInDateRange(viewState.dateRange)
            .map { it.eventId }
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
        val visibleEvents = eventChipsCache.allDayEventChipsInDateRange(viewState.dateRange)
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
