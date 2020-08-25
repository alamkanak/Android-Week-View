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
import android.util.TypedValue.COMPLEX_UNIT_SP
import androidx.annotation.AttrRes
import androidx.core.graphics.ColorUtils
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

typealias DateFormatter = (Calendar) -> String
typealias TimeFormatter = (Int) -> String

internal data class ViewState(
    // View
    var viewWidth: Int = 0,
    var viewHeight: Int = 0,

    // Calendar state
    var firstVisibleDate: Calendar = today(),
    var scrollToDate: Calendar? = null,
    var scrollToHour: Int? = null,

    private var isFirstDraw: Boolean = true,

    // Drawing context
    private var startPixel: Float = 0f,
    val startPixels: MutableList<Float> = mutableListOf(),
    val dateRange: MutableList<Calendar> = mutableListOf(),
    val dateRangeWithStartPixels: MutableList<Pair<Calendar, Float>> = mutableListOf(),

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
            typeface = this@ViewState.typeface
        }

    var timeTextHeight: Float = 0f

    private val _headerTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val headerTextPaint: TextPaint
        get() = _headerTextPaint.apply {
            color = headerRowTextColor
            textSize = headerRowTextSize.toFloat()
            typeface = when (this@ViewState.typeface) {
                Typeface.DEFAULT -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
                else -> Typeface.create(this@ViewState.typeface, Typeface.BOLD)
            }
        }

    private val _headerRowBottomLinePaint: Paint = Paint()

    val headerRowBottomLinePaint: Paint
        get() = _headerRowBottomLinePaint.apply {
            color = headerRowBottomLineColor
            strokeWidth = headerRowBottomLineWidth.toFloat()
        }

    var dateLabelHeight: Float = headerTextPaint.descent() - headerTextPaint.ascent()

    var headerHeight: Float = 0f

    private val _todayHeaderTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val todayHeaderTextPaint: TextPaint
        get() = _todayHeaderTextPaint.apply {
            color = todayHeaderTextColor
            textSize = headerRowTextSize.toFloat()
            // typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            typeface = when (this@ViewState.typeface) {
                Typeface.DEFAULT -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
                else -> Typeface.create(this@ViewState.typeface, Typeface.BOLD)
            }
        }

    var currentAllDayEventHeight: Int = 0

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

    var timeColumnWidth: Float = 0f

    private val _eventTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val eventTextPaint: TextPaint
        get() = _eventTextPaint.apply {
            color = eventTextColor
            textSize = eventTextSize.toFloat()
            typeface = this@ViewState.typeface
        }

    private val _allDayEventTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val allDayEventTextPaint: TextPaint
        get() = _allDayEventTextPaint.apply {
            color = eventTextColor
            textSize = allDayEventTextSize.toFloat()
            typeface = this@ViewState.typeface
        }

    private val _timeColumnBackgroundPaint: Paint = Paint()

    val timeColumnBackgroundPaint: Paint
        get() = _timeColumnBackgroundPaint.apply {
            color = timeColumnBackgroundColor
        }

    var newHourHeight: Float = 0f

    var minDate: Calendar? = null
    var maxDate: Calendar? = null

    var dateFormatter: DateFormatter = { date ->
        defaultDateFormatter(numberOfDays = numberOfVisibleDays).format(date.time)
    }

    var timeFormatter: TimeFormatter = { hour ->
        val date = now().withTime(hour = hour, minutes = 0)
        defaultTimeFormatter().format(date.time)
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

    private val _headerBounds: RectF = RectF()

    val headerBounds: RectF
        get() = _headerBounds.apply {
            left = timeColumnWidth
            top = 0f
            right = viewWidth.toFloat()
            bottom = headerHeight
        }

    private val _calendarGridBounds: RectF = RectF()

    val calendarGridBounds: RectF
        get() = _calendarGridBounds.apply {
            left = timeColumnWidth
            top = headerHeight
            right = viewWidth.toFloat()
            bottom = viewHeight.toFloat()
        }

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
            typeface = this@ViewState.typeface
        }

    private val _weekNumberBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val weekNumberBackgroundPaint: Paint
        get() = _weekNumberBackgroundPaint.apply {
            color = weekNumberBackgroundColor
        }

    val hoursPerDay: Int
        get() = maxHour - minHour

    val minutesPerDay: Int
        get() = hoursPerDay * 60

    private val timeRange: IntRange
        get() {
            val includeMidnightHour = showTimeColumnHourSeparator && showMidnightHour
            val padding = if (includeMidnightHour) 0 else timeColumnHoursInterval
            val startHour = minHour + padding
            return startHour until maxHour
        }

    val displayedHours: IntProgression
        get() = timeRange step timeColumnHoursInterval

    private fun calculateWidthPerDay() {
        val viewWidth = viewWidth.toFloat()
        val availableWidth = viewWidth - timeColumnWidth - columnGap * numberOfVisibleDays
        widthPerDay = availableWidth / numberOfVisibleDays
    }

    fun getXOriginForDate(date: Calendar): Float {
        return date.daysFromToday * totalDayWidth * -1f
    }

    private fun scrollToFirstDayOfWeek() {
        // If the week view is being drawn for the first time, consider the first day of the week.
        val today = today()
        val isWeekView = numberOfVisibleDays >= 7
        val currentDayIsNotToday = today.dayOfWeek != firstDayOfWeek

        if (isWeekView && currentDayIsNotToday) {
            val difference = today.computeDifferenceWithFirstDayOfWeek()
            currentOrigin.x += (widthPerDay + columnGap) * difference
        }

        currentOrigin.x = currentOrigin.x.limit(minValue = minX, maxValue = maxX)
    }

    private fun scrollToCurrentTime() {
        val desired = now()
        if (desired.hour > minHour) {
            // Add some padding above the current time (and thus: the now line)
            desired -= Hours(1)
        } else {
            desired -= Minutes(desired.minute)
        }

        val minTime = now().withTime(hour = minHour, minutes = 0)
        val maxTime = now().withTime(hour = maxHour, minutes = 0)
        desired.limitBy(minTime, maxTime)

        val fraction = desired.minute / 60f
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
        val isNotFillingEntireHeight = dayHeight < (viewHeight - headerHeight)
        val didZoom = newHourHeight > 0

        if (isNotFillingEntireHeight || didZoom) {
            // Compute a minimum hour height so that users can't zoom out further
            // than the desired hours per day
            val newMinHourHeight = (viewHeight - headerHeight) / hoursPerDay
            val effectiveMinHourHeight = max(minHourHeight.toFloat(), newMinHourHeight)

            newHourHeight = newHourHeight.limit(
                minValue = effectiveMinHourHeight,
                maxValue = maxHourHeight.toFloat()
            )

            // newHourHeight = max(newHourHeight, minHourHeight)

            currentOrigin.y = currentOrigin.y / hourHeight * newHourHeight
            hourHeight = newHourHeight
            newHourHeight = 0f
        }
    }

    private fun updateVerticalOrigin() {
        // If the new currentOrigin.y is invalid, make it valid.
        val dayHeight = hourHeight * hoursPerDay
        val potentialNewVerticalOrigin = viewHeight - (dayHeight + headerHeight)

        currentOrigin.y = max(currentOrigin.y, potentialNewVerticalOrigin)
        currentOrigin.y = min(currentOrigin.y, 0f)
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
        headerHeight = headerRowPadding + dateLabelHeight

        if (currentAllDayEventHeight > 0) {
            headerHeight += headerRowPadding + currentAllDayEventHeight.toFloat()
        }

        headerHeight += headerRowPadding

        if (showHeaderRowBottomLine) {
            headerHeight += headerRowBottomLinePaint.strokeWidth
        }

        if (showCompleteDay) {
            hourHeight = (viewHeight - headerHeight) / hoursPerDay
            newHourHeight = hourHeight
        }
    }

    fun updateTimeColumnBounds(lineLength: Float, lineHeight: Float) {
        timeTextHeight = lineHeight
        timeColumnWidth = lineLength + timeColumnPadding * 2
        calculateWidthPerDay()
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
        if (!isFirstDraw) {
            return
        }

        if (showFirstDayOfWeekFirst) {
            scrollToFirstDayOfWeek()
        }

        if (showCurrentTimeFirst) {
            scrollToCurrentTime()
        }

        isFirstDraw = false
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
                headerRowBottomLineColor = a.getColor(R.styleable.WeekView_headerRowBottomLineColor, context.lineColor),
                headerRowBottomLineWidth = a.getDimensionPixelSize(R.styleable.WeekView_headerRowBottomLineWidth, 1),

                // Header bottom shadow
                showHeaderRowBottomShadow = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomShadow, false),
                headerRowBottomShadowColor = a.getColor(R.styleable.WeekView_headerRowBottomShadowColor, context.shadowColor),
                headerRowBottomShadowRadius = a.getDimensionPixelSize(R.styleable.WeekView_headerRowBottomShadowRadius, 4),

                // Time column
                timeColumnTextColor = a.getColor(R.styleable.WeekView_timeColumnTextColor, context.textColorPrimary),
                timeColumnBackgroundColor = a.getColor(R.styleable.WeekView_timeColumnBackgroundColor, context.windowBackground),
                timeColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, 10),
                timeColumnTextSize = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnTextSize, context.defaultTextSize),
                showMidnightHour = a.getBoolean(R.styleable.WeekView_showMidnightHour, false),
                showTimeColumnHourSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnHourSeparator, false),
                timeColumnHoursInterval = a.getInteger(R.styleable.WeekView_timeColumnHoursInterval, 1),

                // Time column separator
                showTimeColumnSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnSeparator, false),
                timeColumnSeparatorColor = a.getColor(R.styleable.WeekView_timeColumnSeparatorColor, context.lineColor),
                timeColumnSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, 1),

                // Time range
                minHour = a.getInt(R.styleable.WeekView_minHour, 0),
                maxHour = a.getInt(R.styleable.WeekView_maxHour, 24),

                // Header row
                headerRowTextColor = a.getColor(R.styleable.WeekView_headerRowTextColor, context.textColorPrimary),
                headerRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, context.windowBackground),
                headerRowTextSize = a.getDimensionPixelSize(R.styleable.WeekView_headerRowTextSize, context.defaultTextSize),
                headerRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, 10),
                todayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, context.colorAccent),

                // Week number
                showWeekNumber = a.getBoolean(R.styleable.WeekView_showWeekNumber, false),
                weekNumberTextColor = a.getColor(R.styleable.WeekView_weekNumberTextColor, Color.WHITE),
                weekNumberTextSize = a.getDimensionPixelSize(R.styleable.WeekView_weekNumberTextSize, context.defaultTextSize),
                weekNumberBackgroundColor = a.getColor(R.styleable.WeekView_weekNumberBackgroundColor, Color.LTGRAY),
                weekNumberBackgroundCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_weekNumberBackgroundCornerRadius, 0),

                // Event chips
                eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, 0),
                eventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, context.defaultTextSize),
                adaptiveEventTextSize = a.getBoolean(R.styleable.WeekView_adaptiveEventTextSize, false),
                eventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, Color.WHITE),
                defaultEventColor = a.getColor(R.styleable.WeekView_defaultEventColor, context.colorAccent),

                // Event padding
                eventPaddingHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_eventPaddingHorizontal, 8),
                eventPaddingVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventPaddingVertical, 8),

                // Event margins
                columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, 10),
                overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, 0),
                eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, 2),
                eventMarginHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, 0),

                // Colors
                dayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, context.windowBackground),
                todayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, context.windowBackground),
                showDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, false),
                showDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, false),
                pastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, context.windowBackground),
                futureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, context.windowBackground),

                // Hour height
                hourHeight = a.getDimension(R.styleable.WeekView_hourHeight, 50f),
                minHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, 0),
                maxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, 400),
                showCompleteDay = a.getBoolean(R.styleable.WeekView_showCompleteDay, false),

                // Now line
                showNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, false),
                nowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, context.colorAccent),
                nowLineStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_nowLineStrokeWidth, 5),

                // Now line dot
                showNowLineDot = a.getBoolean(R.styleable.WeekView_showNowLineDot, false),
                nowLineDotColor = a.getColor(R.styleable.WeekView_nowLineDotColor, context.colorAccent),
                nowLineDotRadius = a.getDimensionPixelSize(R.styleable.WeekView_nowLineDotRadius, 16),

                // Hour separators
                showHourSeparators = a.getBoolean(R.styleable.WeekView_showHourSeparator, true),
                hourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, context.lineColor),
                hourSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorStrokeWidth, 2),

                // Day separators
                showDaySeparators = a.getBoolean(R.styleable.WeekView_showDaySeparator, true),
                daySeparatorColor = a.getColor(R.styleable.WeekView_daySeparatorColor, context.lineColor),
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
                allDayEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventTextSize, eventTextSize)
                effectiveMinHourHeight = minHourHeight

                // Font
                val fontFamily = a.getString(R.styleable.WeekView_fontFamily)
                val typefaceIndex = a.getInteger(R.styleable.WeekView_typeface, Typeface.NORMAL)
                val textStyle = a.getInteger(R.styleable.WeekView_textStyle, Typeface.NORMAL)
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

private fun Context.resolveColor(@AttrRes resourceId: Int, alpha: Double = 1.0): Int {
    val typedValue = TypedValue()
    val typedArray = obtainStyledAttributes(typedValue.data, intArrayOf(resourceId))
    val color = ColorUtils.setAlphaComponent(typedArray.getColor(0, 0), (alpha * 255).roundToInt())
    typedArray.recycle()
    return color
}

private val Context.colorAccent: Int
    get() = resolveColor(R.attr.colorAccent)

private val Context.lineColor: Int
    get() = resolveColor(android.R.attr.textColorTertiary, alpha = 0.1)

private val Context.shadowColor: Int
    get() = resolveColor(android.R.attr.textColorTertiary, alpha = 0.4)

private val Context.textColorPrimary: Int
    get() = resolveColor(android.R.attr.textColorPrimary)

private val Context.windowBackground: Int
    get() = resolveColor(android.R.attr.windowBackground)

private val Context.defaultTextSize: Int
    get() = TypedValue.applyDimension(COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
