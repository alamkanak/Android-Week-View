package com.alamkanak.weekview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

typealias DateFormatter = (Calendar) -> String
typealias TimeFormatter = (Int) -> String

data class ViewState(
    // View
    var viewWidth: Int = 0,
    var viewHeight: Int = 0,

    // Calendar state
    var firstVisibleDate: Calendar = today(),
    var scrollToDate: Calendar? = null,
    var scrollToHour: Int? = null,

    private var isFirstDraw: Boolean = true,
    var areDimensionsInvalid: Boolean = true,

    // Drawing context
    private var startPixel: Float = 0f,
    val startPixels: MutableList<Float> = mutableListOf(),
    val dateRange: MutableList<Calendar> = mutableListOf(),
    val dateRangeWithStartPixels: MutableList<Pair<Calendar, Float>> = mutableListOf(),
    // TODO Update this

    // Calendar configuration
    var firstDayOfWeek: Int = now().firstDayOfWeek,
    var numberOfVisibleDays: Int = 0,
    var restoreNumberOfVisibleDays: Boolean = true,
    var showFirstDayOfWeekFirst: Boolean = false,
    var showCurrentTimeFirst: Boolean = false,

    // Header bottom line
    var showHeaderRowBottomLine: Boolean = false,
    var headerRowBottomLineColor: Int = 0,
    var headerRowBottomLineWidth: Int = 0,

    // Header bottom shadow
    var showHeaderRowBottomShadow: Boolean = false,
    var headerRowBottomShadowColor: Int = 0,
    var headerRowBottomShadowRadius: Int = 0,

    // Time column
    var timeColumnTextColor: Int = 0,
    var timeColumnBackgroundColor: Int = 0,
    var timeColumnPadding: Int = 0,
    var timeColumnTextSize: Int = 0,
    var showMidnightHour: Boolean = false,
    var showTimeColumnHourSeparator: Boolean = false,
    var timeColumnHoursInterval: Int = 0,

    // Time column separator
    var showTimeColumnSeparator: Boolean = false,
    var timeColumnSeparatorColor: Int = 0,
    var timeColumnSeparatorStrokeWidth: Int = 0,

    // Header row
    var headerRowTextColor: Int = 0,
    var headerRowBackgroundColor: Int = 0,
    var headerRowTextSize: Int = 0,
    var headerRowPadding: Int = 0,
    var todayHeaderTextColor: Int = 0,

    // Week number
    var showWeekNumber: Boolean = false,
    var weekNumberTextColor: Int = 0,
    var weekNumberTextSize: Int = 0,
    var weekNumberBackgroundColor: Int = 0,
    var weekNumberBackgroundCornerRadius: Int = 0,

    // Event chips
    var eventCornerRadius: Int = 0,
    var eventTextSize: Int = 0,
    var adaptiveEventTextSize: Boolean = false,
    var eventTextColor: Int = 0,

    var eventPaddingHorizontal: Int = 0,
    var eventPaddingVertical: Int = 0,

    var defaultEventColor: Int = 0,
    var allDayEventTextSize: Int = 0,

    // Event margins
    var columnGap: Int = 0,
    var overlappingEventGap: Int = 0,
    var eventMarginVertical: Int = 0,
    var eventMarginHorizontal: Int = 0,

    // Colors
    var dayBackgroundColor: Int = 0,
    var todayBackgroundColor: Int = 0,
    var showDistinctWeekendColor: Boolean = false,
    var showDistinctPastFutureColor: Boolean = false,
    var pastBackgroundColor: Int = 0,
    var futureBackgroundColor: Int = 0,
    var pastWeekendBackgroundColor: Int = 0,
    var futureWeekendBackgroundColor: Int = 0,

    // Hour height
    var hourHeight: Float = 0f,
    var minHourHeight: Int = 0,
    var maxHourHeight: Int = 0,
    var effectiveMinHourHeight: Int = 0,
    var showCompleteDay: Boolean = false,

    // Now line
    var showNowLine: Boolean = false,
    var nowLineColor: Int = 0,
    var nowLineStrokeWidth: Int = 0,

    // Now line dot
    var showNowLineDot: Boolean = false,
    var nowLineDotColor: Int = 0,
    var nowLineDotRadius: Int = 0,

    // Hour separators
    var showHourSeparators: Boolean = false,
    var hourSeparatorColor: Int = 0,
    var hourSeparatorStrokeWidth: Int = 0,

    // Day separators
    var showDaySeparators: Boolean = false,
    var daySeparatorColor: Int = 0,
    var daySeparatorStrokeWidth: Int = 0,

    // Scrolling
    @Deprecated("No longer used")
    var xScrollingSpeed: Float = 0f,
    @Deprecated("No longer used")
    var verticalFlingEnabled: Boolean = false,
    @Deprecated("No longer used")
    var horizontalFlingEnabled: Boolean = false,
    var horizontalScrollingEnabled: Boolean = false,
    @Deprecated("No longer used")
    var scrollDuration: Int = 0,

    // Time range
    var minHour: Int = 0,
    var maxHour: Int = 0,

    // Font
    var typeface: Typeface = Typeface.DEFAULT
) {

    private val _timeTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
    }

    val timeTextPaint: TextPaint
        get() = _timeTextPaint.apply {
            textAlign = Paint.Align.RIGHT
            textSize = timeColumnTextSize.toFloat()
            color = timeColumnTextColor
            typeface = typeface
        }

    var timeTextWidth: Float = 0.toFloat()

    var timeTextHeight: Float = 0.toFloat()

    private val _headerTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val headerTextPaint: TextPaint
        get() = _headerTextPaint.apply {
            color = headerRowTextColor
            textSize = headerRowTextSize.toFloat()
            typeface = Typeface.create(typeface, Typeface.BOLD)
        }

    private val _headerRowBottomLinePaint: Paint = Paint()

    val headerRowBottomLinePaint: Paint
        get() = _headerRowBottomLinePaint.apply {
            color = headerRowBottomLineColor
            strokeWidth = headerRowBottomLineWidth.toFloat()
        }

    var headerTextHeight: Float = headerTextPaint.descent() - headerTextPaint.ascent()

    var headerHeight: Float = 0.toFloat()

    private val _todayHeaderTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val todayHeaderTextPaint: TextPaint
        get() = _todayHeaderTextPaint.apply {
            textSize = headerRowTextSize.toFloat()
            typeface = Typeface.create(typeface, Typeface.BOLD)
            color = todayHeaderTextColor
        }

    private var currentAllDayEventHeight: Int = 0

    // Dates in the past have origin.x > 0, dates in the future have origin.x < 0
    var currentOrigin = PointF(0f, 0f)

    var headerBackgroundPaint: Paint = Paint().apply {
        color = headerRowBackgroundColor
    }

    var widthPerDay: Float = 0f

    private val _dayBackgroundPaint: Paint = Paint()

    val dayBackgroundPaint: Paint
        get() = _dayBackgroundPaint.apply {
            color = dayBackgroundColor
        }

    private val _hourSeparatorPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
    }

    val hourSeparatorPaint: Paint
        get() = _hourSeparatorPaint.apply {
            strokeWidth = hourSeparatorStrokeWidth.toFloat()
            color = hourSeparatorColor
        }

    private val _daySeparatorPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
    }

    val daySeparatorPaint: Paint
        get() = _daySeparatorPaint.apply {
            strokeWidth = daySeparatorStrokeWidth.toFloat()
            color = daySeparatorColor
        }

    private val _todayBackgroundPaint: Paint = Paint()

    val todayBackgroundPaint: Paint
        get() = _todayBackgroundPaint.apply {
            color = todayBackgroundColor
        }

    private val _futureBackgroundPaint = Paint()

    val futureBackgroundPaint: Paint
        get() = _futureBackgroundPaint.apply {
            color = futureBackgroundColor
        }

    private val _pastBackgroundPaint = Paint()

    val pastBackgroundPaint: Paint
        get() = _pastBackgroundPaint.apply {
            color = pastBackgroundColor
        }

    private val _futureWeekendBackgroundPaint = Paint()

    val futureWeekendBackgroundPaint: Paint
        get() = _futureWeekendBackgroundPaint.apply {
            color = futureWeekendBackgroundColor
        }

    private val _pastWeekendBackgroundPaint = Paint()

    val pastWeekendBackgroundPaint: Paint
        get() = _pastWeekendBackgroundPaint.apply {
            color = pastWeekendBackgroundColor
        }

    private val _timeColumnSeparatorPaint = Paint()

    val timeColumnSeparatorPaint: Paint
        get() = _timeColumnSeparatorPaint.apply {
            color = timeColumnSeparatorColor
            strokeWidth = timeColumnSeparatorStrokeWidth.toFloat()
        }

    private val _nowLinePaint = Paint()

    val nowLinePaint: Paint
        get() = _nowLinePaint.apply {
            strokeWidth = nowLineStrokeWidth.toFloat()
            color = nowLineColor
        }

    private val _nowDotPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val nowDotPaint: Paint
        get() = _nowDotPaint.apply {
            strokeWidth = nowLineDotRadius.toFloat()
            color = nowLineDotColor
        }

    var timeColumnWidth: Float = Constants.UNINITIALIZED

    private val _eventTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val eventTextPaint: TextPaint
        get() = _eventTextPaint.apply {
            color = eventTextColor
            textSize = eventTextSize.toFloat()
            typeface = typeface
        }

    private val _allDayEventTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val allDayEventTextPaint: TextPaint
        get() = _allDayEventTextPaint.apply {
            color = eventTextColor
            textSize = allDayEventTextSize.toFloat()
            typeface = typeface
        }

    private val _timeColumnBackgroundPaint: Paint = Paint()

    val timeColumnBackgroundPaint: Paint
        get() = _timeColumnBackgroundPaint.apply {
            color = timeColumnBackgroundColor
        }

    var hasEventInHeader: Boolean = false

    var newHourHeight: Float = Constants.UNINITIALIZED

    var minDate: Calendar? = null
    var maxDate: Calendar? = null

    var dateFormatter: DateFormatter = { date ->
        defaultDateFormatter(numberOfDays = numberOfVisibleDays).format(date.time)
    }

    private var _timeFormatter: TimeFormatter = { hour ->
        val date = now().withTime(hour = hour, minutes = 0)
        defaultTimeFormatter().format(date.time)
    }

    var timeFormatter: TimeFormatter
        get() = _timeFormatter
        set(value) {
            _timeFormatter = value
            initTimeColumnTextBounds()
        }

    init {
        initTimeColumnTextBounds()
        refreshHeaderHeight()
    }

    val minX: Float
        get() {
            return maxDate?.let {
                val date = it - Days(numberOfVisibleDays - 1)
                getXOriginForDate(date)
            } ?: Float.NEGATIVE_INFINITY
        }

    val maxX: Float
        get() = minDate?.let { getXOriginForDate(it) } ?: Float.POSITIVE_INFINITY

    val isSingleDay: Boolean
        get() = numberOfVisibleDays == 1

    val totalDayHeight: Float
        get() {
            val dayHeight = hourHeight * hoursPerDay
            return dayHeight + headerHeight
        }

    val totalDayWidth: Float
        get() = widthPerDay + columnGap

