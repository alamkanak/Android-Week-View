package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.StaticLayout
import android.util.SparseArray
import androidx.collection.ArrayMap
import java.util.Calendar
import kotlin.math.roundToInt

internal class HeaderRenderer(
    viewState: ViewState,
    eventChipsCache: EventChipsCache,
    onHeaderHeightChanged: () -> Unit
) : Renderer, DateFormatterDependent {

    private val allDayEventLabels = ArrayMap<EventChip, StaticLayout>()
    private val dateLabelLayouts = SparseArray<StaticLayout>()

    private val headerRowUpdater = HeaderRowUpdater(
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
        eventsUpdater.update()
        headerRowUpdater.update()

        headerRowDrawer.draw(canvas)
        dateLabelDrawer.draw(canvas)
        eventsDrawer.draw(canvas)
    }
}

private class HeaderRowUpdater(
    private val viewState: ViewState,
    private val labelLayouts: SparseArray<StaticLayout>,
    private val onHeaderHeightChanged: () -> Unit
) : Updater {

    override fun update() {
        val missingDates = viewState.dateRange.filterNot { labelLayouts.hasKey(it.toEpochDays()) }
        for (date in missingDates) {
            val key = date.toEpochDays()
            labelLayouts.put(key, calculateStaticLayoutForDate(date))
        }

        val dateLabels = viewState.dateRange.map { labelLayouts[it.toEpochDays()] }
        updateHeaderHeight(dateLabels)
    }

    private fun <E> SparseArray<E>.hasKey(key: Int): Boolean = indexOfKey(key) >= 0

    private fun updateHeaderHeight(
        dateLabels: List<StaticLayout>
    ) {
        val maximumLayoutHeight = dateLabels.map { it.height.toFloat() }.maxOrNull() ?: 0f
        viewState.dateLabelHeight = maximumLayoutHeight

        val currentHeaderHeight = viewState.headerHeight
        viewState.refreshHeaderHeight()
        val newHeaderHeight = viewState.headerHeight

        if (currentHeaderHeight != newHeaderHeight) {
            onHeaderHeightChanged()
        }
    }

    private fun calculateStaticLayoutForDate(date: Calendar): StaticLayout {
        val dayLabel = viewState.dateFormatter(date)
        return dayLabel.toTextLayout(
            textPaint = if (date.isToday) viewState.todayHeaderTextPaint else viewState.headerRowTextPaint,
            width = viewState.dayWidth.toInt()
        )
    }

    private operator fun <E> SparseArray<E>.plusAssign(elements: Map<Int, E>) {
        elements.entries.forEach { put(it.key, it.value) }
    }
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
            y = viewState.headerRowPadding
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
            for (eventChip in eventChips) {
                eventChip.updateBounds(startPixel = modifiedStartPixel)
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
    }

    private fun EventChip.updateBounds(startPixel: Float) {
        val candidate = boundsCalculator.calculateAllDayEvent(this, startPixel)
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

    override fun draw(canvas: Canvas) {
        canvas.drawInBounds(viewState.headerBounds) {
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
            viewState.headerRowBackgroundWithShadowPaint
        } else {
            viewState.headerRowBackgroundPaint
        }

        canvas.drawRect(0f, 0f, width, viewState.headerHeight, backgroundPaint)

        if (viewState.showWeekNumber) {
            canvas.drawWeekNumber(viewState)
        }

        if (viewState.showHeaderRowBottomLine) {
            val y = viewState.headerHeight - viewState.headerRowBottomLinePaint.strokeWidth
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

        drawRect(bounds, state.headerRowBackgroundPaint)

        val backgroundPaint = state.weekNumberBackgroundPaint
        val radius = state.weekNumberBackgroundCornerRadius
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
