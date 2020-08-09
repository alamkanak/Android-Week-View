package com.alamkanak.weekview

import android.text.StaticLayout
import android.util.SparseArray
import androidx.collection.ArrayMap

internal class WeekViewCache {
    val allDayEventLayouts = ArrayMap<EventChip, StaticLayout>()
    val dateLabelLayouts = SparseArray<StaticLayout>()
    val timeLabelLayouts = SparseArray<StaticLayout>()
}