//    val headerRowBottomLineWidth: Float
//        get() = if (showHeaderRowBottomLine) headerRowBottomLinePaint.strokeWidth else 0f

    private val _weekNumberBounds: RectF = RectF()

    val weekNumberBounds: RectF
        get() = _weekNumberBounds.apply {
            left = 0f
            top = 0f
            right = timeColumnWidth
            bottom = headerHeight
        }

    private val _weekNumberTextPaint: Paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val weekNumberTextPaint: Paint
        get() = _weekNumberTextPaint.apply {
            color = weekNumberTextColor
            textSize = weekNumberTextSize.toFloat()
            typeface = this.typeface
        }

    private val _weekNumberBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val weekNumberBackgroundPaint: Paint
        get() = _weekNumberBackgroundPaint.apply {
            color = weekNumberBackgroundColor
        }

//    var todayHeaderTextColor: Int
//        get() = todayHeaderTextColor
//        set(value) {
//            todayHeaderTextPaint.color = value
//            todayHeaderTextColor = value
//        }

//    var timeColumnTextColor: Int
//        get() = timeColumnTextColor
//        set(value) {
//            timeTextPaint.color = value
//            timeColumnTextColor = value
//        }

//    var timeColumnBackgroundColor: Int
//        get() = timeColumnBackgroundColor
//        set(value) {
//            timeColumnBackgroundPaint.color = value
//            timeColumnBackgroundColor = value
//        }

