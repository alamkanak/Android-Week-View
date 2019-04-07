package com.alamkanak.weekview

internal class WeekViewEventSplitter<T>(
        private val config: WeekViewConfigWrapper
) {

    fun split(event: WeekViewEvent<T>): List<WeekViewEvent<T>> {
        val newEndTime = event.endTime.copy()
        val isAtStartOfNewPeriod = config.minHour == 0 && newEndTime.isAtStartOfNextDay(event.startTime)

        return if (isAtStartOfNewPeriod) {
            listOf(shortenTooLongAllDayEvent(event))
        } else if (!event.isSameDay(newEndTime)) {
            splitEventByDays(event)
        } else {
            listOf(event)
        }
    }

    private fun shortenTooLongAllDayEvent(
            event: WeekViewEvent<T>
    ): WeekViewEvent<T> {
        val newEndTime = event.endTime.withTimeAtEndOfPeriod(config.maxHour)
        return event.copy(endTime = newEndTime)
    }

    private fun splitEventByDays(event: WeekViewEvent<T>): List<WeekViewEvent<T>> {
        val results = mutableListOf<WeekViewEvent<T>>()

        val firstEventEnd = event.startTime.copy().withTimeAtEndOfPeriod(config.maxHour)
        val firstEvent = event.copy(endTime = firstEventEnd)
        results += firstEvent

        val lastEventStart = event.endTime.copy().withTimeAtStartOfPeriod(config.minHour)
        val lastEvent = event.copy(startTime = lastEventStart)
        results += lastEvent

        val diff = lastEvent.startTime.timeInMillis - firstEvent.startTime.timeInMillis
        val daysInBetween = diff / Constants.DAY_IN_MILLIS

        if (daysInBetween > 0) {
            val start = firstEventEnd.copy().withTimeAtStartOfPeriod(config.minHour).plusDays(1)

            while (start.isSameDate(lastEventStart).not()) {
                val intermediateStart = start.copy().withTimeAtStartOfPeriod(config.minHour)
                val intermediateEnd = start.copy().withTimeAtEndOfPeriod(config.maxHour)
                results += event.copy(startTime = intermediateStart, endTime = intermediateEnd)
                start.addDays(1)
            }
        }

        return results.sorted()
    }

}
