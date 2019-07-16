package com.alamkanak.weekview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import com.alamkanak.weekview.Constants.UNINITIALIZED
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

class WeekView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), WeekViewGestureHandler.Listener, WeekViewViewState.Listener {

    private val configWrapper: WeekViewConfigWrapper by lazy {
        val config = WeekViewConfig(context, attrs)
        WeekViewConfigWrapper(this, config)
    }

    private val eventCache: EventCache<T> by lazy {
        val eventSplitter = WeekViewEventSplitter<T>(configWrapper)
        EventCache(eventSplitter)
    }

    private val cache = WeekViewCache(eventCache)

    private val viewState = WeekViewViewState(configWrapper, this)
    private val gestureHandler = WeekViewGestureHandler(this, configWrapper, eventCache)

    private val drawingContext = DrawingContext(configWrapper)
    private val eventChipsProvider = EventChipsProvider(configWrapper, eventCache, viewState)

    private val paint = Paint()

    // Be careful when changing the order of the updaters, as the calculation of any updater might
    // depend on results of previous updaters
    private val updaters = listOf(
        MultiLineDayLabelHeightUpdater(configWrapper, cache),
        HeaderRowHeightUpdater(configWrapper, eventCache),
        AllDayEventsUpdater(this, configWrapper, cache),
        SingleEventsUpdater(this, configWrapper, cache)
    )

    // Be careful when changing the order of the drawers, as that might cause
    // views to incorrectly draw over each other
    private val drawers = listOf(
        DayBackgroundDrawer(this, configWrapper),
        BackgroundGridDrawer(this, configWrapper),
        SingleEventsDrawer(context, configWrapper, cache),
        NowLineDrawer(configWrapper),
        HeaderRowDrawer(this, configWrapper),
        DayLabelDrawer(configWrapper, cache),
        AllDayEventsDrawer(context, configWrapper, cache),
        TimeColumnDrawer(this, configWrapper)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateDataHolders()
        notifyScrollListeners()
        refreshEvents()
        updateDimensions()
        performDrawing(canvas)
    }

    private fun updateDataHolders() {
        viewState.update(height)
        configWrapper.update()
        drawingContext.update()
    }

    private fun refreshEvents() {
        if (!isInEditMode) {
            eventChipsProvider.loadEventsIfNecessary()
        }
    }

    private fun updateDimensions() {
        updaters
            .filter { it.isRequired() }
            .forEach { it.update(drawingContext) }
    }

