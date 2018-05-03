package com.alamkanak.weekview

import com.alamkanak.weekview.WeekViewUtil.isSameDay
import java.util.*

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://april-shower.com
 */
class WeekViewEvent {
    var id: Long = 0
    val startTime: Calendar
    val endTime: Calendar
    var name: String? = null
    var location: String? = null
    var color: Int = 0
    var isAllDay: Boolean = false

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param startYear Year when the event starts.
     * @param startMonth Month when the event starts.
     * @param startDay Day when the event starts.
     * @param startHour Hour (in 24-hour format) when the event starts.
     * @param startMinute Minute when the event starts.
     * @param endYear Year when the event ends.
     * @param endMonth Month when the event ends.
     * @param endDay Day when the event ends.
     * @param endHour Hour (in 24-hour format) when the event ends.
     * @param endMinute Minute when the event ends.
     */
    constructor(id: Long, name: String, startYear: Int, startMonth: Int, startDay: Int, startHour: Int, startMinute: Int, endYear: Int, endMonth: Int, endDay: Int, endHour: Int, endMinute: Int) {
        this.id = id

        this.startTime = Calendar.getInstance()
        this.startTime.set(Calendar.YEAR, startYear)
        this.startTime.set(Calendar.MONTH, startMonth - 1)
        this.startTime.set(Calendar.DAY_OF_MONTH, startDay)
        this.startTime.set(Calendar.HOUR_OF_DAY, startHour)
        this.startTime.set(Calendar.MINUTE, startMinute)

        this.endTime = Calendar.getInstance()
        this.endTime.set(Calendar.YEAR, endYear)
        this.endTime.set(Calendar.MONTH, endMonth - 1)
        this.endTime.set(Calendar.DAY_OF_MONTH, endDay)
        this.endTime.set(Calendar.HOUR_OF_DAY, endHour)
        this.endTime.set(Calendar.MINUTE, endMinute)

        this.name = name
    }
    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     * @param allDay Is the event an all day event.
     */
    constructor(id: Long, name: String?, location: String?, startTime: Calendar, endTime: Calendar, allDay: Boolean = false) {
        this.id = id
        this.name = name
        this.location = location
        this.startTime = startTime
        this.endTime = endTime
        this.isAllDay = allDay
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    constructor(id: Long, name: String, startTime: Calendar, endTime: Calendar) : this(id, name, null, startTime, endTime) {}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is WeekViewEvent) return false
        val that = other as WeekViewEvent?
        return id == that!!.id
    }

    override fun hashCode(): Int {
        return (id xor id.ushr(32)).toInt()
    }

    fun splitWeekViewEvents(): List<WeekViewEvent> {
        //This function splits the WeekViewEvent in WeekViewEvents by day
        val events = ArrayList<WeekViewEvent>()
        // The first millisecond of the next day is still the same day. (no need to split events for this).
        var endTime = this.endTime.clone() as Calendar
        endTime.add(Calendar.MILLISECOND, -1)
        if (!isSameDay(this.startTime, endTime)) {
            endTime = this.startTime.clone() as Calendar
            endTime.set(Calendar.HOUR_OF_DAY, 23)
            endTime.set(Calendar.MINUTE, 59)
            val event1 = WeekViewEvent(this.id, this.name, this.location, this.startTime, endTime, this.isAllDay)
            event1.color = this.color
            events.add(event1)

            // Add other days.
            val otherDay = this.startTime.clone() as Calendar
            otherDay.add(Calendar.DATE, 1)
            while (!isSameDay(otherDay, this.endTime)) {
                val overDay = otherDay.clone() as Calendar
                overDay.set(Calendar.HOUR_OF_DAY, 0)
                overDay.set(Calendar.MINUTE, 0)
                val endOfOverDay = overDay.clone() as Calendar
                endOfOverDay.set(Calendar.HOUR_OF_DAY, 23)
                endOfOverDay.set(Calendar.MINUTE, 59)
                val eventMore = WeekViewEvent(this.id, this.name, null, overDay, endOfOverDay, this.isAllDay)
                eventMore.color = this.color
                events.add(eventMore)

                // Add next day.
                otherDay.add(Calendar.DATE, 1)
            }

            // Add last day.
            val startTime = this.endTime.clone() as Calendar
            startTime.set(Calendar.HOUR_OF_DAY, 0)
            startTime.set(Calendar.MINUTE, 0)
            val event2 = WeekViewEvent(this.id, this.name, this.location, startTime, this.endTime, this.isAllDay)
            event2.color = this.color
            events.add(event2)
        } else {
            events.add(this)
        }

        return events
    }
}
