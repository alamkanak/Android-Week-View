package com.alamkanak.weekview

internal class WeekViewEventSplitter<T>(
    private val config: WeekViewConfigWrapper
) {

    fun split(event: WeekViewEvent<T>): List<WeekViewEvent<T>> {
        val isAtStartOfNewPeriod = config.minHour == 0 &&
            event.endTime.isAtStartOfNextDay(event.startTime)

        return when {
            isAtStartOfNewPeriod -> listOf(shortenTooLongAllDayEvent(event))
            event.isMultiDay -> splitEventByDates(event)
            else -> listOf(event)
        }
    }

    private fun shortenTooLongAllDayEvent(
        event: WeekViewEvent<T>
    ): WeekViewEvent<T> {
        val newEndTime = event.endTime.withTimeAtEndOfPeriod(config.maxHour)
        return event.copy(endTime = newEndTime)
    }

    private fun splitEventByDates(event: WeekViewEvent<T>): List<WeekViewEvent<T>> {
        val results = mutableListOf<WeekViewEvent<T>>()

        val firstEventEnd = event.startTime.withTimeAtEndOfPeriod(config.maxHour)
        val firstEvent = event.copy(endTime = firstEventEnd)
        results += firstEvent

        val lastEventStart = event.endTime.withTimeAtStartOfPeriod(config.minHour)
        val lastEvent = event.copy(startTime = lastEventStart)
        results += lastEvent

        val diff = lastEvent.startTime.timeInMillis - firstEvent.startTime.timeInMillis
        val daysInBetween = diff / Constants.DAY_IN_MILLIS

        if (daysInBetween > 0) {
            var start = firstEventEnd.withTimeAtStartOfPeriod(config.minHour).plusDays(1)

            while (start.isSameDate(lastEventStart).not()) {
                val intermediateStart = start.withTimeAtStartOfPeriod(config.minHour)
                val intermediateEnd = start.withTimeAtEndOfPeriod(config.maxHour)
                results += event.copy(startTime = intermediateStart, endTime = intermediateEnd)
                start = start.plusDays(1)
            }
        }

        return results.sorted()
    }
}
