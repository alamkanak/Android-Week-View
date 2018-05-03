package com.alamkanak.weekview.sample

import com.alamkanak.weekview.WeekViewEvent
import java.util.*

/**
 * A basic example of how to use week view library.
 * Created by Raquib-ul-Alam Kanak on 1/3/2014.
 * Website: http://alamkanak.github.io
 */
class BasicActivity : BaseActivity() {

    override fun onMonthChange(newYear: Int, newMonth: Int): List<WeekViewEvent> {
        // Populate the week view with some events.
        val events = ArrayList<WeekViewEvent>()

        var startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        var endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR, 1)
        endTime.set(Calendar.MONTH, newMonth - 1)
        var event = WeekViewEvent(1, getEventTitle(startTime), startTime, endTime)
        event.color = resources.getColor(R.color.event_color_01)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 4)
        endTime.set(Calendar.MINUTE, 30)
        endTime.set(Calendar.MONTH, newMonth - 1)
        event = WeekViewEvent(10, getEventTitle(startTime), startTime, endTime)
        event.color = resources.getColor(R.color.event_color_02)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 4)
        startTime.set(Calendar.MINUTE, 20)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 5)
        endTime.set(Calendar.MINUTE, 0)
        event = WeekViewEvent(10, getEventTitle(startTime), startTime, endTime)
        event.color = resources.getColor(R.color.event_color_03)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 5)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 2)
        endTime.set(Calendar.MONTH, newMonth - 1)
        event = WeekViewEvent(2, getEventTitle(startTime), startTime, endTime)
        event.color = resources.getColor(R.color.event_color_02)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 5)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        startTime.add(Calendar.DATE, 1)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        endTime.set(Calendar.MONTH, newMonth - 1)
        event = WeekViewEvent(3, getEventTitle(startTime), startTime, endTime)
        event.color = resources.getColor(R.color.event_color_03)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 15)
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = WeekViewEvent(4, getEventTitle(startTime), startTime, endTime)
        event.color = resources.getColor(R.color.event_color_04)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 1)
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = WeekViewEvent(5, getEventTitle(startTime), startTime, endTime)
        event.color = resources.getColor(R.color.event_color_01)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH))
        startTime.set(Calendar.HOUR_OF_DAY, 15)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = WeekViewEvent(5, getEventTitle(startTime), startTime, endTime)
        event.color = resources.getColor(R.color.event_color_02)
        events.add(event)

        //AllDay event
        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 23)
        event = WeekViewEvent(7, getEventTitle(startTime), null, startTime, endTime, true)
        event.color = resources.getColor(R.color.event_color_04)
        events.add(event)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 8)
        startTime.set(Calendar.HOUR_OF_DAY, 2)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.DAY_OF_MONTH, 10)
        endTime.set(Calendar.HOUR_OF_DAY, 23)
        event = WeekViewEvent(8, getEventTitle(startTime), null, startTime, endTime, true)
        event.color = resources.getColor(R.color.event_color_03)
        events.add(event)

        // All day event until 00:00 next day
        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 10)
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        startTime.set(Calendar.MILLISECOND, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.DAY_OF_MONTH, 11)
        event = WeekViewEvent(8, getEventTitle(startTime), null, startTime, endTime, true)
        event.color = resources.getColor(R.color.event_color_01)
        events.add(event)

        return events
    }

}
