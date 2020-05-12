package com.alamkanak.weekview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.core.view.ViewCompat
import com.alamkanak.weekview.Constants.UNINITIALIZED
import java.util.Calendar
import kotlin.math.min
import kotlin.math.roundToInt

class WeekView<T : Any> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), WeekViewViewState.Listener {

    private val configWrapper: WeekViewConfigWrapper by lazy {
        val config = WeekViewConfig(context, attrs)
        WeekViewConfigWrapper(this, config)
    }

    private val cache = WeekViewCache<T>()
    private val eventChipCache = EventChipCache<T>()

    private val viewState = WeekViewViewState(configWrapper, this)
    private val drawingContext = DrawingContext(configWrapper)

    private val touchHandler = WeekViewTouchHandler(configWrapper, eventChipCache)
    private val gestureHandler = WeekViewGestureHandler(
        view = this,
        config = configWrapper,
        viewState = viewState,
        chipCache = eventChipCache,
        touchHandler = touchHandler,
        onInvalidation = { ViewCompat.postInvalidateOnAnimation(this) }
    )

    private var accessibilityTouchHelper = WeekViewAccessibilityTouchHelper(
        view = this,
        config = configWrapper,
        drawingContext = drawingContext,
        gestureHandler = gestureHandler,
        touchHandler = touchHandler,
        eventChipCache = eventChipCache
    )

    private val eventChipsLoader = EventChipsLoader(configWrapper, eventChipCache)
    private val eventChipsExpander = EventChipsExpander(configWrapper, eventChipCache)

    internal val eventsCacheWrapper = EventsCacheWrapper<T>()
    internal val eventsLoaderWrapper = EventsLoaderWrapper(eventsCacheWrapper)

    private val eventsDiffer = EventsDiffer(eventsCacheWrapper, eventChipsLoader, drawingContext)

    private val eventsLoader: EventsLoader<T>
        get() = eventsLoaderWrapper.get()

    // Be careful when changing the order of the updaters, as the calculation of any updater might
    // depend on results of previous updaters
    private val updaters = listOf(
        MultiLineDayLabelHeightUpdater(configWrapper, cache),
        AllDayEventsUpdater(this, configWrapper, cache, eventChipCache),
        HeaderRowHeightUpdater(configWrapper, eventsCacheWrapper),
        SingleEventsUpdater(this, configWrapper, eventChipCache)
    )

    private var isAccessibilityHelperActive = false

