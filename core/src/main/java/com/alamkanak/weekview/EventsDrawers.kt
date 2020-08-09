package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.Calendar

internal class SingleEventsDrawer(
    private val viewState: ViewState,
    private val chipsCache: EventChipsCache
) : Drawer {

    private val eventChipDrawer = EventChipDrawer(viewState)

    override fun draw(canvas: Canvas) {
        for (date in viewState.dateRange) {
            drawEventsForDate(date, canvas)
        }
    }

    private fun drawEventsForDate(
        date: Calendar,
        canvas: Canvas
    ) {
        chipsCache
            .normalEventChipsByDate(date)
            .filter { it.bounds != null }
            .forEach { eventChipDrawer.draw(it, canvas) }
    }
}

internal class AllDayEventsDrawer(
    private val viewState: ViewState,
    private val cache: WeekViewCache
) : Drawer {

    private val eventChipDrawer = EventChipDrawer(viewState)

    override fun draw(canvas: Canvas) {
        val left = viewState.timeColumnWidth
        val top = 0f
        val right = canvas.width.toFloat()
        val bottom = viewState.getTotalHeaderHeight()

        canvas.drawInRect(left, top, right, bottom) {
            val eventChips = cache.allDayEventLayouts
            for ((eventChip, textLayout) in eventChips) {
                eventChipDrawer.draw(eventChip, canvas, textLayout)
            }
        }
    }
}
