package com.alamkanak.weekview

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.Calendar

class WeekViewEventTest {

    private data class Event(
        val startTime: Calendar,
        val endTime: Calendar
    ) : WeekViewDisplayable<Event> {

        override fun toWeekViewEvent(): WeekViewEvent<Event> {
            return WeekViewEvent.Builder(this)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .build()
        }
    }

    private val config = Mockito.mock(WeekViewConfigWrapper::class.java)
    private val eventSplitter = WeekViewEventSplitter<Event>(config)

    init {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(config.minHour).thenReturn(0)
        Mockito.`when`(config.maxHour).thenReturn(24)
    }

    @Test
    fun `single-day event is recognized correctly`() {
        val startTime = today().plusDays(1).withHour(6).withMinutes(0)
        val endTime = startTime.plusHours(10)
        val event = Event(startTime, endTime)

        val originalEvent = event.toWeekViewEvent()
        val childEvents = eventSplitter.split(originalEvent)
        assertTrue(childEvents.size == 1)

        val child = childEvents.first()
        assertFalse(child.endsOnLaterDay(originalEvent))
        assertFalse(child.startsOnEarlierDay(originalEvent))
    }

    @Test
    fun `two-day event is recognized correctly`() {
        val startTime = today().plusDays(1).withHour(14).withMinutes(0)
        val endTime = today().plusDays(2).withHour(14).withMinutes(0)
        val event = Event(startTime, endTime)

        val originalEvent = event.toWeekViewEvent()
        val childEvents = eventSplitter.split(originalEvent)
        assertTrue(childEvents.size == 2)

        val first = childEvents.first()
        val last = childEvents.last()

        assertTrue(first.endsOnLaterDay(originalEvent))
        assertTrue(last.startsOnEarlierDay(originalEvent))

        assertFalse(first.startsOnEarlierDay(originalEvent))
        assertFalse(last.endsOnLaterDay(originalEvent))
    }

    @Test
    fun `multi-day event is recognized correctly`() {
        val startTime = today().plusDays(1).withHour(14).withMinutes(0)
        val endTime = today().plusDays(3).withHour(1).withMinutes(0)
        val event = Event(startTime, endTime)

        val originalEvent = event.toWeekViewEvent()
        val childEvents = eventSplitter.split(originalEvent)
        assertTrue(childEvents.size == 3)

        val first = childEvents.first()
        val second = childEvents[1]
        val last = childEvents.last()

        assertTrue(first.endsOnLaterDay(originalEvent))
        assertTrue(second.startsOnEarlierDay(originalEvent))
        assertTrue(second.endsOnLaterDay(originalEvent))
        assertTrue(last.startsOnEarlierDay(originalEvent))

        assertFalse(first.startsOnEarlierDay(originalEvent))
        assertFalse(last.endsOnLaterDay(originalEvent))
    }

    @Test
    fun `non-colliding events are recognized correctly`() {
        val firstStartTime = now()
        val firstEndTime = firstStartTime.plusHours(1)
        val first = Event(firstStartTime, firstEndTime).toWeekViewEvent()

        val secondStartTime = firstStartTime.plusHours(2)
        val secondEndTime = secondStartTime.plusHours(1)
        val second = Event(secondStartTime, secondEndTime).toWeekViewEvent()

        assertFalse(first.collidesWith(second))
    }

    @Test
    fun `overlapping events are recognized as colliding`() {
        val firstStartTime = now()
        val firstEndTime = firstStartTime.plusHours(1)
        val first = Event(firstStartTime, firstEndTime).toWeekViewEvent()

        val secondStartTime = firstStartTime.minusHours(1)
        val secondEndTime = firstEndTime.plusHours(1)
        val second = Event(secondStartTime, secondEndTime).toWeekViewEvent()

        assertTrue(first.collidesWith(second))
    }

    @Test
    fun `partly-overlapping events are recognized as colliding`() {
        val firstStartTime = now().withMinutes(0)
        val firstEndTime = firstStartTime.plusHours(1)
        val first = Event(firstStartTime, firstEndTime).toWeekViewEvent()

        val secondStartTime = firstStartTime.withMinutes(30)
        val secondEndTime = secondStartTime.plusHours(1)
        val second = Event(secondStartTime, secondEndTime).toWeekViewEvent()

        assertTrue(first.collidesWith(second))
    }
}
