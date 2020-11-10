package com.alamkanak.weekview

internal fun ResolvedWeekViewEntity.split(viewState: ViewState): List<ResolvedWeekViewEntity> {
    if (startTime >= endTime) {
        return emptyList()
    }

    // Check whether the end date of the event is exactly 12 AM. If so, the event will be
    // shortened by a millisecond.
    val endsOnStartOfNextDay = endTime.isAtStartOfNextDay(startTime)
    val isAtStartOfNextPeriod = viewState.minHour == 0 && endsOnStartOfNextDay

    return when {
        isAtStartOfNextPeriod -> listOf(shortenTooLongAllDayEvent(viewState))
        isMultiDay -> splitEventByDates(viewState)
        else -> listOf(this)
    }
}

private fun ResolvedWeekViewEntity.shortenTooLongAllDayEvent(
    viewState: ViewState
): ResolvedWeekViewEntity {
    val newEndTime = endTime.withTimeAtEndOfPeriod(viewState.maxHour)
    return createCopy(endTime = newEndTime)
}

private fun ResolvedWeekViewEntity.splitEventByDates(
    viewState: ViewState
): List<ResolvedWeekViewEntity> {
    val results = mutableListOf<ResolvedWeekViewEntity>()

    val firstEventEnd = startTime.withTimeAtEndOfPeriod(viewState.maxHour)
    val firstEvent = createCopy(endTime = firstEventEnd)
    results += firstEvent

    val lastEventStart = endTime.withTimeAtStartOfPeriod(viewState.minHour)
    val lastEvent = createCopy(startTime = lastEventStart)
    results += lastEvent

    val diff = lastEvent.startTime.timeInMillis - firstEvent.startTime.timeInMillis
    val daysInBetween = diff / DAY_IN_MILLIS

    if (daysInBetween > 0) {
        val start = firstEventEnd.withTimeAtStartOfPeriod(viewState.minHour) + Days(1)
        while (start.isSameDate(lastEventStart).not()) {
            val intermediateStart = start.withTimeAtStartOfPeriod(viewState.minHour)
            val intermediateEnd = start.withTimeAtEndOfPeriod(viewState.maxHour)
            results += createCopy(startTime = intermediateStart, endTime = intermediateEnd)
            start += Days(1)
        }
    }

    return results.sortedWith(compareBy({ it.startTime }, { it.endTime }))
}