    init {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val isAccessibilityEnabled = accessibilityManager.isEnabled
        val isExploreByTouchEnabled = accessibilityManager.isTouchExplorationEnabled

        isAccessibilityHelperActive = isAccessibilityEnabled && isExploreByTouchEnabled
        if (isAccessibilityHelperActive) {
            ViewCompat.setAccessibilityDelegate(this, accessibilityTouchHelper)
        }

        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    // Be careful when changing the order of the drawers, as that might cause
    // views to incorrectly draw over each other
    private val drawers = listOf(
        DayBackgroundDrawer(this, configWrapper),
        BackgroundGridDrawer(this, configWrapper),
        SingleEventsDrawer(context, configWrapper, eventChipCache),
        TimeColumnDrawer(this, configWrapper),
        HeaderRowDrawer(this, configWrapper),
        DayLabelDrawer(configWrapper, cache),
        AllDayEventsDrawer(context, configWrapper, cache),
        NowLineDrawer(configWrapper)
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
        if (isInEditMode) {
            return
        }

        // These can either be newly loaded events or previously cached events
        val events = eventsLoader.refresh(viewState.firstVisibleDate)
        eventChipCache.clear()

        if (events.isNotEmpty()) {
            eventChipsLoader.createAndCacheEventChips(events)
            eventChipsExpander.calculateEventChipPositions()
        }
    }

    private fun updateDimensions() {
        for (updater in updaters) {
            if (updater.isRequired(drawingContext)) {
                updater.update(drawingContext)
            }
        }
    }

    private fun performDrawing(canvas: Canvas) {
        for (drawer in drawers) {
            drawer.draw(drawingContext, canvas)
        }

        if (isAccessibilityHelperActive) {
            accessibilityTouchHelper.invalidateRoot()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return superState?.let {
            SavedState(it, configWrapper.numberOfVisibleDays, viewState.firstVisibleDate)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        if (configWrapper.restoreNumberOfVisibleDays) {
            configWrapper.numberOfVisibleDays = savedState.numberOfVisibleDays
        }

        goToDate(savedState.firstVisibleDate)
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
        val oldFirstVisibleDay = viewState.firstVisibleDate
        val totalDayWidth = configWrapper.totalDayWidth
        val visibleDays = configWrapper.numberOfVisibleDays

        val daysScrolled = configWrapper.currentOrigin.x / totalDayWidth
        val delta = daysScrolled.roundToInt() * (-1)

        val firstVisibleDate = today() + Days(delta)
        viewState.firstVisibleDate = firstVisibleDate

        val hasFirstVisibleDayChanged = oldFirstVisibleDay.toEpochDays() != firstVisibleDate.toEpochDays()
        if (hasFirstVisibleDayChanged) {
            scrollListener?.onFirstVisibleDateChanged(firstVisibleDate)

            val lastVisibleDate = firstVisibleDate + Days(visibleDays - 1)
            onRangeChangeListener?.onRangeChanged(firstVisibleDate, lastVisibleDate)
        }
    }

    private fun calculateWidthPerDay() {
        if (configWrapper.timeColumnWidth == UNINITIALIZED) {
            configWrapper.calculateTimeColumnWidth()
        }

        configWrapper.calculateWidthPerDay()
    }

    override fun invalidate() {
        super.invalidate()
        viewState.invalidate()
    }

    /*
     ***********************************************************************************************
     *
     *   Calendar configuration
     *
     ***********************************************************************************************
     */

    /**
     * Returns the first day of the week. Possible values are [java.util.Calendar.SUNDAY],
     * [java.util.Calendar.MONDAY], [java.util.Calendar.TUESDAY],
     * [java.util.Calendar.WEDNESDAY], [java.util.Calendar.THURSDAY],
     * [java.util.Calendar.FRIDAY], [java.util.Calendar.SATURDAY].
     */
    @PublicApi
    var firstDayOfWeek: Int
        get() = configWrapper.firstDayOfWeek
        set(value) {
            configWrapper.firstDayOfWeek = value
            invalidate()
        }

    /**
     * Returns the number of visible days.
     */
    @PublicApi
    var numberOfVisibleDays: Int
        get() = configWrapper.numberOfVisibleDays
        set(value) {
            configWrapper.numberOfVisibleDays = value
            dateTimeInterpreter.onSetNumberOfDays(value)
            clearCaches()

            // Scroll to first visible day after changing the number of visible days
            viewState.scrollToDate = viewState.firstVisibleDate

            calculateWidthPerDay()
            invalidate()
        }

    /**
     * Returns whether the first day of the week should be displayed at the left-most position
     * when WeekView is displayed for the first time.
     */
    @PublicApi
    var isShowFirstDayOfWeekFirst: Boolean
        get() = configWrapper.showFirstDayOfWeekFirst
        set(value) {
            configWrapper.showFirstDayOfWeekFirst = value
        }

    /*
     ***********************************************************************************************
     *
     *   Header bottom line
     *
     ***********************************************************************************************
     */

    @PublicApi
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

    @PublicApi
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

    @PublicApi
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

    /*
     ***********************************************************************************************
     *
     *   Time column
     *
     ***********************************************************************************************
     */

    /**
     * Returns the padding in the time column to the left and right side of the time label.
     */
    @PublicApi
    var timeColumnPadding: Int
        get() = configWrapper.timeColumnPadding
        set(value) {
            configWrapper.timeColumnPadding = value
            invalidate()
        }

    /**
     * Returns the text color of the labels in the time column.
     */
    @PublicApi
    var timeColumnTextColor: Int
        get() = configWrapper.timeColumnTextColor
        set(value) {
            configWrapper.timeColumnTextColor = value
            invalidate()
        }

    /**
     * Returns the background color of the time column.
     */
    @PublicApi
    var timeColumnBackgroundColor: Int
        get() = configWrapper.timeColumnBackgroundColor
        set(value) {
            configWrapper.timeColumnBackgroundColor = value
            invalidate()
        }

    /**
     * Returns the text size of the labels in the time column.
     */
    @PublicApi
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
    @PublicApi
    var isShowMidnightHour: Boolean
        get() = configWrapper.showMidnightHour
        set(value) {
            configWrapper.showMidnightHour = value
            invalidate()
        }

    /**
     * Returns whether a horizontal line is displayed for each hour in the time column.
     */
    @PublicApi
    var isShowTimeColumnHourSeparator: Boolean
        get() = configWrapper.showTimeColumnHourSeparator
        set(value) {
            configWrapper.showTimeColumnHourSeparator = value
            invalidate()
        }

    /**
     * Returns the interval in which time labels are displayed in the time column.
     */
    @PublicApi
    var timeColumnHoursInterval: Int
        get() = configWrapper.timeColumnHoursInterval
        set(value) {
            configWrapper.timeColumnHoursInterval = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Time column separator
     *
     ***********************************************************************************************
     */

    /**
     * Returns whether a vertical line is displayed at the end of the time column.
     */
    @PublicApi
    var isShowTimeColumnSeparator: Boolean
        get() = configWrapper.showTimeColumnSeparator
        set(value) {
            configWrapper.showTimeColumnSeparator = value
            invalidate()
        }

    /**
     * Returns the color of the time column separator.
     */
    @PublicApi
    var timeColumnSeparatorColor: Int
        get() = configWrapper.timeColumnSeparatorColor
        set(value) {
            configWrapper.timeColumnSeparatorColor = value
            invalidate()
        }

    /**
     * Returns the stroke width of the time column separator.
     */
    @PublicApi
    var timeColumnSeparatorWidth: Int
        get() = configWrapper.timeColumnSeparatorStrokeWidth
        set(value) {
            configWrapper.timeColumnSeparatorStrokeWidth = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Header row
     *
     ***********************************************************************************************
     */

    /**
     * Returns the header row padding, which is applied above and below the all-day event chips.
     */
    @PublicApi
    var headerRowPadding: Int
        get() = configWrapper.headerRowPadding
        set(value) {
            configWrapper.headerRowPadding = value
            invalidate()
        }

    /**
     * Returns the header row background color.
     */
    @PublicApi
    var headerRowBackgroundColor: Int
        get() = configWrapper.headerRowBackgroundColor
        set(value) {
            configWrapper.headerRowBackgroundColor = value
            invalidate()
        }

    /**
     * Returns the text color used for all date labels except today.
     */
    @PublicApi
    var headerRowTextColor: Int
        get() = configWrapper.headerRowTextColor
        set(value) {
            configWrapper.headerRowTextColor = value
            invalidate()
        }

    /**
     * Returns the text color used for today's date label.
     */
    @PublicApi
    var todayHeaderTextColor: Int
        get() = configWrapper.todayHeaderTextColor
        set(value) {
            configWrapper.todayHeaderTextColor = value
            invalidate()
        }

    /**
     * Returns the text size of all date labels.
     */
    @PublicApi
    var headerRowTextSize: Int
        get() = configWrapper.headerRowTextSize
        set(value) {
            configWrapper.headerRowTextSize = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Event chips
     *
     ***********************************************************************************************
     */

    /**
     * Returns the corner radius of an [EventChip].
     */
    @PublicApi
    var eventCornerRadius: Int
        get() = configWrapper.eventCornerRadius
        set(value) {
            configWrapper.eventCornerRadius = value
            invalidate()
        }

    /**
     * Returns the text size of a single-event [EventChip].
     */
    @PublicApi
    var eventTextSize: Int
        get() = configWrapper.eventTextPaint.textSize.toInt()
        set(value) {
            configWrapper.eventTextPaint.textSize = value.toFloat()
            invalidate()
        }

    /**
     * Returns whether the text size of the [EventChip] is adapting to the [EventChip] height.
     */
    @PublicApi
    var isAdaptiveEventTextSize: Boolean
        get() = configWrapper.adaptiveEventTextSize
        set(value) {
            configWrapper.adaptiveEventTextSize = value
            invalidate()
        }

    /**
     * Returns the text size of an all-day [EventChip].
     */
    @PublicApi
    var allDayEventTextSize: Int
        get() = configWrapper.allDayEventTextPaint.textSize.toInt()
        set(value) {
            configWrapper.allDayEventTextPaint.textSize = value.toFloat()
            invalidate()
        }

    /**
     * Returns the default text color of an [EventChip].
     */
    @PublicApi
    var defaultEventTextColor: Int
        get() = configWrapper.eventTextPaint.color
        set(value) {
            configWrapper.eventTextPaint.color = value
            invalidate()
        }

    /**
     * Returns the horizontal padding within an [EventChip].
     */
    @PublicApi
    var eventPaddingHorizontal: Int
        get() = configWrapper.eventPaddingHorizontal
        set(value) {
            configWrapper.eventPaddingHorizontal = value
            invalidate()
        }

    /**
     * Returns the vertical padding within an [EventChip].
     */
    @PublicApi
    var eventPaddingVertical: Int
        get() = configWrapper.eventPaddingVertical
        set(value) {
            configWrapper.eventPaddingVertical = value
            invalidate()
        }

    /**
     * Returns the default text color of an [EventChip].
     */
    @PublicApi
    var defaultEventColor: Int
        get() = configWrapper.defaultEventColor
        set(value) {
            configWrapper.defaultEventColor = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Event margins
     *
     ***********************************************************************************************
     */

    /**
     * Returns the column gap at the end of each day.
     */
    @PublicApi
    var columnGap: Int
        get() = configWrapper.columnGap
        set(value) {
            configWrapper.columnGap = value
            invalidate()
        }

    /**
     * Returns the horizontal gap between overlapping [EventChip]s.
     */
    @PublicApi
    var overlappingEventGap: Int
        get() = configWrapper.overlappingEventGap
        set(value) {
            configWrapper.overlappingEventGap = value
            invalidate()
        }

    /**
     * Returns the vertical margin of an [EventChip].
     */
    @PublicApi
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
    @PublicApi
    var eventMarginHorizontal: Int
        get() = configWrapper.eventMarginHorizontal
        set(value) {
            configWrapper.eventMarginHorizontal = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Colors
     *
     ***********************************************************************************************
     */

    /**
     * Returns the background color of a day.
     */
    @PublicApi
    var dayBackgroundColor: Int
        get() = configWrapper.dayBackgroundPaint.color
        set(value) {
            configWrapper.dayBackgroundPaint.color = value
            invalidate()
        }

    /**
     * Returns the background color of the current date.
     */
    @PublicApi
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
    @PublicApi
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
    @PublicApi
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
    @PublicApi
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
    @PublicApi
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
    @PublicApi
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
    @PublicApi
    var futureWeekendBackgroundColor: Int
        get() = configWrapper.futureWeekendBackgroundPaint.color
        set(value) {
            configWrapper.futureWeekendBackgroundPaint.color = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Hour height
     *
     ***********************************************************************************************
     */

    /**
     * Returns the current height of an hour.
     */
    @PublicApi
    var hourHeight: Float
        get() = configWrapper.hourHeight
        set(value) {
            configWrapper.newHourHeight = value
            invalidate()
        }

    /**
     * Returns the minimum height of an hour.
     */
    @PublicApi
    var minHourHeight: Int
        get() = configWrapper.minHourHeight
        set(value) {
            configWrapper.minHourHeight = value
            invalidate()
        }

    /**
     * Returns the maximum height of an hour.
     */
    @PublicApi
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
    @PublicApi
    var isShowCompleteDay: Boolean
        get() = configWrapper.showCompleteDay
        set(value) {
            configWrapper.showCompleteDay = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Now line
     *
     ***********************************************************************************************
     */

    /**
     * Returns whether a horizontal line should be displayed at the current time.
     */
    @PublicApi
    var isShowNowLine: Boolean
        get() = configWrapper.showNowLine
        set(value) {
            configWrapper.showNowLine = value
            invalidate()
        }

    /**
     * Returns the color of the horizontal "now" line.
     */
    @PublicApi
    var nowLineColor: Int
        get() = configWrapper.nowLinePaint.color
        set(value) {
            configWrapper.nowLinePaint.color = value
            invalidate()
        }

    /**
     * Returns the stroke width of the horizontal "now" line.
     */
    @PublicApi
    var nowLineStrokeWidth: Int
        get() = configWrapper.nowLinePaint.strokeWidth.toInt()
        set(value) {
            configWrapper.nowLinePaint.strokeWidth = value.toFloat()
            invalidate()
        }

    /**
     * Returns whether a dot at the start of the "now" line is displayed. The dot is only displayed
     * if [isShowNowLine] is set to true.
     */
    @PublicApi
    var isShowNowLineDot: Boolean
        get() = configWrapper.showNowLineDot
        set(value) {
            configWrapper.showNowLineDot = value
            invalidate()
        }

    /**
     * Returns the color of the dot at the start of the "now" line.
     */
    @PublicApi
    var nowLineDotColor: Int
        get() = configWrapper.nowDotPaint.color
        set(value) {
            configWrapper.nowDotPaint.color = value
            invalidate()
        }

    /**
     * Returns the radius of the dot at the start of the "now" line.
     */
    @PublicApi
    var nowLineDotRadius: Int
        get() = configWrapper.nowDotPaint.strokeWidth.toInt()
        set(value) {
            configWrapper.nowDotPaint.strokeWidth = value.toFloat()
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Hour separators
     *
     ***********************************************************************************************
     */

    @PublicApi
    var isShowHourSeparators: Boolean
        get() = configWrapper.showHourSeparators
        set(value) {
            configWrapper.showHourSeparators = value
            invalidate()
        }

    @PublicApi
    var hourSeparatorColor: Int
        get() = configWrapper.hourSeparatorPaint.color
        set(value) {
            configWrapper.hourSeparatorPaint.color = value
            invalidate()
        }

    @PublicApi
    var hourSeparatorStrokeWidth: Int
        get() = configWrapper.hourSeparatorPaint.strokeWidth.toInt()
        set(value) {
            configWrapper.hourSeparatorPaint.strokeWidth = value.toFloat()
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Day separators
     *
     ***********************************************************************************************
     */

    /**
     * Returns whether vertical lines are displayed as separators between dates.
     */
    @PublicApi
    var isShowDaySeparators: Boolean
        get() = configWrapper.showDaySeparators
        set(value) {
            configWrapper.showDaySeparators = value
            invalidate()
        }

    /**
     * Returns the color of the separators between dates.
     */
    @PublicApi
    var daySeparatorColor: Int
        get() = configWrapper.daySeparatorPaint.color
        set(value) {
            configWrapper.daySeparatorPaint.color = value
            invalidate()
        }

    /**
     * Returns the stroke color of the separators between dates.
     */
    @PublicApi
    var daySeparatorStrokeWidth: Int
        get() = configWrapper.daySeparatorPaint.strokeWidth.toInt()
        set(value) {
            configWrapper.daySeparatorPaint.strokeWidth = value.toFloat()
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Date range
     *
     ***********************************************************************************************
     */

    /**
     * Returns the minimum date that [WeekView] will display, or null if none is set. Events before
     * this date will not be shown.
     */
    @PublicApi
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
    @PublicApi
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

    /*
     ***********************************************************************************************
     *
     *   Time range
     *
     ***********************************************************************************************
     */

    /**
     * Returns the minimum hour that [WeekView] will display. Events before this time will not be
     * shown.
     */
    @PublicApi
    var minHour: Int
        get() = configWrapper.minHour
        set(value) {
            if (value < 0 || value > configWrapper.maxHour) {
                throw IllegalArgumentException("minHour must be between 0 and maxHour.")
            }

            configWrapper.minHour = value
            invalidate()
        }

    /**
     * Returns the maximum hour that [WeekView] will display. Events before this time will not be
     * shown.
     */
    @PublicApi
    var maxHour: Int
        get() = configWrapper.maxHour
        set(value) {
            if (value > 24 || value < configWrapper.minHour) {
                throw IllegalArgumentException("maxHour must be between minHour and 24.")
            }

            configWrapper.maxHour = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Scrolling
     *
     ***********************************************************************************************
     */

    /**
     * Returns the scrolling speed factor in horizontal direction.
     */
    @PublicApi
    var xScrollingSpeed: Float
        get() = configWrapper.xScrollingSpeed
        set(value) {
            configWrapper.xScrollingSpeed = value
        }

    /**
     * Returns whether WeekView can fling horizontally.
     */
    @PublicApi
    var isHorizontalFlingEnabled: Boolean
        get() = configWrapper.horizontalFlingEnabled
        set(value) {
            configWrapper.horizontalFlingEnabled = value
        }

    /**
     * Returns whether WeekView can scroll horizontally.
     */
    @PublicApi
    var isHorizontalScrollingEnabled: Boolean
        get() = configWrapper.horizontalScrollingEnabled
        set(value) {
            configWrapper.horizontalScrollingEnabled = value
        }

    /**
     * Returns whether WeekView can fling vertically.
     */
    @PublicApi
    var isVerticalFlingEnabled: Boolean
        get() = configWrapper.verticalFlingEnabled
        set(value) {
            configWrapper.verticalFlingEnabled = value
        }

    @PublicApi
    var scrollDuration: Int
        get() = configWrapper.scrollDuration
        set(value) {
            configWrapper.scrollDuration = value
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = gestureHandler.onTouchEvent(event)

    /*
     ***********************************************************************************************
     *
     *   Date methods
     *
     ***********************************************************************************************
     */

    /**
     * Returns the first visible date.
     */
    @PublicApi
    val firstVisibleDate: Calendar
        get() = viewState.firstVisibleDate.copy()

    /**
     * Returns the last visible date.
     */
    @PublicApi
    val lastVisibleDate: Calendar
        get() = viewState.firstVisibleDate.copy() + Days(configWrapper.numberOfVisibleDays - 1)

    /**
     * Shows the current date.
     */
    @PublicApi
    fun goToToday() {
        goToDate(today())
    }

    /**
     * Shows the current date and time.
     */
    @PublicApi
    fun goToCurrentTime() {
        now().apply {
            goToDate(this)
            goToHour(hour)
        }
    }

    /**
     * Shows a specific date. If it is before [minDate] or after [maxDate], these will be shown
     * instead.
     *
     * @param date The date to show.
     */
    @PublicApi
    override fun goToDate(date: Calendar) {
        val adjustedDate = configWrapper.getDateWithinDateRange(date)
        gestureHandler.forceScrollFinished()

        val isWaitingToBeLaidOut = ViewCompat.isLaidOut(this).not()
        if (viewState.areDimensionsInvalid || isWaitingToBeLaidOut) {
            // If the view's dimensions have just changed or if it hasn't been laid out yet, we
            // postpone the action until onDraw() is called the next time.
            viewState.scrollToDate = adjustedDate
            return
        }

        eventsLoader.requireRefresh()

        val diff = adjustedDate.daysFromToday
        configWrapper.currentOrigin.x = diff.toFloat() * (-1f) * configWrapper.totalDayWidth
        invalidate()
    }

    /**
     * Refreshes the view and loads the events again.
     */
    @PublicApi
    fun notifyDataSetChanged() {
        eventsLoader.requireRefresh()
        invalidate()
    }

    /**
     * Scrolls to a specific hour.
     *
     * @param hour The hour to scroll to, in 24-hour format. Supported values are 0-24.
     *
     * @throws IllegalArgumentException Throws exception if the provided hour is smaller than
     *                                   [minHour] or larger than [maxHour].
     */
    @PublicApi
    override fun goToHour(hour: Int) {
        if (viewState.areDimensionsInvalid) {
            viewState.scrollToHour = hour
            return
        }

        if (hour !in configWrapper.minHour..configWrapper.maxHour) {
            throw IllegalArgumentException(
                "The provided hour ($hour) is outside of the set time range " +
                    "(${configWrapper.minHour} – ${configWrapper.maxHour})"
            )
        }

        val hourHeight = configWrapper.hourHeight
        val desiredOffset = hourHeight * (hour - configWrapper.minHour)

        // We make sure that WeekView doesn't "over-scroll" by limiting the offset to the total day
        // height minus the height of WeekView, which would result in scrolling all the way to the
        // bottom.
        val maxOffset = configWrapper.totalDayHeight - height
        val finalOffset = min(maxOffset, desiredOffset)

        configWrapper.currentOrigin.y = finalOffset * (-1)
        invalidate()
    }

    /**
     * Returns the first hour that is visible on the screen.
     */
    @PublicApi
    val firstVisibleHour: Double
        get() = (configWrapper.currentOrigin.y * -1 / configWrapper.hourHeight).toDouble()

    /*
     ***********************************************************************************************
     *
     *   Typeface
     *
     ***********************************************************************************************
     */

    /**
     * Returns the typeface used for events, time labels and date labels.
     */
    @PublicApi
    var typeface: Typeface
        get() = configWrapper.typeface
        set(value) {
            configWrapper.typeface = value
            invalidate()
        }

    /*
     ***********************************************************************************************
     *
     *   Listeners
     *
     ***********************************************************************************************
     */

    @PublicApi
    var onEventClickListener: OnEventClickListener<T>?
        get() = touchHandler.onEventClickListener
        set(value) {
            touchHandler.onEventClickListener = value
        }

    @PublicApi
    fun setOnEventClickListener(
        block: (data: T, rect: RectF) -> Unit
    ) {
        onEventClickListener = object : OnEventClickListener<T> {
            override fun onEventClick(data: T, eventRect: RectF) {
                block(data, eventRect)
            }
        }
    }

    @PublicApi
    var onMonthChangeListener: OnMonthChangeListener<T>?
        get() = (eventsLoader as? LegacyEventsLoader)?.onMonthChangeListener
        set(value) {
            eventsCacheWrapper.onListenerChanged(value)
            eventsLoaderWrapper.onListenerChanged(value)
        }

    @PublicApi
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

    /**
     * Submits a list of [WeekViewDisplayable]s to [WeekView]. If the new events fall into the
     * currently displayed date range, this method will also redraw [WeekView].
     */
    @PublicApi
    fun submit(items: List<WeekViewDisplayable<T>>) {
        eventsDiffer.submit(items) { shouldInvalidate ->
            if (shouldInvalidate) {
                invalidate()
            }
        }
    }

    @PublicApi
    var onLoadMoreListener: OnLoadMoreListener?
        get() = (eventsLoader as? PagedEventsLoader)?.onLoadMoreListener
        set(value) {
            eventsCacheWrapper.onListenerChanged(value)
            eventsLoaderWrapper.onListenerChanged(value)
        }

    /**
     * Registers a block that is called whenever [WeekView] needs to load more events. This is
     * similar to an [OnMonthChangeListener], but does not require anything to be returned.
     */
    @PublicApi
    fun setOnLoadMoreListener(
        block: (startDate: Calendar, endDate: Calendar) -> Unit
    ) {
        onLoadMoreListener = object : OnLoadMoreListener {
            override fun onLoadMore(startDate: Calendar, endDate: Calendar) {
                block(startDate, endDate)
            }
        }
    }

    @PublicApi
    var onEventLongClickListener: OnEventLongClickListener<T>?
        get() = touchHandler.onEventLongClickListener
        set(value) {
            touchHandler.onEventLongClickListener = value
        }

    @PublicApi
    fun setOnEventLongClickListener(
        block: (data: T, rect: RectF) -> Unit
    ) {
        onEventLongClickListener = object : OnEventLongClickListener<T> {
            override fun onEventLongClick(data: T, eventRect: RectF) {
                block(data, eventRect)
            }
        }
    }

    @PublicApi
    var onEmptyViewClickListener: OnEmptyViewClickListener?
        get() = touchHandler.onEmptyViewClickListener
        set(value) {
            touchHandler.onEmptyViewClickListener = value
        }

    @PublicApi
    fun setOnEmptyViewClickListener(
        block: (time: Calendar) -> Unit
    ) {
        onEmptyViewClickListener = object : OnEmptyViewClickListener {
            override fun onEmptyViewClicked(time: Calendar) {
                block(time)
            }
        }
    }

    @PublicApi
    var onEmptyViewLongClickListener: OnEmptyViewLongClickListener?
        get() = touchHandler.onEmptyViewLongClickListener
        set(value) {
            touchHandler.onEmptyViewLongClickListener = value
        }

    @PublicApi
    fun setOnEmptyViewLongClickListener(
        block: (time: Calendar) -> Unit
    ) {
        onEmptyViewLongClickListener = object : OnEmptyViewLongClickListener {
            override fun onEmptyViewLongClick(time: Calendar) {
                block(time)
            }
        }
    }

    @PublicApi
    var scrollListener: ScrollListener?
        get() = gestureHandler.scrollListener
        set(value) {
            gestureHandler.scrollListener = value
        }

    @PublicApi
    fun setScrollListener(
        block: (date: Calendar) -> Unit
    ) {
        scrollListener = object : ScrollListener {
            override fun onFirstVisibleDateChanged(date: Calendar) {
                block(firstVisibleDate)
            }
        }
    }

    @PublicApi
    var onRangeChangeListener: OnRangeChangeListener? = null

    @PublicApi
    fun setOnRangeChangeListener(
        block: (firstVisibleDate: Calendar, lastVisibleDate: Calendar) -> Unit
    ) {
        onRangeChangeListener = object : OnRangeChangeListener {
            override fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) {
                block(firstVisibleDate, lastVisibleDate)
            }
        }
    }

    @PublicApi
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

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (accessibilityTouchHelper.dispatchHoverEvent(event)) {
            return true
        }
        return super.dispatchHoverEvent(event)
    }
}
