package com.alamkanak.weekview

import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max

internal class WeekViewTouchHandler<T : Any>(
    private val config: WeekViewConfigWrapper,
    private val chipCache: EventChipCache<T>
) {

    var onEventClickListener: OnEventClickListener<T>? = null
    var onEventLongClickListener: OnEventLongClickListener<T>? = null

    var onEmptyViewClickListener: OnEmptyViewClickListener? = null
    var onEmptyViewLongClickListener: OnEmptyViewLongClickListener? = null

    fun handleClick(x: Float, y: Float) {
        val handled = onEventClickListener?.handleClick(x, y) ?: false
        if (!handled) {
            onEmptyViewClickListener?.handleClick(x, y)
        }
    }

    fun handleLongClick(x: Float, y: Float) {
        val handled = onEventLongClickListener?.handleLongClick(x, y) ?: false
        if (!handled) {
            onEmptyViewLongClickListener?.handleLongClick(x, y)
        }
    }

    /**
     * Returns the date and time that the user clicked on.
     *
     * @param touchX The x coordinate of the touch event.
     * @param touchY The y coordinate of the touch event.
     * @return The [Calendar] of the clicked position, or null if none was found.
     */
    fun calculateTimeFromPoint(
        touchX: Float,
        touchY: Float
    ): Calendar? {
        val widthPerDay = config.widthPerDay
        val totalDayWidth = widthPerDay + config.columnGap
        val originX = config.currentOrigin.x
        val timeColumnWidth = config.timeColumnWidth

        val daysFromOrigin = (ceil((originX / totalDayWidth).toDouble()) * -1).toInt()
        var startPixel = originX + daysFromOrigin * totalDayWidth + timeColumnWidth

        val firstDay = daysFromOrigin + 1
        val lastDay = firstDay + config.numberOfVisibleDays

        for (dayNumber in firstDay..lastDay) {
            val start = max(startPixel, timeColumnWidth)
            val end = startPixel + totalDayWidth
            val width = end - start

            val isVisibleHorizontally = width > 0
            val isWithinDay = touchX in start..end

            if (isVisibleHorizontally && isWithinDay) {
                val day = now() + Days(dayNumber - 1)

                val hourHeight = config.hourHeight
                val pixelsFromMidnight = touchY - config.currentOrigin.y - config.headerHeight
                val hour = (pixelsFromMidnight / hourHeight).toInt()

                val pixelsFromFullHour = pixelsFromMidnight - hour * hourHeight
                val minutes = ((pixelsFromFullHour / hourHeight) * 60).toInt()

                return day.withTime(config.minHour + hour, minutes)
            }

            startPixel += totalDayWidth
        }

        return null
    }

    private fun findHitEvent(x: Float, y: Float): EventChip<T>? {
        val candidates = chipCache.allEventChips.filter { it.isHit(x, y) }
        return when {
            candidates.isEmpty() -> null
            // Two events hit. This is most likely because an all-day event was clicked, but a
            // single event is rendered underneath it. We return the all-day event.
            candidates.size == 2 -> candidates.first { it.event.isAllDay }
            else -> candidates.first()
        }
    }

    private fun OnEventClickListener<T>.handleClick(x: Float, y: Float): Boolean {
        val eventChip = findHitEvent(x, y) ?: return false
        val isInHeader = y <= config.headerHeight

        if (eventChip.event.isNotAllDay && isInHeader) {
            // The user tapped in the header area and a single event that is rendered below it
            // has recognized the tap. We ignore this.
            return false
        }

        val data = eventChip.originalEvent.data
        val rect = checkNotNull(eventChip.bounds)
        onEventClick(data, rect)

        return true
    }

    private fun OnEmptyViewClickListener.handleClick(x: Float, y: Float) {
        val isInCalendarArea = x > config.timeColumnWidth && y > config.headerHeight
        if (isInCalendarArea) {
            calculateTimeFromPoint(x, y)?.let { time ->
                onEmptyViewClicked(time)
            }
        }
    }

    private fun OnEventLongClickListener<T>.handleLongClick(x: Float, y: Float): Boolean {
        val isInHeader = y <= config.headerHeight
        val eventChip = findHitEvent(x, y) ?: return false

        if (eventChip.event.isNotAllDay && isInHeader) {
            // The user tapped in the header area and a single event that is rendered below it
            // has recognized the tap. We ignore this.
            return false
        }

        val data = eventChip.originalEvent.data
        val rect = checkNotNull(eventChip.bounds)
        onEventLongClick(data, rect)

        return true
    }

    private fun OnEmptyViewLongClickListener.handleLongClick(x: Float, y: Float) {
        val isInCalendarArea = x > config.timeColumnWidth && y > config.headerHeight
        if (isInCalendarArea) {
            calculateTimeFromPoint(x, y)?.let { time ->
                onEmptyViewLongClick(time)
            }
        }
    }
}
