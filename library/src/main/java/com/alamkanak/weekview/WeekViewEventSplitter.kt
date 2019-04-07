package com.alamkanak.weekview

internal class WeekViewEventSplitter<T>(
        private val config: WeekViewConfigWrapper
) {

    fun split(event: WeekViewEvent<T>): List<WeekViewEvent<T>> {
        val newEndTime = event.endDateTime
        val isAtStartOfNewPeriod = config.minHour == 0
                && newEndTime.isAtStartOfNextDay(event.startDateTime)

        return if (isAtStartOfNewPeriod) {
            listOf(shortenTooLongAllDayEvent(event))
        } else if (!event.isSameDay(newEndTime.toLocalDate())) {
            splitEventByDays(event)
        } else {
            listOf(event)
        }
    }

    private fun shortenTooLongAllDayEvent(
            event: WeekViewEvent<T>
    ): WeekViewEvent<T> {
        val newEndTime = event.endDateTime.withTimeAtEndOfPeriod(config.maxHour)
        return event.copy(endTime = newEndTime)
    }

    private fun splitEventByDays(event: WeekViewEvent<T>): List<WeekViewEvent<T>> {
        val results = mutableListOf<WeekViewEvent<T>>()

        val firstEventEnd = event.startDateTime.withTimeAtEndOfPeriod(config.maxHour)
        val firstEvent = event.copy(endTime = firstEventEnd)
        results += firstEvent

        val lastEventStart = event.endDateTime.withTimeAtStartOfPeriod(config.minHour)
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
