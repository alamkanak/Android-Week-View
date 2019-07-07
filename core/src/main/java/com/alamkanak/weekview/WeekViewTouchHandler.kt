package com.alamkanak.weekview

import android.view.MotionEvent
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max

internal class WeekViewTouchHandler(
    private val config: WeekViewConfigWrapper
) {

    /**
     * Returns the date and time that the user clicked on.
     *
     * @param event The [MotionEvent] of the touch event.
     * @return The [Calendar] with the time and date of the clicked position.
     */
    fun calculateTimeFromPoint(event: MotionEvent): Calendar? {
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
            val isWithinDay = event.x in start..end

            if (isVisibleHorizontally && isWithinDay) {
                val day = now().plusDays(dayNumber - 1)

                val hourHeight = config.hourHeight
                val pixelsFromMidnight = event.y - config.currentOrigin.y - config.headerHeight
                val hour = (pixelsFromMidnight / hourHeight).toInt()

                val pixelsFromFullHour = pixelsFromMidnight - hour * hourHeight
                val minutes = (pixelsFromFullHour / hourHeight).toInt() * 60

                return day.withTime(config.minHour + hour, minutes)
            }

            startPixel += totalDayWidth
        }

        return null
    }

}
