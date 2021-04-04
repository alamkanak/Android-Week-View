package com.alamkanak.weekview

import com.alamkanak.weekview.util.createResolvedWeekViewEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever

class WeekViewEventTest {

    private val viewState = Mockito.mock(ViewState::class.java)

    init {
        MockitoAnnotations.initMocks(this)
        whenever(viewState.minHour).thenReturn(0)
        whenever(viewState.maxHour).thenReturn(24)
    }

    @Test
    fun `single-day event is recognized correctly`() {
        val startTime = (today() + Days(1)).withHour(6).withMinutes(0)
        val endTime = startTime + Hours(10)

        val originalEvent = createResolvedWeekViewEvent(startTime, endTime)
        val childEvents = originalEvent.split(viewState)
        assertTrue(childEvents.size == 1)

        val child = childEvents.first()
        assertFalse(child.endsOnLaterDay(originalEvent))
        assertFalse(child.startsOnEarlierDay(originalEvent))
    }

    @Test
    fun `two-day event is recognized correctly`() {
        val startTime = (today() + Days(1)).withHour(14).withMinutes(0)
        val endTime = (today() + Days(2)).withHour(14).withMinutes(0)

        val originalEvent = createResolvedWeekViewEvent(startTime, endTime)
        val childEvents = originalEvent.split(viewState)
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
        val startTime = (today() + Days(1)).withHour(14).withMinutes(0)
        val endTime = (today() + Days(3)).withHour(1).withMinutes(0)

        val originalEvent = createResolvedWeekViewEvent(startTime, endTime)
        val childEvents = originalEvent.split(viewState)
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
        val firstEndTime = firstStartTime + Hours(1)
        val first = createResolvedWeekViewEvent(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime + Hours(2)
        val secondEndTime = secondStartTime + Hours(1)
        val second = createResolvedWeekViewEvent(secondStartTime, secondEndTime)

        assertFalse(first.collidesWith(second))
    }

    @Test
    fun `overlapping events are recognized as colliding`() {
        val firstStartTime = now()
        val firstEndTime = firstStartTime + Hours(1)
        val first = createResolvedWeekViewEvent(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime - Hours(1)
        val secondEndTime = firstEndTime + Hours(1)
        val second = createResolvedWeekViewEvent(secondStartTime, secondEndTime)

        assertTrue(first.collidesWith(second))
    }

    @Test
    fun `partly-overlapping events are recognized as colliding`() {
        val firstStartTime = now().withMinutes(0)
        val firstEndTime = firstStartTime + Hours(1)
        val first = createResolvedWeekViewEvent(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime.withMinutes(30)
        val secondEndTime = secondStartTime + Hours(1)
        val second = createResolvedWeekViewEvent(secondStartTime, secondEndTime)

        assertTrue(first.collidesWith(second))
    }
}
