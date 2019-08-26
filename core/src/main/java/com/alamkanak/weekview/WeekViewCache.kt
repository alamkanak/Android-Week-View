package com.alamkanak.weekview

import android.text.StaticLayout
import android.util.SparseArray

internal class WeekViewCache<T> {

    val allDayEventLayouts = mutableListOf<Pair<EventChip<T>, StaticLayout>>()
    val dayLabelCache = SparseArray<String>()
    val multiLineDayLabelCache = SparseArray<StaticLayout>()

    fun clearAllDayEventLayouts() {
        allDayEventLayouts.clear()
    }
}
