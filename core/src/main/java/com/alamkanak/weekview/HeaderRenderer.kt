package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.util.SparseArray
import androidx.collection.ArrayMap
import java.util.Calendar
import kotlin.math.max
import kotlin.math.roundToInt

internal class HeaderRenderer(
    viewState: ViewState,
    eventChipsCache: EventChipsCache
) : Renderer, DateFormatterDependent {

    private val allDayEventLabels = ArrayMap<EventChip, StaticLayout>()
    private val dateLabelLayouts = SparseArray<StaticLayout>()

    private val headerRowUpdater = HeaderRowUpdater(
        viewState = viewState,
        labelLayouts = dateLabelLayouts
    )

    private val eventsUpdater = AllDayEventsUpdater(
        viewState = viewState,
        eventsLabelLayouts = allDayEventLabels,
        eventChipsCache = eventChipsCache
    )

    private val dateLabelDrawer = DayLabelsDrawer(
        viewState = viewState,
        dateLabelLayouts = dateLabelLayouts
    )

    private val eventsDrawer = AllDayEventsDrawer(
        viewState = viewState,
        allDayEventLayouts = allDayEventLabels
    )

    private val headerRowDrawer = HeaderRowDrawer(
        viewState = viewState
    )

    override fun onSizeChanged(width: Int, height: Int) {
        allDayEventLabels.clear()
        dateLabelLayouts.clear()
    }

    override fun onDateFormatterChanged(formatter: DateFormatter) {
        allDayEventLabels.clear()
        dateLabelLayouts.clear()
    }

    override fun render(canvas: Canvas) {
        if (eventsUpdater.isRequired()) {
            eventsUpdater.update()
        }
        headerRowUpdater.update()

        headerRowDrawer.draw(canvas)
        dateLabelDrawer.draw(canvas)
        eventsDrawer.draw(canvas)
    }
}

private class HeaderRowUpdater(
    private val viewState: ViewState,
    private val labelLayouts: SparseArray<StaticLayout>
) : Updater {

    override fun update() {
        val missingDates = viewState.dateRange.filterNot { labelLayouts.contains(it.toEpochDays()) }
        for (date in missingDates) {
            val key = date.toEpochDays()
            labelLayouts.put(key, calculateStaticLayoutForDate(date))
        }

        val dateLabels = viewState.dateRange.map { labelLayouts[it.toEpochDays()] }
        updateHeaderHeight(dateLabels)
    }

    private fun <E> SparseArray<E>.contains(key: Int): Boolean = indexOfKey(key) >= 0

    private fun updateHeaderHeight(
        dateLabels: List<StaticLayout>
    ) {
        val maximumLayoutHeight = dateLabels.map { it.height.toFloat() }.max() ?: 0f
        viewState.dateLabelHeight = maximumLayoutHeight
        viewState.refreshHeaderHeight()
    }

    private fun calculateStaticLayoutForDate(date: Calendar): StaticLayout {
        val dayLabel = viewState.dateFormatter(date)
        return dayLabel.toTextLayout(
            textPaint = if (date.isToday) viewState.todayHeaderTextPaint else viewState.headerTextPaint,
            width = viewState.totalDayWidth.toInt()
        )
    }

    private operator fun <E> SparseArray<E>.plusAssign(elements: Map<Int, E>) {
        elements.entries.forEach { put(it.key, it.value) }
    }
}

private class DayLabelsDrawer(
    private val viewState: ViewState,
    private val dateLabelLayouts: SparseArray<StaticLayout>
) : Drawer {

    override fun draw(canvas: Canvas) {
        canvas.drawInBounds(
            left = viewState.timeColumnWidth,
            top = 0f,
            right = canvas.width.toFloat(),
            bottom = viewState.headerHeight
        ) {
            viewState.dateRangeWithStartPixels.forEach { (date, startPixel) ->
                drawLabel(date, startPixel)
            }
        }
    }

    private fun Canvas.drawLabel(day: Calendar, startPixel: Float) {
        val key = day.toEpochDays()
        val textLayout = dateLabelLayouts[key]

        withTranslation(
            x = startPixel + viewState.widthPerDay / 2,
            y = viewState.headerRowPadding.toFloat()
        ) {
            textLayout.draw(this)
        }
    }
}

