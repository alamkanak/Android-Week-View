package com.alamkanak.weekview

import android.text.StaticLayout
import android.util.SparseArray
import androidx.collection.ArrayMap

internal class WeekViewCache<T> {
    val allDayEventLayouts = ArrayMap<EventChip<T>, StaticLayout>()
    val dateLabelLayouts = SparseArray<StaticLayout>()
    val timeLabelLayouts = SparseArray<StaticLayout>()

    fun clearAllDayEventLayouts() {
        allDayEventLayouts.clear()
    }
}
