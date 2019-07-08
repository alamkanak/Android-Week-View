package com.alamkanak.weekview

import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
import android.text.TextUtils.TruncateAt.END
import android.text.style.StyleSpan

internal class EventsUpdater<T>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper,
    private val megaCache: MegaCache<T>
) : Updater {

    private val context = view.context
    private val rectCalculator = EventChipRectCalculator<T>(config)

    /**
     * Compute the StaticLayout for all-day events to update the header height
     *
     * @param drawingContext The [DrawingContext] to use for drawing
     * @return The association of [EventChip]s with their [StaticLayout]s
     */
    override fun update(drawingContext: DrawingContext) {
        config.setCurrentAllDayEventHeight(0)
        megaCache.allDayEventLayouts.clear()

        drawingContext
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                val eventChips = megaCache.eventCache.allDayEventChipsByDate(date)

                for (eventChip in eventChips) {
                    val layout = calculateLayoutForAllDayEvent(eventChip, startPixel)
                    if (layout != null) {
                        megaCache.allDayEventLayouts.add(Pair(eventChip, layout))
                    }
                }
            }
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

        val width = right - left - (config.eventPadding * 2)
        val height = bottom - top - (config.eventPadding * 2)

        if (height < 0f) {
            return null
        }

        if (width < 0f) {
            // This is needed if there are many all-day events
            val dummyTextLayout = createDummyTextLayout(event)
            val chipHeight = dummyTextLayout.height + config.eventPadding * 2
            rect.bottom = rect.top + chipHeight
            setAllDayEventHeight(chipHeight)
            return dummyTextLayout
        }

        val title = when (val resource = event.titleResource) {
            is WeekViewEvent.TextResource.Id -> context.getString(resource.resId)
            is WeekViewEvent.TextResource.Value -> resource.text
            null -> ""
        }

        val text = SpannableStringBuilder(title)
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)

        val location = when (val resource = event.locationResource) {
            is WeekViewEvent.TextResource.Id -> context.getString(resource.resId)
            is WeekViewEvent.TextResource.Value -> resource.text
            null -> null
        }

        location?.let {
            text.append(' ')
            text.append(it)
        }

        val availableWidth = width.toInt()

        // Get text dimensions.
        val textPaint = event.getTextPaint(context, config)
        val textLayout = StaticLayout(text, textPaint, availableWidth, ALIGN_NORMAL, 1f, 0f, false)

        val lineHeight = textLayout.height / textLayout.lineCount

        // For an all day event, we display just one line
        val chipHeight = lineHeight + config.eventPadding * 2
        rect.bottom = rect.top + chipHeight

        // Compute the available height on the right size of the chip
        val availableHeight = (rect.bottom - top - (config.eventPadding * 2).toFloat()).toInt()

        val finalTextLayout = if (availableHeight >= lineHeight) {
            ellipsizeTextToFitChip(
                eventChip, text, textLayout, config, availableHeight, availableWidth)
        } else {
            textLayout
        }

        // Refresh the header height
        setAllDayEventHeight(chipHeight)

        return finalTextLayout
    }

    private fun setAllDayEventHeight(height: Int) {
        if (height > config.getCurrentAllDayEventHeight()) {
            config.setCurrentAllDayEventHeight(height)
        }
    }

    /**
     * Creates a dummy text layout that is only used to determine the height of all-day events.
     */
    private fun createDummyTextLayout(
        event: WeekViewEvent<T>
    ): StaticLayout {
        val textPaint = event.getTextPaint(context, config)
        return StaticLayout("", textPaint, 0, ALIGN_NORMAL, 1f, 0f, false)
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
        val textPaint = eventChip.event.getTextPaint(context, config)

        val lineHeight = textLayout.lineHeight
        var availableLineCount = availableHeight / lineHeight

        val rect = checkNotNull(eventChip.rect)
        val left = rect.left
        val right = rect.right

        do {
            // Ellipsize text to fit into event rect.
            val availableArea = availableLineCount * availableWidth
            val ellipsized = TextUtils.ellipsize(text, textPaint, availableArea.toFloat(), END)
            val width = (right - left - (config.eventPadding * 2).toFloat()).toInt()

            if (eventChip.event.isAllDay && width < 0) {
                // This day contains too many all-day events. We only draw the event chips,
                // but don't attempt to draw the event titles.
                break
            }

            textLayout = StaticLayout(ellipsized, textPaint, width, ALIGN_NORMAL, 1f, 0f, false)

            // Reduce line count.
            availableLineCount--

            // Repeat until text is short enough.
        } while (textLayout.height > availableHeight)

        return textLayout
    }

    private val RectF.isValidAllDayEventRect: Boolean
        get() = (left < right
            && left < view.width
            && top < view.height
            && right > config.timeColumnWidth
            && bottom > 0)

}
