package com.alamkanak.weekview

import java.lang.Math.ceil
import java.util.*

class DrawingContext(
        val dayRange: List<Calendar>,
        val startPixel: Float
) {

    companion object {

        @JvmStatic
        fun create(config: WeekViewConfig): DrawingContext {
            val drawConfig = config.drawingConfig
            val totalDayWidth = config.totalDayWidth
            val leftDaysWithGaps = (ceil((drawConfig.currentOrigin.x / totalDayWidth).toDouble()) * -1).toInt()
            val startPixel = (drawConfig.currentOrigin.x
                    + totalDayWidth * leftDaysWithGaps
                    + drawConfig.timeColumnWidth)

            val start = leftDaysWithGaps + 1
            val end = start + config.numberOfVisibleDays + 1
            val dayRange = DateUtils.getDateRange(start, end)

            return DrawingContext(dayRange, startPixel)
        }
    }

}
