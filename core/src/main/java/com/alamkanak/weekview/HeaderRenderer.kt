package com.alamkanak.weekview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.StaticLayout
import android.text.TextPaint
import android.util.SparseArray
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import java.util.Calendar
import kotlin.math.roundToInt

internal class HeaderRenderer(
    context: Context,
    viewState: ViewState,
    eventChipsCache: EventChipsCache,
    onHeaderHeightChanged: () -> Unit
) : Renderer, DateFormatterDependent {

    private val allDayEventLabels = ArrayMap<EventChip, StaticLayout>()
    private val dateLabelLayouts = SparseArray<StaticLayout>()

    private val headerUpdater = HeaderUpdater(
        viewState = viewState,
        labelLayouts = dateLabelLayouts,
        onHeaderHeightChanged = onHeaderHeightChanged
    )

    private val eventsUpdater = AllDayEventsUpdater(
        viewState = viewState,
        eventsLabelLayouts = allDayEventLabels,
        eventChipsCache = eventChipsCache
    )

    private val dateLabelDrawer = DateLabelsDrawer(
        viewState = viewState,
        dateLabelLayouts = dateLabelLayouts
    )

    private val eventsDrawer = AllDayEventsDrawer(
        viewState = viewState,
        allDayEventLayouts = allDayEventLabels
    )

    private val headerDrawer = HeaderDrawer(
        context = context,
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
        eventsUpdater.update()
        headerUpdater.update()

        headerDrawer.draw(canvas)
        dateLabelDrawer.draw(canvas)
        eventsDrawer.draw(canvas)
    }
}

private class HeaderUpdater(
    private val viewState: ViewState,
    private val labelLayouts: SparseArray<StaticLayout>,
    private val onHeaderHeightChanged: () -> Unit
) : Updater {

    private val animator = ValueAnimator()

    override fun update() {
        val missingDates = viewState.dateRange.filterNot { labelLayouts.hasKey(it.toEpochDays()) }
        for (date in missingDates) {
            val key = date.toEpochDays()
            labelLayouts.put(key, calculateStaticLayoutForDate(date))
        }

        val dateLabels = viewState.dateRange.map { labelLayouts[it.toEpochDays()] }
        updateHeaderHeight(dateLabels)
    }

    private fun updateHeaderHeight(
        dateLabels: List<StaticLayout>
    ) {
        val maximumLayoutHeight = dateLabels.map { it.height.toFloat() }.maxOrNull() ?: 0f
        viewState.dateLabelHeight = maximumLayoutHeight

        val currentHeaderHeight = viewState.headerHeight
        val newHeaderHeight = viewState.calculateHeaderHeight()

        if (currentHeaderHeight == 0f || currentHeaderHeight == newHeaderHeight) {
            // The height hasn't been set yet or didn't change; simply update without an animation
            viewState.updateHeaderHeight(newHeaderHeight)
            return
        }

        if (animator.isRunning) {
            // We're already running the animation to change the header height
            return
        }

        animator.animate(
            fromValue = currentHeaderHeight,
            toValue = newHeaderHeight,
            onUpdate = { height ->
                viewState.updateHeaderHeight(height)
                onHeaderHeightChanged()
            }
        )
    }

    private fun calculateStaticLayoutForDate(date: Calendar): StaticLayout {
        val dayLabel = viewState.dateFormatter(date)
        val textPaint = when {
            date.isToday -> viewState.todayHeaderTextPaint
            date.isWeekend -> viewState.weekendHeaderTextPaint
            else -> viewState.headerTextPaint
        }
        return dayLabel.toTextLayout(textPaint = textPaint, width = viewState.dayWidth.toInt())
    }

    private fun <E> SparseArray<E>.hasKey(key: Int): Boolean = indexOfKey(key) >= 0
}

