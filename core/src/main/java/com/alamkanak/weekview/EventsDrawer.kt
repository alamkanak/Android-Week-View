package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.StaticLayout
import java.util.Calendar

internal class EventsDrawer<T>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) {

    private val context = view.context

    // TODO Move out of EventsDrawer
    private val eventsCalculator = EventsCalculator(view, config, cache)

    private val rectCalculator = EventChipRectCalculator<T>(config)

    fun drawSingleEvents(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) {
        drawingContext
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                drawEventsForDate(date, startPixel, canvas, paint)
            }
    }

    private fun drawEventsForDate(
        date: Calendar,
        startPixel: Float,
        canvas: Canvas,
        paint: Paint
    ) {
        cache.normalEventChipsByDate(date)
            .filter { it.event.isWithin(config.minHour, config.maxHour) }
            .forEach {
                val chipRect = rectCalculator.calculateSingleEvent(it, startPixel)
                if (chipRect.isValidSingleEventRect) {
                    it.rect = chipRect
                    it.draw(context, config, canvas, paint)
                } else {
                    it.rect = null
                }
            }
    }

    /**
     * Compute the StaticLayout for all-day events to update the header height
     *
     * @param drawingContext The [DrawingContext] to use for drawing
     * @return The association of [EventChip]s with his StaticLayout
     */
    fun prepareDrawAllDayEvents(
        drawingContext: DrawingContext
    ): List<Pair<EventChip<T>, StaticLayout>> {
        return eventsCalculator.update(drawingContext)
    }

    /**
     * Draw all the all-day events of a particular day.
     *
     * @param eventChips The list of pairs of [EventChip] and [StaticLayout] to draw
     * @param canvas The canvas to draw upon.
     */
    fun drawAllDayEvents(
        eventChips: List<Pair<EventChip<T>, StaticLayout>>,
        canvas: Canvas,
        paint: Paint
    ) {
        for (pair in eventChips) {
            val eventChip = pair.first
            val layout = pair.second
            eventChip.draw(context, config, layout, canvas, paint)
        }

        // Hide events when they are in the top left corner
        val headerBackground = config.headerBackgroundPaint

        val headerRowBottomLine = if (config.showHeaderRowBottomLine) {
            config.headerRowBottomLinePaint.strokeWidth
        } else {
            0f
        }

        val height = config.headerHeight - headerRowBottomLine * 1.5f
        val width = config.timeTextWidth + config.timeColumnPadding * 2

        canvas.clipRect(0f, 0f, width, height)
        canvas.drawRect(0f, 0f, width, height, headerBackground)

        canvas.restore()
        canvas.save()
    }

    private val RectF.isValidSingleEventRect: Boolean
        get() = (left < right
            && left < view.width
            && top < view.height
            && right > config.timeColumnWidth
            && bottom > config.headerHeight)

}
