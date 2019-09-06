package com.alamkanak.weekview

import android.content.Context
import android.graphics.Canvas
import java.util.Calendar

internal class SingleEventsDrawer<T>(
    context: Context,
    config: WeekViewConfigWrapper,
    private val chipCache: EventChipCache<T>
) : Drawer {

    private val eventChipDrawer = EventChipDrawer<T>(context, config)

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas
    ) = with(drawingContext) {
        for (date in dateRange) {
            drawEventsForDate(date, canvas)
        }
    }

    private fun drawEventsForDate(
        date: Calendar,
        canvas: Canvas
    ) {
        chipCache
            .normalEventChipsByDate(date)
            .filter { it.rect != null }
            .forEach { eventChipDrawer.draw(it, canvas) }
    }
}

internal class AllDayEventsDrawer<T>(
    context: Context,
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) : CachingDrawer {

    private val eventChipDrawer = EventChipDrawer<T>(context, config)

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas
    ) {
        val left = config.timeColumnWidth
        val top = 0f
        val right = canvas.width.toFloat()
        val bottom = config.getTotalHeaderHeight()

        canvas.drawInRect(left, top, right, bottom) {
            val eventChips = cache.allDayEventLayouts
            for ((eventChip, textLayout) in eventChips) {
                eventChipDrawer.draw(eventChip, canvas, textLayout)
            }
        }
    }

    override fun clear() {
        cache.clearAllDayEventLayouts()
    }
}
