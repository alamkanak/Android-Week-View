package com.alamkanak.weekview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcelable
import android.text.StaticLayout
import android.util.AttributeSet
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import com.alamkanak.weekview.Constants.UNINITIALIZED
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.round

class WeekView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), WeekViewGestureHandler.Listener, WeekViewViewState.Listener {

    private val configWrapper: WeekViewConfigWrapper by lazy {
        val config = WeekViewConfig(context, attrs)
        WeekViewConfigWrapper(this, config)
    }

    private val cache: WeekViewCache<T> by lazy {
        val eventSplitter = WeekViewEventSplitter<T>(configWrapper)
        WeekViewCache(eventSplitter)
    }

    private val viewState = WeekViewViewState(configWrapper, this)
    private val gestureHandler = WeekViewGestureHandler(this, configWrapper, cache)

    private val drawingContext = DrawingContext()
    private val eventChipsProvider = EventChipsProvider(configWrapper, cache, viewState)

    private val headerRowDrawer = HeaderRowDrawer(this, configWrapper, cache, viewState)
    private val dayLabelDrawer = DayLabelDrawer(this, configWrapper)
    private val eventsDrawer = EventsDrawer(this, configWrapper, cache)
    private val timeColumnDrawer = TimeColumnDrawer(this, configWrapper)
    private val dayBackgroundDrawer = DayBackgroundDrawer(this, configWrapper)
    private val backgroundGridDrawer = BackgroundGridDrawer(this, configWrapper)
    private val nowLineDrawer = NowLineDrawer(configWrapper)

    private val paint = Paint()
    private val allDayEvents = mutableListOf<Pair<EventChip<T>, StaticLayout>>()

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            numberOfVisibleDays = configWrapper.numberOfVisibleDays
            firstVisibleDate = viewState.firstVisibleDay
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
        dayLabelDrawer.clearLabelCache()

