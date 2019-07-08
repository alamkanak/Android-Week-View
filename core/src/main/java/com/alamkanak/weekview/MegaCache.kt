package com.alamkanak.weekview

import android.text.StaticLayout

internal class MegaCache<T>(
    val eventCache: EventCache<T>
) {

    val allDayEventLayouts = mutableListOf<Pair<EventChip<T>, StaticLayout>>()

}
