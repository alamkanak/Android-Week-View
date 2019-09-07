package com.alamkanak.weekview

import androidx.collection.ArrayMap
import java.util.Calendar

internal class EventChipCache<T> {

    val allEventChips: List<EventChip<T>>
        get() = normalEventChipsByDate.values.flatten() + allDayEventChipsByDate.values.flatten()

    private val normalEventChipsByDate = ArrayMap<Long, MutableList<EventChip<T>>>()
    private val allDayEventChipsByDate = ArrayMap<Long, MutableList<EventChip<T>>>()

    fun groupedByDate(): Map<Calendar, List<EventChip<T>>> {
        return allEventChips.groupBy { it.event.startTime.atStartOfDay }
    }

    fun normalEventChipsByDate(
        date: Calendar
    ): List<EventChip<T>> = normalEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()

    fun allDayEventChipsByDate(
        date: Calendar
    ): List<EventChip<T>> = allDayEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()

    fun allDayEventChipsInDateRange(
        dateRange: List<Calendar>
    ): List<EventChip<T>> {
        val results = mutableListOf<EventChip<T>>()
        for (date in dateRange) {
            results += allDayEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()
        }
        return results
    }

    private fun put(newChips: List<EventChip<T>>) {
        for (eventChip in newChips) {
            val key = eventChip.event.startTime.atStartOfDay.timeInMillis
            if (eventChip.event.isAllDay) {
                allDayEventChipsByDate.addOrReplace(key, eventChip)
            } else {
                normalEventChipsByDate.addOrReplace(key, eventChip)
            }
        }
    }

    operator fun plusAssign(newChips: List<EventChip<T>>) = put(newChips)

    fun clearCache() {
        allEventChips.filter { it.originalEvent.isNotAllDay }.forEach(EventChip<T>::clearCache)
    }

    fun clear() {
        allDayEventChipsByDate.clear()
        normalEventChipsByDate.clear()
    }
}