    private fun performDrawing(canvas: Canvas) {
        drawers.forEach { it.draw(drawingContext, canvas, paint) }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return superState?.let {
            SavedState(it, configWrapper.numberOfVisibleDays, viewState.firstVisibleDay)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        if (configWrapper.restoreNumberOfVisibleDays) {
            configWrapper.numberOfVisibleDays = savedState.numberOfVisibleDays
        }

        savedState.firstVisibleDate?.let {
            goToDate(it)
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        viewState.areDimensionsInvalid = true

        clearCaches()
        calculateWidthPerDay()

        if (configWrapper.showCompleteDay) {
            configWrapper.updateHourHeight(height)
        }
    }

    private fun notifyScrollListeners() {
        val oldFirstVisibleDay = viewState.firstVisibleDay
        val totalDayWidth = configWrapper.totalDayWidth
        val visibleDays = configWrapper.numberOfVisibleDays

        val isBeforeCurrentTime = configWrapper.currentOrigin.x > 0
        val delta = if (isBeforeCurrentTime) {
            ceil(configWrapper.currentOrigin.x / totalDayWidth).roundToInt() * (-1)
        } else {
            floor(configWrapper.currentOrigin.x / totalDayWidth).roundToInt() * (-1)
        }

        val firstVisibleDay = today().plusDays(delta)
        val lastVisibleDay = firstVisibleDay.plusDays(visibleDays - 1)

        viewState.firstVisibleDay = firstVisibleDay
        viewState.lastVisibleDay = lastVisibleDay

        val hasFirstVisibleDayChanged = oldFirstVisibleDay?.let {
            firstVisibleDay.isSameDate(it).not()
        } ?: true

        if (hasFirstVisibleDayChanged) {
            scrollListener?.onFirstVisibleDateChanged(firstVisibleDay)
            onRangeChangeListener?.onRangeChanged(firstVisibleDay, lastVisibleDay)
        }
    }

    private fun calculateWidthPerDay() {
        if (configWrapper.timeColumnWidth == UNINITIALIZED) {
            configWrapper.calculateTimeColumnWidth()
        }

        configWrapper.calculateWidthPerDay()
    }

    override fun onScaled() {
        invalidate()
    }

    override fun onScrolled() {
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun invalidate() {
        super.invalidate()
        viewState.invalidate()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Calendar configuration
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the first day of the week. Possible values are [java.util.Calendar.SUNDAY],
     * [java.util.Calendar.MONDAY], [java.util.Calendar.TUESDAY],
     * [java.util.Calendar.WEDNESDAY], [java.util.Calendar.THURSDAY],
     * [java.util.Calendar.FRIDAY], [java.util.Calendar.SATURDAY].
     */
    var firstDayOfWeek: Int
        get() = configWrapper.firstDayOfWeek
        set(value) {
            configWrapper.firstDayOfWeek = value
            invalidate()
        }

    /**
     * Returns the number of visible days.
     */
    var numberOfVisibleDays: Int
        get() = configWrapper.numberOfVisibleDays
        set(value) {
            configWrapper.numberOfVisibleDays = value
            dateTimeInterpreter.onSetNumberOfDays(value)
            clearCaches()

            viewState.firstVisibleDay?.let {
                // Scroll to first visible day after changing the number of visible days
                viewState.scrollToDay = it
            }

            calculateWidthPerDay()
            invalidate()
        }

    /**
     * Returns whether the first day of the week should be displayed at the left-most position
     * when WeekView is displayed for the first time.
     */
    var isShowFirstDayOfWeekFirst: Boolean
        get() = configWrapper.showFirstDayOfWeekFirst
        set(value) {
            configWrapper.showFirstDayOfWeekFirst = value
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Header bottom line
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated(
        "Use isShowHeaderRowBottomLine",
        ReplaceWith("isShowHeaderRowBottomLine")
    )
    fun getShowHeaderRowBottomLine() = isShowHeaderRowBottomLine

    var isShowHeaderRowBottomLine: Boolean
        /**
         * Returns whether a horizontal line should be displayed at the bottom of the header row.
         */
        get() = configWrapper.showHeaderRowBottomLine
        /**
         * Sets whether a horizontal line should be displayed at the bottom of the header row.
         */
        set(value) {
            configWrapper.showHeaderRowBottomLine = value
            invalidate()
        }

    var headerRowBottomLineColor: Int
        /**
         * Returns the color of the horizontal line at the bottom of the header row.
         */
        get() = configWrapper.headerRowBottomLinePaint.color
        /**
         * Sets the color of the horizontal line at the bottom of the header row. Whether the line
         * is displayed, is determined by [isShowHeaderRowBottomLine]
         */
        set(value) {
            configWrapper.headerRowBottomLinePaint.color = value
            invalidate()
        }

    var headerRowBottomLineWidth: Int
        /**
         * Returns the stroke width of the horizontal line at the bottom of the header row.
         */
        get() = configWrapper.headerRowBottomLinePaint.strokeWidth.toInt()
        /**
         * Sets the stroke width of the horizontal line at the bottom of the header row. Whether the
         * line is displayed, is determined by [isShowHeaderRowBottomLine]
         */
        set(value) {
            configWrapper.headerRowBottomLinePaint.strokeWidth = value.toFloat()
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the padding in the time column to the left and right side of the time label.
     */
    var timeColumnPadding: Int
        get() = configWrapper.timeColumnPadding
        set(value) {
            configWrapper.timeColumnPadding = value
            invalidate()
        }

    /**
     * Returns the text color of the labels in the time column.
     */
    var timeColumnTextColor: Int
        get() = configWrapper.timeColumnTextColor
        set(value) {
            configWrapper.timeColumnTextColor = value
            invalidate()
        }

    /**
     * Returns the background color of the time column.
     */
    var timeColumnBackgroundColor: Int
        get() = configWrapper.timeColumnBackgroundColor
        set(value) {
            configWrapper.timeColumnBackgroundColor = value
            invalidate()
        }

    /**
     * Returns the text size of the labels in the time column.
     */
    var timeColumnTextSize: Int
        get() = configWrapper.timeColumnTextSize
        set(value) {
            configWrapper.timeColumnTextSize = value
            invalidate()
        }

    /**
     * Returns whether the label for the midnight hour is displayed in the time column. This setting
     * is only considered if [isShowTimeColumnHourSeparator] is set to true.
     */
    var isShowMidnightHour: Boolean
        get() = configWrapper.showMidnightHour
        set(value) {
            configWrapper.showMidnightHour = value
            invalidate()
        }

    @Deprecated(
        "Use isShowTimeColumnHourSeparator",
        ReplaceWith("isShowTimeColumnHourSeparator")
    )
    fun showTimeColumnHourSeparator() = isShowTimeColumnHourSeparator

    /**
     * Returns whether a horizontal line is displayed for each hour in the time column.
     */
    var isShowTimeColumnHourSeparator: Boolean
        get() = configWrapper.showTimeColumnHourSeparator
        set(value) {
            configWrapper.showTimeColumnHourSeparator = value
            invalidate()
        }

    /**
     * Returns the interval in which time labels are displayed in the time column.
     */
    var timeColumnHoursInterval: Int
        get() = configWrapper.timeColumnHoursInterval
        set(value) {
            configWrapper.timeColumnHoursInterval = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column separator
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated(
        "Use isShowTimeColumnSeparator",
        ReplaceWith("isShowTimeColumnSeparator")
    )
    fun getShowTimeColumnSeparator() = isShowTimeColumnSeparator

    /**
     * Returns whether a vertical line is displayed at the end of the time column.
     */
    var isShowTimeColumnSeparator: Boolean
        get() = configWrapper.showTimeColumnSeparator
        set(value) {
            configWrapper.showTimeColumnSeparator = value
            invalidate()
        }

    /**
     * Returns the color of the time column separator.
     */
    var timeColumnSeparatorColor: Int
        get() = configWrapper.timeColumnSeparatorColor
        set(value) {
            configWrapper.timeColumnSeparatorColor = value
            invalidate()
        }

    /**
     * Returns the stroke width of the time column separator.
     */
    var timeColumnSeparatorWidth: Int
        get() = configWrapper.timeColumnSeparatorStrokeWidth
        set(value) {
            configWrapper.timeColumnSeparatorStrokeWidth = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Header row
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the header row padding, which is applied above and below the all-day event chips.
     */
    var headerRowPadding: Int
        get() = configWrapper.headerRowPadding
        set(value) {
            configWrapper.headerRowPadding = value
            invalidate()
        }

    /**
     * Returns the header row background color.
     */
    var headerRowBackgroundColor: Int
        get() = configWrapper.headerRowBackgroundColor
        set(value) {
            configWrapper.headerRowBackgroundColor = value
            invalidate()
        }

    /**
     * Returns the text color used for all date labels except today.
     */
    var headerRowTextColor: Int
        get() = configWrapper.headerRowTextColor
        set(value) {
            configWrapper.headerRowTextColor = value
            invalidate()
        }

    /**
     * Returns the text color used for today's date label.
     */
    var todayHeaderTextColor: Int
        get() = configWrapper.todayHeaderTextColor
        set(value) {
            configWrapper.todayHeaderTextColor = value
            invalidate()
        }

    /**
     * Returns the text size of all date labels.
     */
    var headerRowTextSize: Int
        get() = configWrapper.headerRowTextSize
        set(value) {
            configWrapper.headerRowTextSize = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Event chips
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the corner radius of an [EventChip].
     */
    var eventCornerRadius: Int
        get() = configWrapper.eventCornerRadius
        set(value) {
            configWrapper.eventCornerRadius = value
            invalidate()
        }

    /**
     * Returns the text size of a single-event [EventChip].
     */
    var eventTextSize: Int
        get() = configWrapper.eventTextPaint.textSize.toInt()
        set(value) {
            configWrapper.eventTextPaint.textSize = value.toFloat()
            invalidate()
        }

    /**
     * Returns whether the text size of the [EventChip] is adapting to the [EventChip] height.
     */
    var isAdaptiveEventTextSize: Boolean
        get() = configWrapper.adaptiveEventTextSize
        set(value) {
            configWrapper.adaptiveEventTextSize = value
            invalidate()
        }

    /**
     * Returns the text size of an all-day [EventChip].
     */
    var allDayEventTextSize: Int
        get() = configWrapper.allDayEventTextPaint.textSize.toInt()
        set(value) {
            configWrapper.allDayEventTextPaint.textSize = value.toFloat()
            invalidate()
        }

    @Deprecated(
        "Use defaultEventTextColor",
        ReplaceWith("defaultEventTextColor")
    )
    var eventTextColor: Int
        get() = defaultEventTextColor
        set(value) {
            defaultEventTextColor = value
        }

    /**
     * Returns the default text color of an [EventChip].
     */
    var defaultEventTextColor: Int
        get() = configWrapper.eventTextPaint.color
        set(value) {
            configWrapper.eventTextPaint.color = value
            invalidate()
        }

    /**
     * Returns the padding within an [EventChip].
     */
    var eventPadding: Int
        get() = configWrapper.eventPadding
        set(value) {
            configWrapper.eventPadding = value
            invalidate()
        }

    /**
     * Returns the default text color of an [EventChip].
     */
    var defaultEventColor: Int
        get() = configWrapper.defaultEventColor
        set(value) {
            configWrapper.defaultEventColor = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Event margins
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the column gap at the end of each day.
     */
    var columnGap: Int
        get() = configWrapper.columnGap
        set(value) {
            configWrapper.columnGap = value
            invalidate()
        }

    /**
     * Returns the horizontal gap between overlapping [EventChip]s.
     */
    var overlappingEventGap: Int
        get() = configWrapper.overlappingEventGap
        set(value) {
            configWrapper.overlappingEventGap = value
            invalidate()
        }

    /**
     * Returns the vertical margin of an [EventChip].
     */
    var eventMarginVertical: Int
        get() = configWrapper.eventMarginVertical
        set(value) {
            configWrapper.eventMarginVertical = value
            invalidate()
        }

    /**
     * Returns the horizontal margin of an [EventChip]. This margin is only applied in single-day
     * view and if there are no overlapping events.
     */
    var eventMarginHorizontal: Int
        get() = configWrapper.eventMarginHorizontal
        set(value) {
            configWrapper.eventMarginHorizontal = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Colors
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the background color of a day.
     */
    var dayBackgroundColor: Int
        get() = configWrapper.dayBackgroundPaint.color
        set(value) {
            configWrapper.dayBackgroundPaint.color = value
            invalidate()
        }

    /**
     * Returns the background color of the current date.
     */
    var todayBackgroundColor: Int
        get() = configWrapper.todayBackgroundPaint.color
        set(value) {
            configWrapper.todayBackgroundPaint.color = value
            invalidate()
        }

    /**
     * Returns whether weekends should have a background color different from [dayBackgroundColor].
     *
     * The weekend background colors can be defined by [pastWeekendBackgroundColor] and
     * [futureWeekendBackgroundColor].
     */
    var isShowDistinctWeekendColor: Boolean
        get() = configWrapper.showDistinctWeekendColor
        set(value) {
            configWrapper.showDistinctWeekendColor = value
            invalidate()
        }

    /**
     * Returns whether past and future days should have background colors different from
     * [dayBackgroundColor].
     *
     * The past and future day colors can be defined by [pastBackgroundColor] and
     * [futureBackgroundColor].
     */
    var isShowDistinctPastFutureColor: Boolean
        get() = configWrapper.showDistinctPastFutureColor
        set(value) {
            configWrapper.showDistinctPastFutureColor = value
            invalidate()
        }

    /**
     * Returns the background color for past dates. If not explicitly set, WeekView will used
     * [dayBackgroundColor].
     */
    var pastBackgroundColor: Int
        get() = configWrapper.pastBackgroundPaint.color
        set(value) {
            configWrapper.pastBackgroundPaint.color = value
            invalidate()
        }

    /**
     * Returns the background color for past weekend dates. If not explicitly set, WeekView will
     * used [pastBackgroundColor].
     */
    var pastWeekendBackgroundColor: Int
        get() = configWrapper.pastWeekendBackgroundPaint.color
        set(value) {
            configWrapper.pastWeekendBackgroundPaint.color = value
            invalidate()
        }

    /**
     * Returns the background color for future dates. If not explicitly set, WeekView will used
     * [dayBackgroundColor].
     */
    var futureBackgroundColor: Int
        get() = configWrapper.futureBackgroundPaint.color
        set(value) {
            configWrapper.futureBackgroundPaint.color = value
            invalidate()
        }

    /**
     * Returns the background color for future weekend dates. If not explicitly set, WeekView will
     * used [futureBackgroundColor].
     */
    var futureWeekendBackgroundColor: Int
        get() = configWrapper.futureWeekendBackgroundPaint.color
        set(value) {
            configWrapper.futureWeekendBackgroundPaint.color = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour height
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the current height of an hour.
     */
    var hourHeight: Float
        get() = configWrapper.hourHeight
        set(value) {
            configWrapper.newHourHeight = value
            invalidate()
        }

    /**
     * Returns the minimum height of an hour.
     */
    var minHourHeight: Int
        get() = configWrapper.minHourHeight
        set(value) {
            configWrapper.minHourHeight = value
            invalidate()
        }

    /**
     * Returns the maximum height of an hour.
     */
    var maxHourHeight: Int
        get() = configWrapper.maxHourHeight
        set(value) {
            configWrapper.maxHourHeight = value
            invalidate()
        }

    /**
     * Returns whether the complete day should be shown, in which case [hourHeight] automatically
     * adjusts to accommodate all hours between [minHour] and [maxHour].
     */
    var isShowCompleteDay: Boolean
        get() = configWrapper.showCompleteDay
        set(value) {
            configWrapper.showCompleteDay = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Now line
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns whether a horizontal line should be displayed at the current time.
     */
    var isShowNowLine: Boolean
        get() = configWrapper.showNowLine
        set(value) {
            configWrapper.showNowLine = value
            invalidate()
        }

    /**
     * Returns the color of the horizontal "now" line.
     */
    var nowLineColor: Int
        get() = configWrapper.nowLinePaint.color
        set(value) {
            configWrapper.nowLinePaint.color = value
            invalidate()
        }

    /**
     * Returns the stroke width of the horizontal "now" line.
     */
    var nowLineStrokeWidth: Int
        get() = configWrapper.nowLinePaint.strokeWidth.toInt()
        set(value) {
            configWrapper.nowLinePaint.strokeWidth = value.toFloat()
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Now line dot
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns whether a dot at the start of the "now" line is displayed. The dot is only displayed
     * if [isShowNowLine] is set to true.
     */
    var isShowNowLineDot: Boolean
        get() = configWrapper.showNowLineDot
        set(value) {
            configWrapper.showNowLineDot = value
            invalidate()
        }

    /**
     * Returns the color of the dot at the start of the "now" line.
     */
    var nowLineDotColor: Int
        get() = configWrapper.nowDotPaint.color
        set(value) {
            configWrapper.nowDotPaint.color = value
            invalidate()
        }

    /**
     * Returns the radius of the dot at the start of the "now" line.
     */
    var nowLineDotRadius: Int
        get() = configWrapper.nowDotPaint.strokeWidth.toInt()
        set(value) {
            configWrapper.nowDotPaint.strokeWidth = value.toFloat()
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated(
        "Use isShowHourSeparators",
        ReplaceWith("isShowHourSeparators")
    )
    fun showHourSeparators() = isShowHourSeparators

    var isShowHourSeparators: Boolean
        get() = configWrapper.showHourSeparators
        set(value) {
            configWrapper.showHourSeparators = value
            invalidate()
        }

    var hourSeparatorColor: Int
        get() = configWrapper.hourSeparatorPaint.color
        set(value) {
            configWrapper.hourSeparatorPaint.color = value
            invalidate()
        }

    var hourSeparatorStrokeWidth: Int
        get() = configWrapper.hourSeparatorPaint.strokeWidth.toInt()
        set(value) {
            configWrapper.hourSeparatorPaint.strokeWidth = value.toFloat()
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Day separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated(
        "Use isShowDaySeparators",
        ReplaceWith("isShowDaySeparators")
    )
    fun showDaySeparators() = isShowDaySeparators

    /**
     * Returns whether vertical lines are displayed as separators between dates.
     */
    var isShowDaySeparators: Boolean
        get() = configWrapper.showDaySeparators
        set(value) {
            configWrapper.showDaySeparators = value
            invalidate()
        }

    /**
     * Returns the color of the separators between dates.
     */
    var daySeparatorColor: Int
        get() = configWrapper.daySeparatorPaint.color
        set(value) {
            configWrapper.daySeparatorPaint.color = value
            invalidate()
        }

    /**
     * Returns the stroke color of the separators between dates.
     */
    var daySeparatorStrokeWidth: Int
        get() = configWrapper.daySeparatorPaint.strokeWidth.toInt()
        set(value) {
            configWrapper.daySeparatorPaint.strokeWidth = value.toFloat()
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Date range
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the minimum date that [WeekView] will display, or null if none is set. Events before
     * this date will not be shown.
     */
    var minDate: Calendar?
        get() = configWrapper.minDate?.copy()
        set(value) {
            val maxDate = configWrapper.maxDate
            if (maxDate != null && value != null && value.isAfter(maxDate)) {
                throw IllegalArgumentException("Can't set a minDate that's after maxDate")
            }

            configWrapper.minDate = value
            invalidate()
        }

    /**
     * Returns the maximum date that [WeekView] will display, or null if none is set. Events after
     * this date will not be shown.
     */
    var maxDate: Calendar?
        get() = configWrapper.maxDate?.copy()
        set(value) {
            val minDate = configWrapper.minDate
            if (minDate != null && value != null && value.isBefore(minDate)) {
                throw IllegalArgumentException("Can't set a maxDate that's before minDate")
            }

            configWrapper.maxDate = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time range
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the minimum hour that [WeekView] will display. Events before this time will not be
     * shown.
     */
    var minHour: Int
        get() = configWrapper.minHour
        set(value) {
            if (value < 0 || value > configWrapper.maxHour) {
                throw IllegalArgumentException("minHour must be larger than 0 and smaller than maxHour.")
            }

            configWrapper.minHour = value
            invalidate()
        }

    /**
     * Returns the maximum hour that [WeekView] will display. Events before this time will not be
     * shown.
     */
    var maxHour: Int
        get() = configWrapper.maxHour
        set(value) {
            if (value > 24 || value < configWrapper.minHour) {
                throw IllegalArgumentException("maxHour must be smaller than 24 and larger than minHour.")
            }

            configWrapper.maxHour = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Scrolling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the scrolling speed factor in horizontal direction.
     */
    var xScrollingSpeed: Float
        get() = configWrapper.xScrollingSpeed
        set(value) {
            configWrapper.xScrollingSpeed = value
        }

    /**
     * Returns whether WeekView can fling horizontally.
     */
    var isHorizontalFlingEnabled: Boolean
        get() = configWrapper.horizontalFlingEnabled
        set(value) {
            configWrapper.horizontalFlingEnabled = value
        }

    /**
     * Returns whether WeekView can scroll horizontally.
     */
    var isHorizontalScrollingEnabled: Boolean
        get() = configWrapper.horizontalScrollingEnabled
        set(value) {
            configWrapper.horizontalScrollingEnabled = value
        }

    /**
     * Returns whether WeekView can fling vertically.
     */
    var isVerticalFlingEnabled: Boolean
        get() = configWrapper.verticalFlingEnabled
        set(value) {
            configWrapper.verticalFlingEnabled = value
        }

    var scrollDuration: Int
        get() = configWrapper.scrollDuration
        set(value) {
            configWrapper.scrollDuration = value
        }

    /////////////////////////////////////////////////////////////////
    //
    //  Functions related to scrolling
    //
    /////////////////////////////////////////////////////////////////

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = gestureHandler.onTouchEvent(event)

    override fun computeScroll() {
        super.computeScroll()
        gestureHandler.computeScroll()
    }

    /////////////////////////////////////////////////////////////////
    //
    //  Public methods
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Returns the first visible day.
     */
    val firstVisibleDay: Calendar?
        get() = viewState.firstVisibleDay?.copy()

    /**
     * Returns the last visible day.
     */
    val lastVisibleDay: Calendar?
        get() = viewState.lastVisibleDay?.copy()

    /**
     * Shows the current date.
     */
    fun goToToday() {
        goToDate(today())
    }

    /**
     * Shows the current date and time.
     */
    fun goToCurrentTime() {
        now().apply {
            goToDate(this)
            goToHour(hour)
        }
    }

    /**
     * Shows a specific day. If it is before [minDate] or after [maxDate], these will be shown
     * instead.
     *
     * @param date The date to show.
     */
    override fun goToDate(date: Calendar) {
        val minDate = configWrapper.minDate
        val maxDate = configWrapper.maxDate

        val numberOfVisibleDays = configWrapper.numberOfVisibleDays
        val showFirstDayOfWeekFirst = configWrapper.showFirstDayOfWeekFirst

        // If a minimum or maximum date is set, don't allow to go beyond them.
        val modifiedDate = if (minDate != null && date.isBefore(minDate)) {
            minDate
        } else if (maxDate != null && date.isAfter(maxDate)) {
            maxDate.plusDays(1 - numberOfVisibleDays)
        } else if (numberOfVisibleDays >= 7 && showFirstDayOfWeekFirst) {
            val diff = configWrapper.computeDifferenceWithFirstDayOfWeek(date)
            date.minusDays(diff)
        } else {
            date
        }

        gestureHandler.forceScrollFinished()

        if (viewState.areDimensionsInvalid) {
            viewState.scrollToDay = modifiedDate
            return
        }

        viewState.shouldRefreshEvents = true

        val diff = modifiedDate.daysFromToday
        configWrapper.currentOrigin.x = diff.toFloat() * (-1f) * configWrapper.totalDayWidth
        invalidate()
    }

    /**
     * Refreshes the view and loads the events again.
     */
    fun notifyDataSetChanged() {
        viewState.shouldRefreshEvents = true
        invalidate()
    }

    /**
     * Scrolls to a specific hour.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    override fun goToHour(hour: Int) {
        if (viewState.areDimensionsInvalid) {
            viewState.scrollToHour = hour
            return
        }

        val modifiedHour = min(hour, configWrapper.hoursPerDay)
        var verticalOffset = configWrapper.hourHeight * modifiedHour

        val dayHeight = configWrapper.totalDayHeight
        val viewHeight = height.toDouble()

        val desiredOffset = dayHeight - viewHeight
        verticalOffset = min(desiredOffset.toFloat(), verticalOffset)

        configWrapper.currentOrigin.y = -verticalOffset
        invalidate()
    }

    /**
     * Returns the first hour that is visible on the screen.
     */
    val firstVisibleHour: Double
        get() = (configWrapper.currentOrigin.y * -1 / configWrapper.hourHeight).toDouble()

    /////////////////////////////////////////////////////////////////
    //
    //  Fonts
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Returns the typeface used for events, time labels and date labels.
     */
    var typeface: Typeface
        get() = configWrapper.typeface
        set(value) {
            configWrapper.typeface = value
            invalidate()
        }

    /////////////////////////////////////////////////////////////////
    //
    //  Listeners
    //
    /////////////////////////////////////////////////////////////////

    var onEventClickListener: OnEventClickListener<T>?
        get() = gestureHandler.onEventClickListener
        set(value) {
            gestureHandler.onEventClickListener = value
        }

    fun setOnEventClickListener(
        block: (data: T, rect: RectF) -> Unit
    ) {
        onEventClickListener = object : OnEventClickListener<T> {
            override fun onEventClick(data: T, eventRect: RectF) {
                block(data, eventRect)
            }
        }
    }

    @Deprecated(
        "Use onMonthChangeListener",
        ReplaceWith("onMonthChangeListener")
    )
    fun getMonthChangeListener() = onMonthChangeListener

    var onMonthChangeListener: OnMonthChangeListener<T>?
        get() {
            return eventChipsProvider.monthLoader?.onMonthChangeListener
        }
        set(value) {
            eventChipsProvider.monthLoader = MonthLoader(value)
        }

    @Deprecated(
        "Use onMonthChangeListener",
        ReplaceWith("onMonthChangeListener")
    )
    fun setMonthChangeListener(listener: OnMonthChangeListener<T>) {
        onMonthChangeListener = listener
    }

    fun setOnMonthChangeListener(
        block: (startDate: Calendar, endDate: Calendar) -> List<WeekViewDisplayable<T>>
    ) {
        onMonthChangeListener = object : OnMonthChangeListener<T> {
            override fun onMonthChange(
                startDate: Calendar,
                endDate: Calendar
            ): List<WeekViewDisplayable<T>> {
                return block(startDate, endDate)
            }
        }
    }

    @Deprecated(
        "Use onEventLongPressListener",
        ReplaceWith("onEventLongPressListener")
    )
    val eventLongPressListener = onEventLongPressListener

    @Deprecated(
        "Use onEventLongPressListener",
        ReplaceWith("onEventLongPressListener")
    )
    fun setEventLongPressListener(listener: EventLongPressListener<T>) {
        onEventLongPressListener = listener
    }

    var onEventLongPressListener: OnEventLongPressListener<T>?
        get() = gestureHandler.onEventLongPressListener
        set(value) {
            gestureHandler.onEventLongPressListener = value
        }

    fun setOnEventLongPressListener(
        block: (data: T, rect: RectF) -> Unit
    ) {
        onEventLongPressListener = object : OnEventLongPressListener<T> {
            override fun onEventLongPress(data: T, eventRect: RectF) {
                block(data, eventRect)
            }
        }
    }

    @Deprecated(
        "Use onEmptyViewClickListener",
        ReplaceWith("onEmptyViewClickListener")
    )
    val emptyViewClickListener = onEmptyViewClickListener

    var onEmptyViewClickListener: OnEmptyViewClickListener?
        get() = gestureHandler.onEmptyViewClickListener
        set(value) {
            gestureHandler.onEmptyViewClickListener = value
        }

    @Deprecated(
        "Use onEmptyViewClickListener",
        ReplaceWith("onEmptyViewClickListener")
    )
    fun setEmptyViewClickListener(listener: EmptyViewClickListener) {
        onEmptyViewClickListener = listener
    }

    fun setOnEmptyViewClickListener(
        block: (time: Calendar) -> Unit
    ) {
        onEmptyViewClickListener = object : OnEmptyViewClickListener {
            override fun onEmptyViewClicked(time: Calendar) {
                block(time)
            }
        }
    }

    @Deprecated(
        "Use onEmptyViewLongPressListener",
        ReplaceWith("onEmptyViewLongPressListener")
    )
    val emptyViewLongPressListener = onEmptyViewLongPressListener

    var onEmptyViewLongPressListener: OnEmptyViewLongPressListener?
        get() = gestureHandler.onEmptyViewLongPressListener
        set(value) {
            gestureHandler.onEmptyViewLongPressListener = value
        }

    @Deprecated(
        "Use onEmptyViewLongPressListener",
        ReplaceWith("onEmptyViewLongPressListener")
    )
    fun setEmptyViewLongPressListener(listener: EmptyViewLongPressListener) {
        onEmptyViewLongPressListener = listener
    }

    fun setOnEmptyViewLongPressListener(
        block: (time: Calendar) -> Unit
    ) {
        onEmptyViewLongPressListener = object : OnEmptyViewLongPressListener {
            override fun onEmptyViewLongPress(time: Calendar) {
                block(time)
            }
        }
    }

    var scrollListener: ScrollListener?
        get() = gestureHandler.scrollListener
        set(value) {
            gestureHandler.scrollListener = value
        }

    fun setScrollListener(
        block: (date: Calendar) -> Unit
    ) {
        scrollListener = object : ScrollListener {
            override fun onFirstVisibleDateChanged(date: Calendar) {
                block(checkNotNull(firstVisibleDay))
            }
        }
    }

    @Deprecated(
        "Use setScrollListener(block: (Calendar) -> Unit)"
    )
    fun setScrollListener(
        block: (newFirstVisibleDay: Calendar?, oldFirstVisibleDay: Calendar?) -> Unit
    ) {
        scrollListener = object : ScrollListener {
            override fun onFirstVisibleDateChanged(date: Calendar) {
                block(firstVisibleDay, null)
            }
        }
    }

    var onRangeChangeListener: OnRangeChangeListener? = null

    fun setOnRangeChangeListener(
        block: (firstVisibleDate: Calendar, lastVisibleDate: Calendar) -> Unit
    ) {
        onRangeChangeListener = object : OnRangeChangeListener {
            override fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) {
                block(checkNotNull(firstVisibleDay), checkNotNull(lastVisibleDay))
            }
        }
    }

    var dateTimeInterpreter: DateTimeInterpreter
        get() = configWrapper.dateTimeInterpreter
        set(value) {
            configWrapper.dateTimeInterpreter = value
            clearCaches()
        }

    private fun clearCaches() {
        drawers
            .filterIsInstance(CachingDrawer::class.java)
            .forEach { it.clear() }
    }

}