private class DateLabelsDrawer(
    private val viewState: ViewState,
    private val dateLabelLayouts: SparseArray<StaticLayout>
) : Drawer {

    override fun draw(canvas: Canvas) {
        canvas.drawInBounds(viewState.headerBounds) {
            viewState.dateRangeWithStartPixels.forEach { (date, startPixel) ->
                drawLabel(date, startPixel)
            }
        }
    }

    private fun Canvas.drawLabel(day: Calendar, startPixel: Float) {
        val key = day.toEpochDays()
        val textLayout = dateLabelLayouts[key]

        withTranslation(
            x = startPixel + viewState.dayWidth / 2f,
            y = viewState.headerPadding
        ) {
            draw(textLayout)
        }
    }
}

private class AllDayEventsUpdater(
    private val viewState: ViewState,
    private val eventsLabelLayouts: ArrayMap<EventChip, StaticLayout>,
    private val eventChipsCache: EventChipsCache
) : Updater {

    private val boundsCalculator = EventChipBoundsCalculator(viewState)
    private val textFitter = TextFitter(viewState)

    private var previousHorizontalOrigin: Float? = null

    private val isRequired: Boolean
        get() {
            val didScrollHorizontally = previousHorizontalOrigin != viewState.currentOrigin.x
            val dateRange = viewState.dateRange
            val containsNewChips = eventChipsCache.allDayEventChipsInDateRange(dateRange).any { it.bounds.isEmpty }
            return didScrollHorizontally || containsNewChips
        }

    override fun update() {
        if (!isRequired) {
            return
        }

        eventsLabelLayouts.clear()

        val datesWithStartPixels = viewState.dateRangeWithStartPixels
        for ((date, startPixel) in datesWithStartPixels) {
            // If we use a horizontal margin in the day view, we need to offset the start pixel.
            val modifiedStartPixel = when {
                viewState.isSingleDay -> startPixel + viewState.singleDayHorizontalPadding.toFloat()
                else -> startPixel
            }

            val eventChips = eventChipsCache.allDayEventChipsByDate(date)

            eventChips.forEachIndexed { index, eventChip ->
                eventChip.updateBounds(index = index, startPixel = modifiedStartPixel)
                if (eventChip.bounds.isNotEmpty) {
                    eventsLabelLayouts[eventChip] = textFitter.fit(eventChip)
                } else {
                    eventsLabelLayouts.remove(eventChip)
                }
            }
        }

        val maximumChipHeight = eventsLabelLayouts.keys
            .map { it.bounds.height().roundToInt() }
            .maxOrNull() ?: 0

        viewState.currentAllDayEventHeight = maximumChipHeight

        val maximumChipsPerDay = eventsLabelLayouts.keys
            .groupBy { it.event.startTime.toEpochDays() }
            .values
            .maxByOrNull { it.size }?.size ?: 0

        viewState.maxNumberOfAllDayEvents = maximumChipsPerDay
    }

    private fun EventChip.updateBounds(index: Int, startPixel: Float) {
        val candidate = boundsCalculator.calculateAllDayEvent(index, eventChip = this, startPixel)
        bounds = if (candidate.isValid) candidate else RectF()
    }

    private val RectF.isValid: Boolean
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

    private val expandInfoTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(canvas: Canvas) = canvas.drawInBounds(viewState.headerBounds) {
        for (date in viewState.dateRange) {
            val events = allDayEventLayouts
                .filter { it.key.event.startTime.isSameDate(date) }
                .toList()

            if (viewState.arrangeAllDayEventsVertically) {
                renderEventsVertically(events.sortedBy { it.first.bounds.top })
            } else {
                renderEventsHorizontally(events)
            }
        }
    }

    private fun Canvas.renderEventsHorizontally(events: List<Pair<EventChip, StaticLayout>>) {
        for ((eventChip, textLayout) in events) {
            eventChipDrawer.draw(eventChip, canvas = this, textLayout)
        }
    }

    private fun Canvas.renderEventsVertically(events: List<Pair<EventChip, StaticLayout>>) {
        // Un-hide all events. To prevent any click handler from mapping a click to a hidden event,
        // we set isHidden to true for all events that aren't shown in the collapsed state.
        events.forEach { it.first.isHidden = false }

        if (viewState.allDayEventsExpanded || events.size <= 2) {
            // Draw them all!
            for ((eventChip, textLayout) in events) {
                eventChipDrawer.draw(eventChip, canvas = this, textLayout)
            }
        } else {
            val (firstEventChip, firstTextLayout) = events[0]
            eventChipDrawer.draw(firstEventChip, canvas = this, firstTextLayout)

            val needsExpandInfo = events.size >= 2
            if (needsExpandInfo) {
                drawExpandInfo(eventsCount = events.size - 1, priorEventChip = firstEventChip)
                events.drop(1).forEach { it.first.isHidden = true }
            } else {
                val (secondEventChip, secondTextLayout) = events[1]
                eventChipDrawer.draw(secondEventChip, canvas = this, secondTextLayout)
                events.drop(2).forEach { it.first.isHidden = true }
            }
        }
    }

    private fun Canvas.drawExpandInfo(eventsCount: Int, priorEventChip: EventChip) {
        // Draw +X text
        val text = "+$eventsCount"
        val textPaint = expandInfoTextPaint.apply {
            textSize = viewState.allDayEventTextPaint.textSize
            color = viewState.headerTextPaint.color
        }

        val x = priorEventChip.bounds.left + viewState.eventPaddingHorizontal.toFloat()
        val y = priorEventChip.bounds.bottom +
            viewState.eventMarginVertical +
            viewState.eventPaddingVertical +
            textPaint.textSize

        drawText(text, x, y, textPaint)
    }
}

