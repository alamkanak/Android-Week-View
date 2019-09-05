package com.alamkanak.weekview

import android.graphics.RectF
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
import android.text.TextUtils.TruncateAt.END
import android.text.style.StyleSpan

internal class AllDayEventsUpdater<T>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>,
    private val eventsCacheWrapper: EventsCacheWrapper<T>,
    private val chipCache: EventChipCache<T>
) : Updater {

    private val context = view.context
    private val rectCalculator = EventChipRectCalculator<T>(config)

    private var previousHorizontalOrigin: Float? = null
    private val previousAllDayEventIds = mutableSetOf<Long>()

    private val eventsCache: EventsCache<T>
        get() = eventsCacheWrapper.get()

    override fun isRequired(drawingContext: DrawingContext): Boolean {
        val didScrollHorizontally = previousHorizontalOrigin != config.currentOrigin.x
        val allDayEvents = eventsCache[drawingContext.dateRange].filter { it.isAllDay }
        val allDayEventIds = allDayEvents.map { it.id }.toSet()
        val didEventsChange = allDayEventIds != previousAllDayEventIds

        return (didScrollHorizontally || didEventsChange).also {
            previousAllDayEventIds.clear()
            previousAllDayEventIds += allDayEventIds
        }
    }

    override fun update(drawingContext: DrawingContext) {
        previousHorizontalOrigin = config.currentOrigin.x
        config.setCurrentAllDayEventHeight(0)
        cache.clearAllDayEventLayouts()

        drawingContext
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                // If we use a horizontal margin in the day view, we need to offset the start pixel.
                val modifiedStartPixel = when {
                    config.isSingleDay -> startPixel + config.eventMarginHorizontal.toFloat()
                    else -> startPixel
                }

                val eventChips = chipCache.allDayEventChipsByDate(date)
                for (eventChip in eventChips) {
                    val layout = calculateLayoutForAllDayEvent(eventChip, modifiedStartPixel)
                    if (layout != null) {
                        cache.allDayEventLayouts.add(Pair(eventChip, layout))
                        previousAllDayEventIds.add(eventChip.event.id)
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

        val fullHorizontalPadding = config.eventPaddingHorizontal * 2
        val fullVerticalPadding = config.eventPaddingVertical * 2

        val width = right - left - fullHorizontalPadding
        val height = bottom - top - fullVerticalPadding

        if (height < 0f) {
            return null
        }

        if (width < 0f) {
            // This is needed if there are many all-day events
            val dummyTextLayout = createDummyTextLayout(event)
            val chipHeight = dummyTextLayout.height + fullVerticalPadding
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
        val textLayout = TextLayoutBuilder.build(text, textPaint, availableWidth)

        val lineHeight = textLayout.height / textLayout.lineCount

        // For an all day event, we display just one line
        val chipHeight = lineHeight + fullVerticalPadding
        rect.bottom = rect.top + chipHeight

        // Compute the available height on the right size of the chip
        val availableHeight = (rect.bottom - top - fullVerticalPadding.toFloat()).toInt()

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
        if (height > config.currentAllDayEventHeight) {
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
        return TextLayoutBuilder.build("", textPaint, width = 0)
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
            val width = (right - left - (config.eventPaddingHorizontal * 2).toFloat()).toInt()

            if (eventChip.event.isAllDay && width < 0) {
                // This day contains too many all-day events. We only draw the event chips,
                // but don't attempt to draw the event titles.
                break
            }

            textLayout = TextLayoutBuilder.build(ellipsized, textPaint, width)
            availableLineCount--

            // Repeat until text is short enough.
        } while (textLayout.height > availableHeight)

        return textLayout
    }

    private val RectF.isValidAllDayEventRect: Boolean
        get() = (left < right &&
            left < view.width &&
            top < view.height &&
            right > config.timeColumnWidth &&
            bottom > 0)
}
