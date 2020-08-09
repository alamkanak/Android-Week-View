package com.alamkanak.weekview

import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max

internal class WeekViewTouchHandler(
    private val viewState: ViewState
) {

    var adapter: WeekView.Adapter<*>? = null

    fun handleClick(x: Float, y: Float) {
        val inCalendarArea = x > viewState.timeColumnWidth
        if (!inCalendarArea) {
            return
        }

        val handled = adapter?.handleClick(x, y) ?: false
        if (!handled) {
            val time = calculateTimeFromPoint(x, y) ?: return
            adapter?.handleEmptyViewClick(time)
        }
    }

    fun handleLongClick(x: Float, y: Float) {
        val handled = adapter?.handleLongClick(x, y) ?: false
        if (!handled) {
            adapter?.handleLongClick(x, y)
        }
    }

    /**
     * Returns the date and time that the user clicked on.
     *
     * @param touchX The x coordinate of the touch event.
     * @param touchY The y coordinate of the touch event.
     * @return The [Calendar] of the clicked position, or null if none was found.
     */
    internal fun calculateTimeFromPoint(
        touchX: Float,
        touchY: Float
    ): Calendar? {
        val widthPerDay = viewState.widthPerDay
        val totalDayWidth = widthPerDay + viewState.columnGap
        val originX = viewState.currentOrigin.x
        val timeColumnWidth = viewState.timeColumnWidth

        val daysFromOrigin = (ceil((originX / totalDayWidth).toDouble()) * -1).toInt()
        var startPixel = originX + daysFromOrigin * totalDayWidth + timeColumnWidth

        val firstDay = daysFromOrigin + 1
        val lastDay = firstDay + viewState.numberOfVisibleDays

        for (dayNumber in firstDay..lastDay) {
            val start = max(startPixel, timeColumnWidth)
            val end = startPixel + totalDayWidth
            val width = end - start

            val isVisibleHorizontally = width > 0
            val isWithinDay = touchX in start..end

            if (isVisibleHorizontally && isWithinDay) {
                val day = now() + Days(dayNumber - 1)

                val hourHeight = viewState.hourHeight
                val pixelsFromMidnight = touchY - viewState.currentOrigin.y - viewState.headerHeight
                val hour = (pixelsFromMidnight / hourHeight).toInt()

                val pixelsFromFullHour = pixelsFromMidnight - hour * hourHeight
                val minutes = ((pixelsFromFullHour / hourHeight) * 60).toInt()

                return day.withTime(viewState.minHour + hour, minutes)
            }

            startPixel += totalDayWidth
        }

        return null
    }
}