private class AllDayEventsUpdater(
    private val viewState: ViewState,
    private val eventsLabelLayouts: ArrayMap<EventChip, StaticLayout>,
    private val eventChipsCache: EventChipsCache
) : Updater {

    private val boundsCalculator = EventChipBoundsCalculator(viewState)
    private val spannableStringBuilder = SpannableStringBuilder()

    private var previousHorizontalOrigin: Float? = null
    private var dummyTextLayout: StaticLayout? = null

    override fun isRequired(): Boolean {
        val didScrollHorizontally = previousHorizontalOrigin != viewState.currentOrigin.x
        val dateRange = viewState.dateRange
        val containsNewChips = eventChipsCache.allDayEventChipsInDateRange(dateRange).any { it.bounds == null }
        return didScrollHorizontally || containsNewChips
    }

    override fun update() {
        eventsLabelLayouts.clear()

        val datesWithStartPixels = viewState.dateRangeWithStartPixels
        for ((date, startPixel) in datesWithStartPixels) {
            // If we use a horizontal margin in the day view, we need to offset the start pixel.
            val modifiedStartPixel = when {
                viewState.isSingleDay -> startPixel + viewState.eventMarginHorizontal.toFloat()
                else -> startPixel
            }

            val eventChips = eventChipsCache.allDayEventChipsByDate(date)
            for (eventChip in eventChips) {
                val bounds = eventChip.calculateBounds(modifiedStartPixel)
                if (bounds != null) {
                    eventsLabelLayouts[eventChip] = eventChip.calculateTextLayout()
                }
            }
        }

        val maximumChipHeight = eventsLabelLayouts.keys
            .mapNotNull { it.bounds }
            .map { it.height().roundToInt() }
            .max() ?: 0

        viewState.currentAllDayEventHeight = maximumChipHeight
    }

    private fun EventChip.calculateBounds(startPixel: Float): RectF? {
        val chipRect = boundsCalculator.calculateAllDayEvent(this, startPixel)
        bounds = chipRect
        return chipRect.takeIf { it.isValidEventBounds }
    }

    private fun EventChip.calculateTextLayout(): StaticLayout? {
        val event = event
        val bounds = checkNotNull(bounds)

        val fullHorizontalPadding = viewState.eventPaddingHorizontal * 2
        val fullVerticalPadding = viewState.eventPaddingVertical * 2

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

        val location = event.location?.emojify()
        if (location != null) {
            spannableStringBuilder.append(' ')
            spannableStringBuilder.append(location)
        }

        val text = spannableStringBuilder.build()
        val availableWidth = width.toInt()

        val textPaint = viewState.getTextPaint(event)
        val textLayout = text.toTextLayout(textPaint, availableWidth)
        val lineHeight = textLayout.height / textLayout.lineCount

        // For an all day event, we display just one line
        val chipHeight = lineHeight + fullVerticalPadding
        bounds.bottom = bounds.top + chipHeight

        return ellipsizeText(text, availableWidth, existingTextLayout = textLayout)
    }

    /**
     * Creates a dummy text layout that is only used to determine the height of all-day events.
     */
    private fun createDummyTextLayout(
        event: ResolvedWeekViewEvent<*>
    ): StaticLayout {
        if (dummyTextLayout == null) {
            val textPaint = viewState.getTextPaint(event)
            dummyTextLayout = "".toTextLayout(textPaint, width = 0)
        }
        return checkNotNull(dummyTextLayout)
    }

    private fun EventChip.ellipsizeText(
        text: CharSequence,
        availableWidth: Int,
        existingTextLayout: StaticLayout
    ): StaticLayout {
        val textPaint = viewState.getTextPaint(event)
        val bounds = checkNotNull(bounds)
        val width = bounds.width().roundToInt() - (viewState.eventPaddingHorizontal * 2)

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
            left < viewState.viewWidth &&
            top < viewState.viewHeight &&
            right > viewState.timeColumnWidth &&
            bottom > 0)
}

internal class AllDayEventsDrawer(
    private val viewState: ViewState,
    private val allDayEventLayouts: ArrayMap<EventChip, StaticLayout>
) : Drawer {

    private val eventChipDrawer = EventChipDrawer(viewState)

    override fun draw(canvas: Canvas) {
        canvas.drawInBounds(
            left = viewState.timeColumnWidth,
            top = 0f,
            right = canvas.width.toFloat(),
            bottom = viewState.headerHeight
        ) {
            for ((eventChip, textLayout) in allDayEventLayouts) {
                eventChipDrawer.draw(eventChip, canvas, textLayout)
            }
        }
    }
}

private class HeaderRowDrawer(
    private val viewState: ViewState
) : Drawer {

    override fun draw(canvas: Canvas) {
        val width = viewState.viewWidth.toFloat()

        val backgroundPaint = if (viewState.showHeaderRowBottomShadow) {
            viewState.headerBackgroundPaint.withShadow(
                radius = viewState.headerRowBottomShadowRadius,
                color = viewState.headerRowBottomShadowColor
            )
        } else viewState.headerBackgroundPaint

        canvas.drawRect(0f, 0f, width, viewState.headerHeight, backgroundPaint)

        if (viewState.showWeekNumber) {
            canvas.drawWeekNumber(viewState)
        }

        if (viewState.showHeaderRowBottomLine) {
            val y = viewState.headerHeight - viewState.headerRowBottomLineWidth
            canvas.drawLine(0f, y, width, y, viewState.headerRowBottomLinePaint)
        }
    }

    private fun Canvas.drawWeekNumber(state: ViewState) {
        val weekNumber = state.dateRange.first().weekOfYear.toString()

        val bounds = state.weekNumberBounds
        val textPaint = state.weekNumberTextPaint

        val textHeight = textPaint.textHeight
        val textOffset = (textHeight / 2f).roundToInt() - textPaint.descent().roundToInt()

        val width = textPaint.getTextBounds("52").width() * 2.5f
        val height = textHeight * 1.5f

        val backgroundRect = RectF(
            bounds.centerX() - width / 2f,
            bounds.centerY() - height / 2f,
            bounds.centerX() + width / 2f,
            bounds.centerY() + height / 2f
        )

        drawRect(bounds, state.headerBackgroundPaint)

        val backgroundPaint = state.weekNumberBackgroundPaint
        val radius = state.weekNumberBackgroundCornerRadius.toFloat()
        drawRoundRect(backgroundRect, radius, radius, backgroundPaint)

        drawText(weekNumber, bounds.centerX(), bounds.centerY() + textOffset, textPaint)
    }
}

private val Paint.textHeight: Int
    get() = (descent() - ascent()).roundToInt()

private fun Paint.getTextBounds(text: String): Rect {
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)
    return rect
}

private fun Paint.withShadow(radius: Int, color: Int): Paint {
    return Paint(this).apply {
        setShadowLayer(radius.toFloat(), 0f, 0f, color)
    }
}