        if (configWrapper.showCompleteDay) {
            configWrapper.updateHourHeight(height)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val isFirstDraw = viewState.isFirstDraw

        calculateWidthPerDay()
        viewState.update(this)

        configWrapper.refreshAfterZooming(this)
        configWrapper.updateVerticalOrigin(this)

        notifyScrollListeners()
        prepareEventDrawing(canvas)

        if (viewState.isFirstDraw) {
            configWrapper.moveCurrentOriginIfFirstDraw()
            viewState.isFirstDraw = false
        }

        drawingContext.update(configWrapper)
        if (!isInEditMode) {
            eventChipsProvider.loadEventsIfNecessary()
        }

        allDayEvents.clear()
        allDayEvents.addAll(eventsDrawer.prepareDrawAllDayEvents(drawingContext))

        dayBackgroundDrawer.draw(drawingContext, canvas)
        backgroundGridDrawer.draw(drawingContext, canvas)

        eventsDrawer.drawSingleEvents(drawingContext, canvas, paint)

        nowLineDrawer.draw(drawingContext, canvas)
        headerRowDrawer.draw(drawingContext, canvas, paint)
        dayLabelDrawer.draw(drawingContext, canvas)

        eventsDrawer.drawAllDayEvents(allDayEvents, canvas, paint)
        timeColumnDrawer.drawTimeColumn(canvas)

        if (isFirstDraw) {
            // Temporary workaround to make sure that the events are actually being displayed
            invalidate()
        }

        if (viewState.requiresPostInvalidateOnAnimation) {
            viewState.requiresPostInvalidateOnAnimation = false
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun notifyScrollListeners() {
        val oldFirstVisibleDay = viewState.firstVisibleDay

        val totalDayWidth = configWrapper.totalDayWidth
        val visibleDays = configWrapper.numberOfVisibleDays
        val delta = round(ceil((configWrapper.currentOrigin.x / totalDayWidth).toDouble())).toInt() * -1

        val firstVisibleDay = today().plusDays(delta)
        val lastVisibleDay = firstVisibleDay.plusDays(visibleDays - 1)

        viewState.firstVisibleDay = firstVisibleDay
        viewState.lastVisibleDay = lastVisibleDay

        val hasFirstVisibleDayChanged = firstVisibleDay != oldFirstVisibleDay
        if (hasFirstVisibleDayChanged && scrollListener != null) {
            scrollListener?.onFirstVisibleDayChanged(firstVisibleDay, oldFirstVisibleDay)
        }
    }

    private fun prepareEventDrawing(canvas: Canvas) {
        cache.clearEventChipsCache()
        canvas.save()
        clipEventsRect(canvas)
    }

    private fun calculateWidthPerDay() {
        if (configWrapper.timeColumnWidth == UNINITIALIZED) {
            configWrapper.calculateTimeColumnWidth()
        }

        configWrapper.calculateWidthPerDay(width)
    }

    private fun clipEventsRect(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        canvas.clipRect(configWrapper.timeColumnWidth, configWrapper.headerHeight, width, height)
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

    var firstDayOfWeek: Int
        get() = DayOfWeek.toJavaCalendar(configWrapper.firstDayOfWeek)
        /**
         * Set the first day of the week. First day of the week is used only when the week view is first
         * drawn. It does not of any effect after user starts scrolling horizontally.
         *
         *
         * **Note:** This method will only work if WeekView is set to display more than 6 days at
         * once.
         *
         *
         * @param value The supported values are [java.util.Calendar.SUNDAY],
         * [java.util.Calendar.MONDAY], [java.util.Calendar.TUESDAY],
         * [java.util.Calendar.WEDNESDAY], [java.util.Calendar.THURSDAY],
         * [java.util.Calendar.FRIDAY].
         */
        set(value) {
            configWrapper.firstDayOfWeek = DayOfWeek.fromJavaCalendar(value)
            invalidate()
        }

    var numberOfVisibleDays: Int
        /**
         * Get the number of visible days in a week.
         *
         * @return The number of visible days in a week.
         */
        get() = configWrapper.numberOfVisibleDays
        /**
         * Set the number of visible days in a week.
         *
         * @param value The number of visible days in a week.
         */
        set(value) {
            configWrapper.numberOfVisibleDays = value
            (dateTimeInterpreter as? DefaultDateTimeInterpreter)?.setNumberOfDays(value)

            viewState.firstVisibleDay?.let {
                viewState.scrollToDay = it
            }

            invalidate()
        }

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

    var isShowHeaderRowBottomLine: Boolean
        get() = configWrapper.showHeaderRowBottomLine
        set(value) {
            configWrapper.showHeaderRowBottomLine = value
            invalidate()
        }

    var headerRowBottomLineColor: Int
        get() = configWrapper.headerRowBottomLinePaint.color
        set(value) {
            configWrapper.headerRowBottomLinePaint.color = value
            invalidate()
        }

    var headerRowBottomLineWidth: Int
        get() = configWrapper.headerRowBottomLinePaint.strokeWidth.toInt()
        set(value) {
            configWrapper.headerRowBottomLinePaint.strokeWidth = value.toFloat()
            invalidate()
        }

    var todayHeaderTextColor: Int
        get() = configWrapper.todayHeaderTextColor
        set(value) {
            configWrapper.todayHeaderTextColor = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    var timeColumnPadding: Int
        get() = configWrapper.timeColumnPadding
        set(value) {
            configWrapper.timeColumnPadding = value
            invalidate()
        }

    var timeColumnTextColor: Int
        get() = configWrapper.timeColumnTextColor
        set(value) {
            configWrapper.timeColumnTextColor = value
            invalidate()
        }

    var timeColumnBackgroundColor: Int
        get() = configWrapper.timeColumnBackgroundColor
        set(value) {
            configWrapper.timeColumnBackgroundColor = value
            invalidate()
        }

    var timeColumnTextSize: Int
        get() = configWrapper.timeColumnTextSize
        set(value) {
            configWrapper.timeColumnTextSize = value
            invalidate()
        }

    var isShowMidnightHour: Boolean
        get() = configWrapper.showMidnightHour
        set(value) {
            configWrapper.showMidnightHour = value
            invalidate()
        }

    var isShowTimeColumnHourSeparator: Boolean
        get() = configWrapper.showTimeColumnHourSeparator
        set(value) {
            configWrapper.showTimeColumnHourSeparator = value
            invalidate()
        }

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

    var isShowTimeColumnSeparator: Boolean
        get() = configWrapper.showTimeColumnSeparator
        set(value) {
            configWrapper.showTimeColumnSeparator = value
            invalidate()
        }

    var timeColumnSeparatorColor: Int
        get() = configWrapper.timeColumnSeparatorColor
        set(value) {
            configWrapper.timeColumnSeparatorColor = value
            invalidate()
        }

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

    var headerRowPadding: Int
        get() = configWrapper.headerRowPadding
        set(value) {
            configWrapper.headerRowPadding = value
            invalidate()
        }

    var headerRowBackgroundColor: Int
        get() = configWrapper.headerRowBackgroundColor
        set(value) {
            configWrapper.headerRowBackgroundColor = value
            invalidate()
        }

    var headerRowTextColor: Int
        get() = configWrapper.headerRowTextColor
        set(value) {
            configWrapper.headerRowTextColor = value
            invalidate()
        }

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

    var eventCornerRadius: Int
        get() = configWrapper.eventCornerRadius
        /**
         * Set corner radius for event rect.
         *
         * @param value the radius in px.
         */
        set(value) {
            configWrapper.eventCornerRadius = value
            invalidate()
        }

    var eventTextSize: Int
        get() = configWrapper.eventTextPaint.textSize.toInt()
        set(value) {
            configWrapper.eventTextPaint.textSize = value.toFloat()
            invalidate()
        }

    var allDayEventTextSize: Int
        get() = configWrapper.allDayEventTextPaint.textSize.toInt()
        set(value) {
            configWrapper.allDayEventTextPaint.textSize = value.toFloat()
            invalidate()
        }

    var eventTextColor: Int
        get() = configWrapper.eventTextPaint.color
        set(value) {
            configWrapper.eventTextPaint.color = value
            invalidate()
        }

    var eventPadding: Int
        get() = configWrapper.eventPadding
        set(value) {
            configWrapper.eventPadding = value
            invalidate()
        }

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

    var columnGap: Int
        get() = configWrapper.columnGap
        set(value) {
            configWrapper.columnGap = value
            invalidate()
        }

    var overlappingEventGap: Int
        get() = configWrapper.overlappingEventGap
        /**
         * Set the gap between overlapping events.
         *
         * @param value The gap between overlapping events.
         */
        set(value) {
            configWrapper.overlappingEventGap = value
            invalidate()
        }

    var eventMarginVertical: Int
        get() = configWrapper.eventMarginVertical
        /**
         * Set the top and bottom margin of the event. The event will release this margin from the top
         * and bottom edge. This margin is useful for differentiation consecutive events.
         *
         * @param value The top and bottom margin.
         */
        set(value) {
            configWrapper.eventMarginVertical = value
            invalidate()
        }

    var eventMarginHorizontal: Int
        get() = configWrapper.eventMarginHorizontal
        /**
         * Set the start and end margin of the event. The event will release this margin from the start
         * and end edge.
         *
         * @param value The start and end margin.
         */
        set(value) {
            configWrapper.eventMarginHorizontal = value
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Colors
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    var dayBackgroundColor: Int
        get() = configWrapper.dayBackgroundPaint.color
        set(value) {
            configWrapper.dayBackgroundPaint.color = value
            invalidate()
        }

    var todayBackgroundColor: Int
        get() = configWrapper.todayBackgroundPaint.color
        set(value) {
            configWrapper.todayBackgroundPaint.color = value
            invalidate()
        }

    var isShowDistinctWeekendColor: Boolean
        /**
         * Whether weekends should have a background color different from the normal day background
         * color. The weekend background colors are defined by the attributes
         * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
         *
         * @return True if weekends should have different background colors.
         */
        get() = configWrapper.showDistinctWeekendColor
        /**
         * Set whether weekends should have a background color different from the normal day background
         * color. The weekend background colors are defined by the attributes
         * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
         *
         * @param value True if weekends should have different background colors.
         */
        set(value) {
            configWrapper.showDistinctWeekendColor = value
            invalidate()
        }

    var isShowDistinctPastFutureColor: Boolean
        /**
         * Whether past and future days should have two different background colors. The past and
         * future day colors are defined by the attributes `futureBackgroundColor` and
         * `pastBackgroundColor`.
         *
         * @return True if past and future days should have two different background colors.
         */
        get() = configWrapper.showDistinctPastFutureColor
        /**
         * Set whether weekends should have a background color different from the normal day background
         * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
         * and `pastBackgroundColor`.
         *
         * @param value True if past and future should have two different background colors.
         */
        set(value) {
            configWrapper.showDistinctPastFutureColor = value
            invalidate()
        }

    // TODO: Past & future background color, pastWeekend and futureWeekend

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour height
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    var hourHeight: Float
        get() = configWrapper.hourHeight
        set(value) {
            configWrapper.newHourHeight = value
            invalidate()
        }

    var minHourHeight: Int
        get() = configWrapper.minHourHeight
        set(value) {
            configWrapper.minHourHeight = value
            invalidate()
        }

    var maxHourHeight: Int
        get() = configWrapper.maxHourHeight
        set(value) {
            configWrapper.maxHourHeight = value
            invalidate()
        }

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

    var isShowNowLine: Boolean
        /**
         * Get whether "now" line should be displayed. "Now" line is defined by the attributes
         * `nowLineColor` and `nowLineStrokeWidth`.
         *
         * @return True if "now" line should be displayed.
         */
        get() = configWrapper.showNowLine
        /**
         * Set whether "now" line should be displayed. "Now" line is defined by the attributes
         * `nowLineColor` and `nowLineStrokeWidth`.
         *
         * @param value True if "now" line should be displayed.
         */
        set(value) {
            configWrapper.showNowLine = value
            invalidate()
        }

    var nowLineColor: Int
        /**
         * Get the "now" line color.
         *
         * @return The color of the "now" line.
         */
        get() = configWrapper.nowLinePaint.color
        /**
         * Set the "now" line color.
         *
         * @param value The color of the "now" line.
         */
        set(value) {
            configWrapper.nowLinePaint.color = value
            invalidate()
        }

    var nowLineStrokeWidth: Int
        /**
         * Get the "now" line thickness.
         *
         * @return The thickness of the "now" line.
         */
        get() = configWrapper.nowLinePaint.strokeWidth.toInt()
        /**
         * Set the "now" line thickness.
         *
         * @param value The thickness of the "now" line.
         */
        set(value) {
            configWrapper.nowLinePaint.strokeWidth = value.toFloat()
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Now line dot
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    var isShowNowLineDot: Boolean
        /**
         * Get whether the dot on the left-hand side of the "now" line is displayed.
         *
         * @return True if "now" line dot is be displayed.
         */
        get() = configWrapper.showNowLineDot
        /**
         * Set whether the dot on the left-hand side of the "now" line should be displayed
         *
         * @param value True if "now" line dot should be displayed.
         */
        set(value) {
            configWrapper.showNowLineDot = value
            invalidate()
        }

    var nowLineDotColor: Int
        /**
         * Get the color of the dot on the left-hand side of the "now" line.
         *
         * @return The color of the "now" line dot.
         */
        get() = configWrapper.nowDotPaint.color
        /**
         * Set the color of the dot on the left-hand side of the "now" line.
         *
         * @param value The color of the "now" line dot.
         */
        set(value) {
            configWrapper.nowDotPaint.color = value
            invalidate()
        }

    var nowLineDotRadius: Int
        /**
         * Get the radius of the dot on the left-hand side of the "now" line.
         *
         * @return The radius of the "now" line dot.
         */
        get() = configWrapper.nowDotPaint.strokeWidth.toInt()
        /**
         * Set the radius of the dot on the left-hand side of the "now" line.
         *
         * @param value The radius of the "now" line dot.
         */
        set(value) {
            configWrapper.nowDotPaint.strokeWidth = value.toFloat()
            invalidate()
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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

    var isShowDaySeparators: Boolean
        get() = configWrapper.showDaySeparators
        set(value) {
            configWrapper.showDaySeparators = value
            invalidate()
        }

    var daySeparatorColor: Int
        get() = configWrapper.daySeparatorPaint.color
        set(value) {
            configWrapper.daySeparatorPaint.color = value
            invalidate()
        }

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

    var minDate: Calendar?
        get() = configWrapper.minDate
        set(value) {
            val maxDate = configWrapper.maxDate
            if (maxDate != null && value != null && value.isAfter(maxDate)) {
                throw IllegalArgumentException("Can't set a minDate that's after maxDate")
            }

            configWrapper.minDate = value
            invalidate()
        }

    var maxDate: Calendar?
        get() = configWrapper.maxDate
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

    var minHour: Int
        get() = configWrapper.minHour
        set(value) {
            if (value < 0 || value > configWrapper.maxHour) {
                throw IllegalArgumentException("minHour must be larger than 0 and smaller than maxHour.")
            }

            configWrapper.minHour = value
            invalidate()
        }

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

    var xScrollingSpeed: Float
        /**
         * Get the scrolling speed factor in horizontal direction.
         *
         * @return The speed factor in horizontal direction.
         */
        get() = configWrapper.xScrollingSpeed
        /**
         * Sets the speed for horizontal scrolling.
         *
         * @param value The new horizontal scrolling speed.
         */
        set(value) {
            configWrapper.xScrollingSpeed = value
        }

    var isHorizontalFlingEnabled: Boolean
        /**
         * Get whether the week view should fling horizontally.
         *
         * @return True if the week view has horizontal fling enabled.
         */
        get() = configWrapper.horizontalFlingEnabled
        /**
         * Set whether the week view should fling horizontally.
         */
        set(value) {
            configWrapper.horizontalFlingEnabled = value
        }

    var isHorizontalScrollingEnabled: Boolean
        /**
         * Returns whether the user can scroll horizontally. If not, the user can
         * only scroll vertically.
         *
         * @return True if horizontal scrolling is enabled. Default is true.
         */
        get() = configWrapper.horizontalScrollingEnabled
        /**
         * Sets whether the user can scroll horizontally.
         */
        set(value) {
            configWrapper.horizontalScrollingEnabled = value
        }

    var isVerticalFlingEnabled: Boolean
        /**
         * Get whether the week view should fling vertically.
         *
         * @return True if the week view has vertical fling enabled.
         */
        get() = configWrapper.verticalFlingEnabled
        /**
         * Set whether the week view should fling vertically.
         */
        set(value) {
            configWrapper.verticalFlingEnabled = value
        }

    var scrollDuration: Int
        /**
         * Get scroll duration
         *
         * @return scroll duration
         */
        get() = configWrapper.scrollDuration
        /**
         * Set the scroll duration
         */
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
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    val firstVisibleDay: Calendar
        get() = checkNotNull(viewState.firstVisibleDay)

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    val lastVisibleDay: Calendar?
        get() = viewState.lastVisibleDay

    /**
     * Show today on the week view.
     */
    fun goToToday() {
        goToDate(today())
    }

    fun goToCurrentTime() {
        val now = Calendar.getInstance()
        goToDate(now)
        goToHour(now.hour)
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    override fun goToDate(date: Calendar) {
        val minDate = configWrapper.minDate
        val maxDate = configWrapper.maxDate

        val numberOfVisibleDays = configWrapper.numberOfVisibleDays
        val showFirstDayOfWeekFirst = configWrapper.showFirstDayOfWeekFirst

        // If a minimum or maximum date is set, don't allow to go beyond them.
        var modifiedDate = date

        if (minDate != null && date.isBefore(minDate)) {
            modifiedDate = minDate
        } else if (maxDate != null && date.isAfter(maxDate)) {
            modifiedDate = maxDate.plusDays(1 - numberOfVisibleDays)
        } else if (numberOfVisibleDays >= 7 && showFirstDayOfWeekFirst) {
            val diff = configWrapper.computeDifferenceWithFirstDayOfWeek(date)
            modifiedDate = date.minusDays(diff)
        }

        gestureHandler.forceScrollFinished()

        if (viewState.areDimensionsInvalid) {
            viewState.scrollToDay = modifiedDate
            return
        }

        viewState.shouldRefreshEvents = true

        val diff = modifiedDate.daysFromToday

        configWrapper.currentOrigin.x = diff.toFloat() * (-1f) * configWrapper.totalDayWidth
        viewState.requiresPostInvalidateOnAnimation = true
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
     * Vertically scroll to a specific hour in the week view.
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
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    val firstVisibleHour: Double
        get() = (configWrapper.currentOrigin.y * -1 / configWrapper.hourHeight).toDouble()

    /////////////////////////////////////////////////////////////////
    //
    //  Fonts
    //
    /////////////////////////////////////////////////////////////////

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

    var onMonthChangeListener: OnMonthChangeListener<T>?
        get() {
            return eventChipsProvider.monthLoader?.onMonthChangeListener
        }
        set(value) {
            eventChipsProvider.monthLoader = MonthLoader(value)
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

    var onEmptyViewClickListener: OnEmptyViewClickListener?
        get() = gestureHandler.onEmptyViewClickListener
        set(value) {
            gestureHandler.onEmptyViewClickListener = value
        }

    fun setEmptyViewClickListener(
        block: (time: Calendar) -> Unit
    ) {
        onEmptyViewClickListener = object : OnEmptyViewClickListener {
            override fun onEmptyViewClicked(time: Calendar) {
                block(time)
            }
        }
    }

    var onEmptyViewLongPressListener: OnEmptyViewLongPressListener?
        get() = gestureHandler.onEmptyViewLongPressListener
        set(value) {
            gestureHandler.onEmptyViewLongPressListener = value
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
        block: (newFirstVisibleDay: Calendar, oldFirstVisibleDay: Calendar?) -> Unit
    ) {
        scrollListener = object : ScrollListener {
            override fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar, oldFirstVisibleDay: Calendar?) {
                block(firstVisibleDay, oldFirstVisibleDay)
            }
        }
    }

    var dateTimeInterpreter: DateTimeInterpreter
        get() = configWrapper.dateTimeInterpreter
        set(value) {
            configWrapper.dateTimeInterpreter = value
            dayLabelDrawer.clearLabelCache()
            timeColumnDrawer.clearLabelCache()
        }

}
