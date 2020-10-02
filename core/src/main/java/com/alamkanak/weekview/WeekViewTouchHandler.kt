package com.alamkanak.weekview

import java.util.Calendar

internal class WeekViewTouchHandler(
    private val viewState: ViewState
) {

    var adapter: WeekView.Adapter<*>? = null

    fun handleClick(x: Float, y: Float) {
        val inCalendarArea = x > viewState.timeColumnWidth
        val inAllDayEventsToggleArea = viewState.toggleAllDayEventsAreaBounds.contains(x, y)

        if (inAllDayEventsToggleArea && viewState.showAllDayEventsToggleArrow) {
            viewState.allDayEventsExpanded = !viewState.allDayEventsExpanded
            adapter?.updateObserver()
            return
        }

        if (!inCalendarArea) {
            return
        }

        val handled = adapter?.handleClick(x, y) ?: false
        if (!handled && y > viewState.headerHeight) {
            val time = calculateTimeFromPoint(x, y) ?: return
            adapter?.handleEmptyViewClick(time)
        }
    }

    fun handleLongClick(x: Float, y: Float) {
        val inCalendarArea = x > viewState.timeColumnWidth
        if (!inCalendarArea) {
            return
        }

        val handled = adapter?.handleLongClick(x, y) ?: false
        if (!handled && y > viewState.headerHeight) {
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
        val dateRange = viewState.dateRangeWithStartPixels

        for ((date, startPixel) in dateRange) {
            val endPixel = startPixel + viewState.dayWidth
            val isWithinDay = touchX in startPixel..endPixel

            if (isWithinDay) {
                val hourHeight = viewState.hourHeight
                val pixelsFromMidnight = touchY - viewState.currentOrigin.y - viewState.headerHeight
                val hour = (pixelsFromMidnight / hourHeight).toInt()

                val pixelsFromFullHour = pixelsFromMidnight - hour * hourHeight
                val minutes = ((pixelsFromFullHour / hourHeight) * 60).toInt()

                return date.withTime(viewState.minHour + hour, minutes)
            }
        }

        return null
    }
}
