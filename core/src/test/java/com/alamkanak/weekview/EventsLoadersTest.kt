package com.alamkanak.weekview

import android.content.Context
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.alamkanak.weekview.model.Event
import com.alamkanak.weekview.util.createDate
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar.FEBRUARY
import java.util.Calendar.MARCH
import org.mockito.Mockito.`when` as whenever

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EventsLoadersTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        EmojiCompat.init(BundledEmojiCompatConfig(context))
    }

    @Test
    fun `CachingEventsLoader is called correctly`() = weekViewRobot(context) {
        assertCachingEventsLoader()

        val date = createDate(2019, FEBRUARY, 24)
        val event = Event(date, date + Hours(1)).toWeekViewEvent()

        fillCache(event)
        assertDateRangeContains(date, event)
    }

    @Test
    fun `PagedEventsLoader is called correctly`() = weekViewRobot(context) {
        val listener = mock(OnLoadMoreListener::class.java)
        weekView.onLoadMoreListener = listener

        assertPagedEventsLoader()

        val date = createDate(2019, FEBRUARY, 24)
        val fetchRange = FetchRange.create(date)
        scrollToDate(date)
        assertOnLoadMoreCalled(listener, fetchRange.periods)

        val newDate = createDate(2019, MARCH, 1)
        val newFetchRange = FetchRange.create(newDate)
        scrollToDate(newDate)
        assertOnLoadMoreCalled(listener, newFetchRange.next)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `LegacyEventsLoader is called correctly`() = weekViewRobot(context) {
        val listener = mock(OnMonthChangeListener::class.java) as OnMonthChangeListener<Event>
        weekView.onMonthChangeListener = listener

        val date = createDate(2019, FEBRUARY, 24)
        val event = Event(date, date + Hours(1)).toWeekViewEvent()
        val fetchRange = FetchRange.create(date)

        fetchRange.current.run {
            whenever(listener.onMonthChange(startDate, endDate)).thenReturn(listOf(event))
        }

        assertLegacyEventsLoader()
        scrollToDate(date)

        val events = assertOnMonthChangeCalled(fetchRange)
        assertThat(events).contains(event)
    }
}
