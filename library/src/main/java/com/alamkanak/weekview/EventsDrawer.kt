package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils.TruncateAt.END
import android.text.TextUtils.ellipsize
import android.text.style.StyleSpan
import android.util.Pair
import java.util.ArrayList
import java.util.Calendar

internal class EventsDrawer<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) {

    private val rectCalculator = EventChipRectCalculator<T>(config)
    private val staticLayoutCache = ArrayList<Pair<EventChip<T>, StaticLayout>>()

    fun drawSingleEvents(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) {
        var startPixel = drawingContext.startPixel

        for (date in drawingContext.dateRange) {
            if (config.isSingleDay) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel += config.eventMarginHorizontal
            }

            drawEventsForDate(date, startPixel, canvas, paint)

            // In the next iteration, start from the next day.
            startPixel += config.totalDayWidth
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
                    it.draw(config, canvas, paint)
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
        config.setCurrentAllDayEventHeight(0)
        staticLayoutCache.clear()

        var startPixel = drawingContext.startPixel

        for (date in drawingContext.dateRange) {
            if (config.isSingleDay) {
                startPixel += config.eventMarginHorizontal
            }

            val eventChips = cache.allDayEventChipsByDate(date)

            for (eventChip in eventChips) {
                calculateLayoutForAllDayEvent(eventChip, startPixel)?.let {
                    staticLayoutCache.add(Pair(eventChip, it))
                }
            }

            startPixel += config.totalDayWidth
        }

        return staticLayoutCache
    }

    private fun calculateLayoutForAllDayEvent(
        eventChip: EventChip<T>,
        startPixel: Float
    ): StaticLayout? {
        val chipRect = rectCalculator.calculateAllDayEvent(eventChip, startPixel)
        if (chipRect.isValidAllDayEventRect) {
            eventChip.rect = chipRect
            return calculateChipTextLayout(eventChip)
        } else {
            eventChip.rect = null
        }
        return null
    }

    private fun calculateChipTextLayout(
        eventChip: EventChip<T>
    ): StaticLayout? {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.rect)

        val left = rect.left
        val top = rect.top
        val right = rect.right
        val bottom = rect.bottom

        val negativeWidth = right - left - (config.eventPadding * 2).toFloat() < 0
        val negativeHeight = bottom - top - (config.eventPadding * 2).toFloat() < 0
        if (negativeWidth || negativeHeight) {
            return null
        }

        val text = SpannableStringBuilder(event.title)
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)

        event.location?.let {
            text.append(' ')
            text.append(it)
        }

        val availableWidth = (right - left - (config.eventPadding * 2).toFloat()).toInt()

        // Get text dimensions.
        val textPaint = event.getTextPaint(config)
        val textLayout = StaticLayout(text, textPaint, availableWidth, ALIGN_NORMAL, 1f, 0f, false)

        val lineHeight = textLayout.height / textLayout.lineCount

        // For an all day event, we display just one line
        val chipHeight = lineHeight + config.eventPadding * 2
        rect.bottom = rect.top + chipHeight

        // Compute the available height on the right size of the chip
        val availableHeight = (rect.bottom - top - (config.eventPadding * 2).toFloat()).toInt()

        val finalTextLayout = if (availableHeight >= lineHeight) {
            ellipsizeTextToFitChip(eventChip, text, textLayout, config, availableHeight, availableWidth)
        } else {
            textLayout
        }

        // Refresh the header height
        if (chipHeight > config.getCurrentAllDayEventHeight()) {
            config.setCurrentAllDayEventHeight(chipHeight)
        }

        return finalTextLayout
    }

    private fun ellipsizeTextToFitChip(
        eventChip: EventChip<T>,
        text: CharSequence,
        staticLayout: StaticLayout,
        config: WeekViewConfigWrapper,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        var textLayout = staticLayout
        val textPaint = eventChip.event.getTextPaint(config)

        val lineHeight = textLayout.lineHeight
        var availableLineCount = availableHeight / lineHeight

        val rect = checkNotNull(eventChip.rect)
        val left = rect.left
        val right = rect.right

        do {
            // Ellipsize text to fit into event rect.
            val availableArea = availableLineCount * availableWidth
            val ellipsized = ellipsize(text, textPaint, availableArea.toFloat(), END)
            val width = (right - left - (config.eventPadding * 2).toFloat()).toInt()
            textLayout = StaticLayout(ellipsized, textPaint, width, ALIGN_NORMAL, 1.0f, 0.0f, false)

            // Reduce line count.
            availableLineCount--

            // Repeat until text is short enough.
        } while (textLayout.height > availableHeight)

        return textLayout
    }

    /**
     * Draw all the all-day events of a particular day.
     *
     * @param eventChips The list of Pair<[EventChip], StaticLayout>s to draw
     * @param canvas         The canvas to draw upon.
     */
    fun drawAllDayEvents(
        eventChips: List<Pair<EventChip<T>, StaticLayout>>?,
        canvas: Canvas,
        paint: Paint
    ) {
        if (eventChips == null) {
            return
        }

        for (pair in eventChips) {
            val eventChip = pair.first
            val layout = pair.second
            eventChip.draw(config, layout, canvas, paint)
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
            && left < WeekView.width
            && top < WeekView.height
            && right > config.timeColumnWidth
            && bottom > config.headerHeight)

    private val RectF.isValidAllDayEventRect: Boolean
        get() = (left < right
            && left < WeekView.width
            && top < WeekView.height
            && right > config.timeColumnWidth
            && bottom > 0)

}