private class HeaderDrawer(
    context: Context,
    private val viewState: ViewState
) : Drawer {

    private val upArrow: Drawable by lazy {
        checkNotNull(ContextCompat.getDrawable(context, R.drawable.ic_arrow_up))
    }

    private val downArrow: Drawable by lazy {
        checkNotNull(ContextCompat.getDrawable(context, R.drawable.ic_arrow_down))
    }

    override fun draw(canvas: Canvas) {
        val width = viewState.viewWidth.toFloat()

        val backgroundPaint = if (viewState.showHeaderBottomShadow) {
            viewState.headerBackgroundWithShadowPaint
        } else {
            viewState.headerBackgroundPaint
        }

        canvas.drawRect(0f, 0f, width, viewState.headerHeight, backgroundPaint)

        if (viewState.showWeekNumber) {
            canvas.drawWeekNumber()
        }

        if (viewState.showAllDayEventsToggleArrow) {
            canvas.drawAllDayEventsToggleArrow()
        }

        if (viewState.showHeaderBottomLine) {
            val y = viewState.headerHeight - viewState.headerBottomLinePaint.strokeWidth
            canvas.drawLine(0f, y, width, y, viewState.headerBottomLinePaint)
        }
    }

    private fun Canvas.drawWeekNumber() {
        val weekNumber = viewState.dateRange.first().weekOfYear.toString()

        val bounds = viewState.weekNumberBounds
        val textPaint = viewState.weekNumberTextPaint

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

        drawRect(bounds, viewState.headerBackgroundPaint)

        val backgroundPaint = viewState.weekNumberBackgroundPaint
        val radius = viewState.weekNumberBackgroundCornerRadius
        drawRoundRect(backgroundRect, radius, radius, backgroundPaint)

        drawText(weekNumber, bounds.centerX(), bounds.centerY() + textOffset, textPaint)
    }

    private fun Canvas.drawAllDayEventsToggleArrow() = with(viewState) {
        val bottom = (headerHeight - headerPadding).roundToInt()
        val top = bottom - currentAllDayEventHeight

        val width = weekNumberBounds.width().roundToInt()
        val height = bottom - top

        val left = (width - height) / 2
        val right = (left + height)

        if (allDayEventsExpanded) {
            upArrow.setBounds(left, top, right, bottom)
            upArrow.draw(this@drawAllDayEventsToggleArrow)
        } else {
            downArrow.setBounds(left, top, right, bottom)
            downArrow.draw(this@drawAllDayEventsToggleArrow)
        }
    }
}

private val Paint.textHeight: Int
    get() = (descent() - ascent()).roundToInt()

private fun Paint.getTextBounds(text: String): Rect {
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)
    return rect
}
