package com.alamkanak.weekview

import java.util.Calendar

internal fun ResolvedWeekViewEntity.split(viewState: ViewState): List<ResolvedWeekViewEntity> {
    if (startTime >= endTime) {
        return emptyList()
    }

    val entities = if (isMultiDay) {
        splitByDates(minHour = viewState.minHour, maxHour = viewState.maxHour)
    } else {
        listOf(limitTo(minHour = viewState.minHour, maxHour = viewState.maxHour))
    }

    return entities.filter { it.startTime < it.endTime }
}

private fun ResolvedWeekViewEntity.splitByDates(
    minHour: Int,
    maxHour: Int,
): List<ResolvedWeekViewEntity> {
    val firstEvent = createCopy(
        startTime = startTime.limitToMinHour(minHour),
        endTime = startTime.atEndOfDay.limitToMaxHour(maxHour)
    )

    val results = mutableListOf<ResolvedWeekViewEntity>()
    results += firstEvent

    val daysInBetween = endTime.toEpochDays() - startTime.toEpochDays() - 1

    if (daysInBetween > 0) {
        val currentDate = startTime.atStartOfDay + Days(1)
        while (currentDate.toEpochDays() < endTime.toEpochDays()) {
            val intermediateStart = currentDate.withTimeAtStartOfPeriod(minHour)
            val intermediateEnd = currentDate.withTimeAtEndOfPeriod(maxHour)
            results += createCopy(startTime = intermediateStart, endTime = intermediateEnd)
            currentDate += Days(1)
        }
    }

    val lastEvent = createCopy(
        startTime = endTime.atStartOfDay.limitToMinHour(minHour),
        endTime = endTime.limitToMaxHour(maxHour)
    )
    results += lastEvent

    return results.sortedWith(compareBy({ it.startTime }, { it.endTime }))
}

private fun ResolvedWeekViewEntity.limitTo(minHour: Int, maxHour: Int) = createCopy(
    startTime = startTime.limitToMinHour(minHour),
    endTime = endTime.limitToMaxHour(maxHour)
)

private fun Calendar.limitToMinHour(minHour: Int): Calendar {
    return if (hour < minHour) {
        withTimeAtStartOfPeriod(hour = minHour)
    } else {
        this
    }
}

private fun Calendar.limitToMaxHour(maxHour: Int): Calendar {
    return if (hour >= maxHour) {
        withTimeAtEndOfPeriod(hour = maxHour)
    } else {
        this
    }
}
