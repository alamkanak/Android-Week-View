package com.alamkanak.weekview

import android.view.MotionEvent
import com.alamkanak.weekview.date.now
import com.alamkanak.weekview.date.plusDays
import com.alamkanak.weekview.date.withTime
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

internal class WeekViewTouchHandler(
        private val config: WeekViewConfigWrapper
) {

    /**
     * Returns the date and time where the user clicked on.
     *
     * @param event The [MotionEvent] of the touch event.
     * @return The time and date at the clicked position.
     */
    fun getTimeFromPoint(event: MotionEvent): Calendar? {
        val touchX = event.x
        val touchY = event.y

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
            val isWithinDay = (touchX > start) and (touchX < startPixel + totalDayWidth)

            if (isVisibleHorizontally && isWithinDay) {
                val day = now().plusDays(dayNumber - 1)

                val originY = config.currentOrigin.y
                val hourHeight = config.hourHeight

                val pixelsFromZero = touchY - originY - config.headerHeight
                val hour = (pixelsFromZero / hourHeight).toInt()
                val minutes = (60 * (pixelsFromZero - hour * hourHeight) / hourHeight).toInt()

                return day.withTime(config.minHour + hour, minutes)
            }

            startPixel += totalDayWidth
        }

        return null
    }

}
