package com.alamkanak.weekview

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import java.util.Calendar

private const val SCROLL_THRESHOLD = 80

internal class DragHandler(
    private val viewState: ViewState,
    private val touchHandler: WeekViewTouchHandler,
    private val eventsCacheProvider: EventsCacheProvider,
    private val navigator: Navigator,
    private val dragListener: (Long) -> Unit,
    private val eventsProcessorProvider: () -> EventsProcessor?,
) {

    private val executor = DragScrollExecutor()

    private val draggedEvent: ResolvedWeekViewEntity?
        get() {
            val eventsCache = eventsCacheProvider() ?: return null
            val eventId = viewState.dragState?.eventId ?: return null
            return eventsCache[eventId]
        }

    val isDragging: Boolean
        get() = viewState.dragState != null

    fun startDragAndDrop(eventChip: EventChip, x: Float, y: Float) {
        viewState.dragState = DragState(
            eventId = eventChip.eventId,
            draggedEventStartTime = eventChip.event.startTime,
            dragStartTime = requireNotNull(touchHandler.calculateTimeFromPoint(x, y)),
        )

        navigator.requestInvalidation()
    }

    fun updateDragAndDrop(e: MotionEvent) {
        val currentDragLocation = touchHandler.calculateTimeFromPoint(touchX = e.x, touchY = e.y)

        if (currentDragLocation != null) {
            val newEventStart = calculateNewEventStart(currentDragLocation)
            val sanitizedEventStart = sanitizeEventStart(newEventStart)
            updateDraggedEvent(newStartTime = sanitizedEventStart)
            scrollIfNecessary(e)
        }

        navigator.requestInvalidation()
    }

    fun finishDragAndDrop() {
        val draggedEventId = requireNotNull(viewState.dragState).eventId
        viewState.dragState = null
        executor.stop()
        navigator.requestInvalidation()
        dragListener(draggedEventId)
    }

    private fun calculateNewEventStart(
        currentDragLocation: Calendar,
    ): Calendar {
        val dragState = requireNotNull(viewState.dragState)
        val delta = currentDragLocation minutesUntil dragState.dragStartTime
        return dragState.draggedEventStartTime + delta
    }

    private fun sanitizeEventStart(
        rawEventStart: Calendar,
    ): Calendar {
        val minutesBeyondQuarterHour = rawEventStart.minute % 15
        val minutesUntilNextQuarterHour = 15 - minutesBeyondQuarterHour

        return if (minutesBeyondQuarterHour >= 8) {
            // Go to next quarter hour
            rawEventStart + Minutes(minutesUntilNextQuarterHour)
        } else {
            // Go to previous quarter hour
            rawEventStart - Minutes(minutesBeyondQuarterHour)
        }
    }

    private fun updateDraggedEvent(newStartTime: Calendar) {
        val originalEvent = draggedEvent ?: return
        val updatedEvent = originalEvent.createCopy(
            startTime = newStartTime,
            endTime = newStartTime + Minutes(originalEvent.durationInMinutes),
        )

        val eventsProcessor = eventsProcessorProvider() ?: return
        eventsProcessor.updateDraggedEntity(updatedEvent, viewState)
    }

    private fun scrollIfNecessary(e: MotionEvent) {
        val isAtTopOfCalendarArea = (e.y - viewState.calendarGridBounds.top) < SCROLL_THRESHOLD
        val isAtBottomOfCalendarArea = (viewState.calendarGridBounds.bottom - e.y) < SCROLL_THRESHOLD
        val isAtStartOfCalendarArea = (e.x - viewState.calendarGridBounds.left) < SCROLL_THRESHOLD
        val isAtEndOfCalendarArea = (viewState.calendarGridBounds.right - e.x) < SCROLL_THRESHOLD

        when {
            isAtTopOfCalendarArea -> scrollUp()
            isAtBottomOfCalendarArea -> scrollDown()
            isAtStartOfCalendarArea -> scrollLeft()
            isAtEndOfCalendarArea -> scrollRight()
            else -> executor.stop()
        }
    }

    private fun scrollUp() {
        executor.execute {
            if (viewState.currentOrigin.y == 0f) {
                executor.stop()
                return@execute
            }

            val draggedEvent = draggedEvent ?: return@execute
            updateDraggedEvent(newStartTime = draggedEvent.startTime - Minutes(15))

            val distance = viewState.hourHeight / 4f
            navigator.scrollVerticallyBy(distance = distance * (-1))
        }
    }

    private fun scrollDown() {
        executor.execute {
            val maxY = viewState.dayHeight - (viewState.headerHeight + viewState.calendarGridBounds.height())
            if (viewState.currentOrigin.y == maxY) {
                executor.stop()
                return@execute
            }

            val draggedEvent = draggedEvent ?: return@execute
            updateDraggedEvent(newStartTime = draggedEvent.startTime + Minutes(15))

            val distance = viewState.hourHeight / 4f
            navigator.scrollVerticallyBy(distance = distance)
        }
    }

    private fun scrollLeft() {
        executor.execute(delay = 600) {
            val draggedEvent = draggedEvent ?: return@execute
            updateDraggedEvent(newStartTime = draggedEvent.startTime - Days(1))

            val date = draggedEvent.startTime.atStartOfDay
            navigator.scrollHorizontallyTo(date - Days(1))
        }
    }

    private fun scrollRight() {
        executor.execute(delay = 600) {
            val draggedEvent = draggedEvent ?: return@execute
            updateDraggedEvent(newStartTime = draggedEvent.startTime + Days(1))

            val date = draggedEvent.startTime.atStartOfDay
            navigator.scrollHorizontallyTo(date + Days(1))
        }
    }

    private class DragScrollExecutor {

        private val handler = Handler(Looper.getMainLooper())
        private var runnable: Runnable? = null

        fun execute(delay: Long = 100L, code: () -> Unit) {
            if (runnable != null) {
                // There’s already an automatic scroll happening
                return
            }

            runnable = object : Runnable {
                override fun run() {
                    code()
                    handler.postDelayed(this, delay)
                }
            }
            runnable?.run()
        }

        fun stop() {
            runnable?.let {
                handler.removeCallbacks(it)
            }
            runnable = null
        }
    }
}
