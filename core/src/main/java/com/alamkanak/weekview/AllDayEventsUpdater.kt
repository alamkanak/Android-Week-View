package com.alamkanak.weekview

import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import kotlin.math.roundToInt

internal class AllDayEventsUpdater<T : Any>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>,
    private val chipsCache: EventChipsCache<T>
) : Updater {

    private val boundsCalculator = EventChipBoundsCalculator<T>(config)
    private val spannableStringBuilder = SpannableStringBuilder()

    private var previousHorizontalOrigin: Float? = null
    private var dummyTextLayout: StaticLayout? = null

    override fun isRequired(drawingContext: DrawingContext): Boolean {
        val didScrollHorizontally = previousHorizontalOrigin != config.currentOrigin.x
        val dateRange = drawingContext.dateRange
        val containsNewChips = chipsCache.allDayEventChipsInDateRange(dateRange).any { it.bounds == null }
        return didScrollHorizontally || containsNewChips
    }

    override fun update(drawingContext: DrawingContext) {
        cache.clearAllDayEventLayouts()

        val datesWithStartPixels = drawingContext.dateRangeWithStartPixels
        for ((date, startPixel) in datesWithStartPixels) {
            // If we use a horizontal margin in the day view, we need to offset the start pixel.
            val modifiedStartPixel = when {
                config.isSingleDay -> startPixel + config.eventMarginHorizontal.toFloat()
                else -> startPixel
            }

            val eventChips = chipsCache.allDayEventChipsByDate(date)
            for (eventChip in eventChips) {
                calculateTextLayout(eventChip, modifiedStartPixel)
            }
        }

        val maximumChipHeight = cache.allDayEventLayouts.keys
            .mapNotNull { it.bounds }
            .map { it.height().roundToInt() }
            .max() ?: 0

        config.updateAllDayEventHeight(maximumChipHeight)
    }

    private fun calculateTextLayout(
        eventChip: EventChip<T>,
        startPixel: Float
    ) {
        val chipRect = boundsCalculator.calculateAllDayEvent(eventChip, startPixel)
        eventChip.bounds = if (chipRect.isValidEventBounds) chipRect else null

        if (chipRect.isValidEventBounds) {
            val textLayout = calculateChipTextLayout(eventChip)
            if (textLayout != null) {
                cache.allDayEventLayouts[eventChip] = textLayout
            }
        }
    }

    private fun calculateChipTextLayout(
        eventChip: EventChip<T>
    ): StaticLayout? {
        val event = eventChip.event
        val bounds = checkNotNull(eventChip.bounds)

        val fullHorizontalPadding = config.eventPaddingHorizontal * 2
        val fullVerticalPadding = config.eventPaddingVertical * 2

        val width = bounds.width() - fullHorizontalPadding
        val height = bounds.height() - fullVerticalPadding

        if (height < 0) {
            return null
        }

        if (width < 0) {
            // This happens if there are many all-day events
            val dummyTextLayout = createDummyTextLayout(event)
            val chipHeight = dummyTextLayout.height + fullVerticalPadding
            bounds.bottom = bounds.top + chipHeight
            return dummyTextLayout
        }

        spannableStringBuilder.clear()
        val title = event.title.emojify()
        spannableStringBuilder.append(title)

        // val title = event.title.emojify()
        // val text = SpannableStringBuilder(title)
        // text.setSpan(StyleSpan(Typeface.BOLD))

        val location = event.location?.emojify()
        if (location != null) {
            spannableStringBuilder.append(' ')
            spannableStringBuilder.append(location)
        }

        val text = spannableStringBuilder.build()
        val availableWidth = width.toInt()

        val textPaint = config.getTextPaint(event)
        val textLayout = text.toTextLayout(textPaint, availableWidth)
        val lineHeight = textLayout.height / textLayout.lineCount

        // For an all day event, we display just one line
        val chipHeight = lineHeight + fullVerticalPadding
        bounds.bottom = bounds.top + chipHeight

        return eventChip.ellipsizeText(text, availableWidth, existingTextLayout = textLayout)
    }

    /**
     * Creates a dummy text layout that is only used to determine the height of all-day events.
     */
    private fun createDummyTextLayout(
        event: ResolvedWeekViewEvent<T>
    ): StaticLayout {
        if (dummyTextLayout == null) {
            val textPaint = config.getTextPaint(event)
            dummyTextLayout = "".toTextLayout(textPaint, width = 0)
        }
        return checkNotNull(dummyTextLayout)
    }

    private fun EventChip<T>.ellipsizeText(
        text: CharSequence,
        availableWidth: Int,
        existingTextLayout: StaticLayout
    ): StaticLayout {
        val textPaint = config.getTextPaint(event)
        val bounds = checkNotNull(bounds)
        val width = bounds.width().roundToInt() - (config.eventPaddingHorizontal * 2)

        val ellipsized = text.ellipsized(textPaint, availableWidth)
        val isTooSmallForText = width < 0
        if (isTooSmallForText) {
            // This day contains too many all-day events. We only draw the event chips,
            // but don't attempt to draw the event titles.
            return existingTextLayout
        }

        return ellipsized.toTextLayout(textPaint, width)
    }

    private val RectF.isValidEventBounds: Boolean
        get() = (left < right &&
            left < view.width &&
            top < view.height &&
            right > config.timeColumnWidth &&
            bottom > 0)
}
