package com.alamkanak.weekview

import android.text.StaticLayout
import android.util.SparseArray

internal class WeekViewCache<T>(
    val eventCache: EventCache<T>
) {

    val allDayEventLayouts = mutableListOf<Pair<EventChip<T>, StaticLayout>>()

    val dayLabelCache = SparseArray<String>()
    val multiLineDayLabelCache = SparseArray<StaticLayout>()

    fun clear() {
        allDayEventLayouts.clear()
        dayLabelCache.clear()
        multiLineDayLabelCache.clear()
    }

}
