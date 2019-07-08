package com.alamkanak.weekview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import java.util.Calendar

internal class SingleEventsDrawer<T>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) : Drawer {

    private val context = view.context
    private val rectCalculator = EventChipRectCalculator<T>(config)

    override fun draw(
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
        cache.eventCache.normalEventChipsByDate(date)
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

    private val RectF.isValidSingleEventRect: Boolean
        get() = (left < right
            && left < view.width
            && top < view.height
            && right > config.timeColumnWidth
            && bottom > config.headerHeight)

}

internal class AllDayEventsDrawer<T>(
    private val context: Context,
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) : Drawer, CachingDrawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) {
        val eventChips = cache.allDayEventLayouts
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

    override fun clear() {
        cache.allDayEventLayouts.clear()
    }

}