//    var headerRowBackgroundColor: Int
//        get() = headerRowBackgroundColor
//        set(value) {
//            headerRowBackgroundColor = value
//            headerBackgroundPaint.color = value
//        }

//    var headerRowTextColor: Int
//        get() = headerRowTextColor
//        set(value) {
//            headerRowTextColor = value
//            headerTextPaint.color = value
//        }

//    var headerRowTextSize: Int
//        get() = headerRowTextSize
//        set(value) {
//            headerRowTextSize = value
//            headerTextPaint.textSize = value.toFloat()
//            todayHeaderTextPaint.textSize = value.toFloat()
//        }

    val hoursPerDay: Int
        get() = maxHour - minHour

    val minutesPerDay: Int
        get() = (hoursPerDay * Constants.MINUTES_PER_HOUR).toInt()

    val timeRange: IntRange
        get() {
            val includeMidnightHour = showTimeColumnHourSeparator && showMidnightHour
            val padding = if (includeMidnightHour) 0 else timeColumnHoursInterval
            val startHour = minHour + padding
            return startHour until maxHour
        }

    val displayedHours: IntProgression
        get() = timeRange step timeColumnHoursInterval

    fun calculateTimeColumnWidth() {
        timeColumnWidth = timeTextWidth + timeColumnPadding * 2
    }

    fun calculateWidthPerDay() {
        val viewWidth = viewWidth.toFloat()
        val availableWidth = viewWidth - timeColumnWidth - columnGap * numberOfVisibleDays
        widthPerDay = availableWidth / numberOfVisibleDays
    }

    fun getXOriginForDate(date: Calendar): Float {
        return date.daysFromToday * totalDayWidth * -1f
    }

    fun updateAllDayEventHeight(height: Int) {
        currentAllDayEventHeight = height
        refreshHeaderHeight()
    }

    private fun moveCurrentOriginIfFirstDraw() {
        // If the week view is being drawn for the first time, then consider the first day of the
        // week.
        val today = today()
        val isWeekView = numberOfVisibleDays >= 7
        val currentDayIsNotToday = today.dayOfWeek != firstDayOfWeek

        if (isWeekView && currentDayIsNotToday && showFirstDayOfWeekFirst) {
            val difference = today.computeDifferenceWithFirstDayOfWeek()
            currentOrigin.x += (widthPerDay + columnGap) * difference
        }

        if (showCurrentTimeFirst) {
            scrollToCurrentTime()
        }

        // Overwrites the origin when today is out of date range
        currentOrigin.x = min(currentOrigin.x, maxX)
        currentOrigin.x = max(currentOrigin.x, minX)
    }

    private fun scrollToCurrentTime() {
        val desired = now()
        if (desired.hour > minHour) {
            // Add some padding above the current time (and thus: the now line)
            desired -= Hours(1)
        }

        val minTime = now().withTime(hour = minHour, minutes = 0)
        val maxTime = now().withTime(hour = maxHour, minutes = 0)
        desired.limitBy(minTime, maxTime)

        val fraction = desired.minute.toFloat() / Constants.MINUTES_PER_HOUR
        val verticalOffset = hourHeight * (desired.hour + fraction)
        val desiredOffset = totalDayHeight - viewHeight

        currentOrigin.y = min(desiredOffset, verticalOffset) * -1
    }

    /**
     * Returns the provided date, if it is within [minDate] and [maxDate]. Otherwise, it returns
     * [minDate] or [maxDate].
     */
    fun getDateWithinDateRange(date: Calendar): Calendar {
        val minDate = minDate ?: date
        val maxDate = maxDate ?: date

        return if (date.isBefore(minDate)) {
            minDate
        } else if (date.isAfter(maxDate)) {
            maxDate + Days(1 - numberOfVisibleDays)
        } else if (numberOfVisibleDays >= 7 && showFirstDayOfWeekFirst) {
            val diff = date.computeDifferenceWithFirstDayOfWeek()
            date - Days(diff)
        } else {
            date
        }
    }

    private fun Calendar.computeDifferenceWithFirstDayOfWeek(): Int {
        val firstDayOfWeek = firstDayOfWeek
        return if (firstDayOfWeek == Calendar.MONDAY && dayOfWeek == Calendar.SUNDAY) {
            // Special case, because Calendar.MONDAY has constant value 2 and Calendar.SUNDAY has
            // constant value 1. The correct result to return is 6 days, not -1 days.
            6
        } else {
            dayOfWeek - firstDayOfWeek
        }
    }

    private fun refreshAfterZooming() {
        if (showCompleteDay) {
            return
        }

        val dayHeight = hourHeight * hoursPerDay
        val isNotFillingEntireHeight = dayHeight < viewHeight
        val didZoom = newHourHeight > 0

        if (isNotFillingEntireHeight || didZoom) {
            newHourHeight = max(newHourHeight, effectiveMinHourHeight.toFloat())
            newHourHeight = min(newHourHeight, maxHourHeight.toFloat())

            // Compute a minimum hour height so that users can't zoom out further
            // than the desired hours per day
            val minHourHeight = (viewHeight - headerHeight) / hoursPerDay
            newHourHeight = max(newHourHeight, minHourHeight)

            currentOrigin.y = currentOrigin.y / hourHeight * newHourHeight
            hourHeight = newHourHeight
            newHourHeight = Constants.UNINITIALIZED
        }
    }

    private fun updateVerticalOrigin() {
        // If the new currentOrigin.y is invalid, make it valid.
        val dayHeight = hourHeight * hoursPerDay
        val potentialNewVerticalOrigin = viewHeight - (dayHeight + headerHeight)

        currentOrigin.y = max(currentOrigin.y, potentialNewVerticalOrigin)
        currentOrigin.y = min(currentOrigin.y, 0f)
    }

    fun getTotalHeaderHeight(): Float {
        return headerHeight + headerRowPadding * 2f
    }

    fun getPastBackgroundPaint(useWeekendColor: Boolean): Paint {
        return if (useWeekendColor) pastWeekendBackgroundPaint else pastBackgroundPaint
    }

    fun getFutureBackgroundPaint(useWeekendColor: Boolean): Paint {
        return if (useWeekendColor) futureWeekendBackgroundPaint else futureBackgroundPaint
    }

    fun getDayBackgroundPaint(isToday: Boolean): Paint {
        return if (isToday) todayBackgroundPaint else dayBackgroundPaint
    }

    private fun updateHourHeight(viewHeight: Int) {
        hourHeight = (viewHeight - headerHeight) / hoursPerDay
        newHourHeight = hourHeight
    }

    fun refreshHeaderHeight() {
        headerHeight = headerRowPadding * 2 + headerTextHeight

        if (showHeaderRowBottomLine) {
            headerHeight += headerRowBottomLinePaint.strokeWidth
        }

        if (hasEventInHeader) {
            headerHeight += currentAllDayEventHeight.toFloat()
        }

        if (showCompleteDay) {
            hourHeight = (viewHeight - headerHeight) / hoursPerDay
            newHourHeight = hourHeight
        }
    }

    private fun initTimeColumnTextBounds() {
        val textLayouts = timeRange
            .map { _timeFormatter(it) }
            .map { it.toTextLayout(timeTextPaint, width = Int.MAX_VALUE) }

        timeTextWidth = textLayouts.map { it.maxLineLength }.max() ?: 0f
        timeTextHeight = textLayouts.map { it.height.toFloat() }.max() ?: 0f
    }

    fun update() {
        updateViewState()
        updateScrollState()
        updateDateRange()
    }

    private fun updateScrollState() {
        refreshAfterZooming()
        updateVerticalOrigin()
    }

    private fun updateViewState() {
        val totalHeaderHeight = getTotalHeaderHeight().toInt()
        val dynamicHourHeight = (viewHeight - totalHeaderHeight) / hoursPerDay

        if (areDimensionsInvalid) {
            effectiveMinHourHeight = max(minHourHeight, dynamicHourHeight)
//            scrollToDate = null
//            scrollToHour = null
            areDimensionsInvalid = false
        }

        if (isFirstDraw) {
            moveCurrentOriginIfFirstDraw()
            isFirstDraw = false
        }
    }

    private fun updateDateRange() {
        val originX = currentOrigin.x

        val daysFromOrigin = ceil(originX / totalDayWidth).toInt() * (-1)
        startPixel = timeColumnWidth + originX + totalDayWidth * daysFromOrigin

        val start = daysFromOrigin + 1
        val end = start + numberOfVisibleDays

        // If the user is scrolling, a new view becomes partially visible, so we must add an
        // additional date to the date range
        val isNotScrolling = originX % totalDayWidth == 0f
        val modifiedEnd = if (isNotScrolling) end - 1 else end

        dateRange.clear()
        dateRange += createDateRange(start, modifiedEnd)

        startPixels.clear()
        startPixels += dateRange.indices.map { startPixel + it * totalDayWidth }

        dateRangeWithStartPixels.clear()
        dateRangeWithStartPixels += dateRange.zip(startPixels)
    }

    fun onSizeChanged(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height

        if (timeColumnWidth == Constants.UNINITIALIZED) {
            calculateTimeColumnWidth()
        }

        calculateWidthPerDay()

        if (showCompleteDay) {
            updateHourHeight(height)
        }
    }

    fun updateNumberOfVisibleDays(days: Int) {
        numberOfVisibleDays = days
        // Scroll to first visible day after changing the number of visible days
        scrollToDate = firstVisibleDate
        calculateWidthPerDay()
    }

    fun invalidate() {
        areDimensionsInvalid = true
    }

    companion object {
        fun make(context: Context, attrs: AttributeSet?): ViewState {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
            return ViewState(
                // Calendar configuration
                firstDayOfWeek = a.getInt(R.styleable.WeekView_firstDayOfWeek)
                    ?: now().firstDayOfWeek,
                numberOfVisibleDays = a.getInt(R.styleable.WeekView_numberOfVisibleDays, 3),
                restoreNumberOfVisibleDays = a.getBoolean(R.styleable.WeekView_restoreNumberOfVisibleDays, true),
                showFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, false),
                showCurrentTimeFirst = a.getBoolean(R.styleable.WeekView_showCurrentTimeFirst, false),

                // Header bottom line
                showHeaderRowBottomLine = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomLine, false),
                headerRowBottomLineColor = a.getColor(R.styleable.WeekView_headerRowBottomLineColor, Defaults.GRID_COLOR),
                headerRowBottomLineWidth = a.getDimensionPixelSize(R.styleable.WeekView_headerRowBottomLineWidth, 1),

                // Header bottom shadow
                showHeaderRowBottomShadow = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomShadow, false),
                headerRowBottomShadowColor = a.getColor(R.styleable.WeekView_headerRowBottomShadowColor, Color.LTGRAY),
                headerRowBottomShadowRadius = a.getDimensionPixelSize(R.styleable.WeekView_headerRowBottomShadowRadius, 2),

                // Time column
                timeColumnTextColor = a.getColor(R.styleable.WeekView_timeColumnTextColor, Color.BLACK),
                timeColumnBackgroundColor = a.getColor(R.styleable.WeekView_timeColumnBackgroundColor, Color.WHITE),
                timeColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, 10),
                timeColumnTextSize = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnTextSize, Defaults.textSize(context)),
                showMidnightHour = a.getBoolean(R.styleable.WeekView_showMidnightHour, false),
                showTimeColumnHourSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnHourSeparator, false),
                timeColumnHoursInterval = a.getInteger(R.styleable.WeekView_timeColumnHoursInterval, 1),

                // Time column separator
                showTimeColumnSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnSeparator, false),
                timeColumnSeparatorColor = a.getColor(R.styleable.WeekView_timeColumnSeparatorColor, Defaults.GRID_COLOR),
                timeColumnSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, 1),

                // Time range
                minHour = a.getInt(R.styleable.WeekView_minHour, 0),
                maxHour = a.getInt(R.styleable.WeekView_maxHour, 24),

                // Header row
                headerRowTextColor = a.getColor(R.styleable.WeekView_headerRowTextColor, Color.BLACK),
                headerRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, Color.WHITE),
                headerRowTextSize = a.getDimensionPixelSize(R.styleable.WeekView_headerRowTextSize, Defaults.textSize(context)),
                headerRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, 10),
                todayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, Defaults.HIGHLIGHT_COLOR),

                // Week number
                showWeekNumber = a.getBoolean(R.styleable.WeekView_showWeekNumber, false),
                weekNumberTextColor = a.getColor(R.styleable.WeekView_weekNumberTextColor, Color.WHITE),
                weekNumberTextSize = a.getDimensionPixelSize(R.styleable.WeekView_weekNumberTextSize, Defaults.textSize(context)),
                weekNumberBackgroundColor = a.getColor(R.styleable.WeekView_weekNumberBackgroundColor, Color.LTGRAY),
                weekNumberBackgroundCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_weekNumberBackgroundCornerRadius, 0),

                // Event chips
                eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, 0),
                eventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, Defaults.textSize(context)),
                adaptiveEventTextSize = a.getBoolean(R.styleable.WeekView_adaptiveEventTextSize, false),
                eventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, Color.BLACK),
                defaultEventColor = a.getColor(R.styleable.WeekView_defaultEventColor, Defaults.EVENT_COLOR),

                // Event padding
                eventPaddingHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_eventPaddingHorizontal, 8),
                eventPaddingVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventPaddingVertical, 8),

                // Event margins
                columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, 10),
                overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, 0),
                eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, 2),
                eventMarginHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, 0),

                // Colors
                dayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, Defaults.BACKGROUND_COLOR),
                todayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, Defaults.BACKGROUND_COLOR),
                showDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, false),
                showDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, false),
                pastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, Defaults.PAST_BACKGROUND_COLOR),
                futureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, Defaults.FUTURE_BACKGROUND_COLOR),

                // Hour height
                hourHeight = a.getDimension(R.styleable.WeekView_hourHeight, 50f),
                minHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, 0),
                maxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, 400),
                showCompleteDay = a.getBoolean(R.styleable.WeekView_showCompleteDay, false),

                // Now line
                showNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, false),
                nowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, Defaults.NOW_COLOR),
                nowLineStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_nowLineStrokeWidth, 5),

                // Now line dot
                showNowLineDot = a.getBoolean(R.styleable.WeekView_showNowLineDot, false),
                nowLineDotColor = a.getColor(R.styleable.WeekView_nowLineDotColor, Defaults.NOW_COLOR),
                nowLineDotRadius = a.getDimensionPixelSize(R.styleable.WeekView_nowLineDotRadius, 16),

                // Hour separators
                showHourSeparators = a.getBoolean(R.styleable.WeekView_showHourSeparator, true),
                hourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, Defaults.SEPARATOR_COLOR),
                hourSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorStrokeWidth, 2),

                // Day separators
                showDaySeparators = a.getBoolean(R.styleable.WeekView_showDaySeparator, true),
                daySeparatorColor = a.getColor(R.styleable.WeekView_daySeparatorColor, Defaults.SEPARATOR_COLOR),
                daySeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_daySeparatorStrokeWidth, 2),

                // Scrolling
                xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, 1f),
                horizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, true),
                horizontalScrollingEnabled = a.getBoolean(R.styleable.WeekView_horizontalScrollingEnabled, true),
                verticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, true),
                scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, 250)
            ).apply {
                pastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, pastBackgroundColor)
                futureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, futureBackgroundColor)
                effectiveMinHourHeight = minHourHeight

                allDayEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventTextSize, eventTextSize)

                // Font
                val fontFamily = a.getString(R.styleable.WeekView_fontFamily)
                val typefaceIndex = a.getInteger(R.styleable.WeekView_typeface, 0)
                val textStyle = a.getInteger(R.styleable.WeekView_textStyle, 0)
                typeface = getTypefaceFromAttrs(fontFamily, typefaceIndex, textStyle)
            }.also {
                a.recycle()
            }
        }

        private fun getTypefaceFromAttrs(
            familyName: String?,
            typefaceIndex: Int,
            styleIndex: Int
        ): Typeface {
            if (familyName != null) {
                val tf = Typeface.create(familyName, styleIndex)
                if (tf != null) {
                    return tf
                }
            }

            return when (typefaceIndex) {
                SANS -> Typeface.SANS_SERIF
                SERIF -> Typeface.SERIF
                MONOSPACE -> Typeface.MONOSPACE
                else -> Typeface.DEFAULT
            }
        }

        private const val SANS = 1
        private const val SERIF = 2
        private const val MONOSPACE = 3
    }
}

private fun TypedArray.getInt(index: Int): Int? {
    return if (hasValue(index)) getInteger(index, 0) else null
}

private object Defaults {
    const val BACKGROUND_COLOR = Color.WHITE
    val PAST_BACKGROUND_COLOR = Color.rgb(227, 227, 227)
    val FUTURE_BACKGROUND_COLOR = Color.rgb(245, 245, 245)

    val EVENT_COLOR = Color.rgb(159, 198, 231)

    val GRID_COLOR = Color.rgb(102, 102, 102)
    const val NOW_COLOR = Color.BLACK
    val SEPARATOR_COLOR = Color.rgb(230, 230, 230)
    val HIGHLIGHT_COLOR = Color.rgb(39, 137, 228)

    fun textSize(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, displayMetrics).toInt()
    }
}
