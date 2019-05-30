package com.alamkanak.weekview

import android.view.MotionEvent
import java.util.*
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
    fun getTimeFromPoint(event: MotionEvent): Calendar? {
        val widthPerDay = config.widthPerDay
        val totalDayWidth = widthPerDay + config.columnGap
        val originX = config.currentOrigin.x
        val timeColumnWidth = config.timeColumnWidth

        val leftDaysWithGaps = (ceil((originX / totalDayWidth).toDouble()) * -1).toInt()
        var startPixel = originX + totalDayWidth * leftDaysWithGaps + timeColumnWidth

        val begin = leftDaysWithGaps + 1
        val end = leftDaysWithGaps + config.numberOfVisibleDays + 1

        for (dayNumber in begin..end) {
            val start = max(startPixel, timeColumnWidth)

            val isVisibleHorizontally = startPixel + widthPerDay - start > 0
            val isWithinDay = (event.x > start) and (event.x < startPixel + totalDayWidth)

            if (isVisibleHorizontally && isWithinDay) {
                val day = now().plusDays(dayNumber - 1)

                val originY = config.currentOrigin.y
                val hourHeight = config.hourHeight

                val pixelsFromZero = event.y - originY - config.headerHeight
                val hour = (pixelsFromZero / hourHeight).toInt()
                val minutes = (60 * (pixelsFromZero - hour * hourHeight) / hourHeight).toInt()

                return day.withTime(config.minHour + hour, minutes)
            }

            startPixel += totalDayWidth
        }

        return null
    }

}
