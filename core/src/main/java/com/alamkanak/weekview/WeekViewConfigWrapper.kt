package com.alamkanak.weekview

import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.LINEAR_TEXT_FLAG
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import com.alamkanak.weekview.Constants.UNINITIALIZED
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

typealias DateFormatter = (Calendar) -> String
typealias TimeFormatter = (Int) -> String

internal class WeekViewConfigWrapper(
    private val view: WeekView<*>,
    private val config: WeekViewConfig
) {

    private val _timeTextPaint: TextPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
    }

    val timeTextPaint: TextPaint
        get() = _timeTextPaint.apply {
            textAlign = Paint.Align.RIGHT
            textSize = config.timeColumnTextSize.toFloat()
            color = config.timeColumnTextColor
            typeface = config.typeface
        }

    var timeTextWidth: Float = 0.toFloat()

    var timeTextHeight: Float = 0.toFloat()

    private val _headerTextPaint: TextPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val headerTextPaint: TextPaint
        get() = _headerTextPaint.apply {
            color = config.headerRowTextColor
            textSize = config.headerRowTextSize.toFloat()
            typeface = Typeface.create(config.typeface, Typeface.BOLD)
        }

    private val _headerRowBottomLinePaint: Paint = Paint()

    val headerRowBottomLinePaint: Paint
        get() = _headerRowBottomLinePaint.apply {
            color = config.headerRowBottomLineColor
            strokeWidth = config.headerRowBottomLineWidth.toFloat()
        }

    var headerTextHeight: Float = headerTextPaint.descent() - headerTextPaint.ascent()

    var headerHeight: Float = 0.toFloat()

    private val _todayHeaderTextPaint: TextPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val todayHeaderTextPaint: TextPaint
        get() = _todayHeaderTextPaint.apply {
            textSize = config.headerRowTextSize.toFloat()
            typeface = Typeface.create(config.typeface, Typeface.BOLD)
            color = config.todayHeaderTextColor
        }

    private var currentAllDayEventHeight: Int = 0

    // Dates in the past have origin.x > 0, dates in the future have origin.x < 0
    var currentOrigin = PointF(0f, 0f)

    var headerBackgroundPaint: Paint = Paint().apply {
        color = config.headerRowBackgroundColor
    }

    var widthPerDay: Float = 0f

    private val _dayBackgroundPaint: Paint = Paint()

    val dayBackgroundPaint: Paint
        get() = _dayBackgroundPaint.apply {
            color = config.dayBackgroundColor
        }

    private val _hourSeparatorPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
    }

    val hourSeparatorPaint: Paint
        get() = _hourSeparatorPaint.apply {
            strokeWidth = config.hourSeparatorStrokeWidth.toFloat()
            color = config.hourSeparatorColor
        }

    private val _daySeparatorPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
    }

    val daySeparatorPaint: Paint
        get() = _daySeparatorPaint.apply {
            strokeWidth = config.daySeparatorStrokeWidth.toFloat()
            color = config.daySeparatorColor
        }

    private val _todayBackgroundPaint: Paint = Paint()

    val todayBackgroundPaint: Paint
        get() = _todayBackgroundPaint.apply {
            color = config.todayBackgroundColor
        }

    private val _futureBackgroundPaint = Paint()

    val futureBackgroundPaint: Paint
        get() = _futureBackgroundPaint.apply {
            color = config.futureBackgroundColor
        }

    private val _pastBackgroundPaint = Paint()

    val pastBackgroundPaint: Paint
        get() = _pastBackgroundPaint.apply {
            color = config.pastBackgroundColor
        }

    private val _futureWeekendBackgroundPaint = Paint()

    val futureWeekendBackgroundPaint: Paint
        get() = _futureWeekendBackgroundPaint.apply {
            color = config.futureWeekendBackgroundColor
        }

    private val _pastWeekendBackgroundPaint = Paint()

    val pastWeekendBackgroundPaint: Paint
        get() = _pastWeekendBackgroundPaint.apply {
            color = config.pastWeekendBackgroundColor
        }

    private val _timeColumnSeparatorPaint = Paint()

    val timeColumnSeparatorPaint: Paint
        get() = _timeColumnSeparatorPaint.apply {
            color = config.timeColumnSeparatorColor
            strokeWidth = config.timeColumnSeparatorStrokeWidth.toFloat()
        }

    private val _nowLinePaint = Paint()

    val nowLinePaint: Paint
        get() = _nowLinePaint.apply {
            strokeWidth = config.nowLineStrokeWidth.toFloat()
            color = config.nowLineColor
        }

    private val _nowDotPaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val nowDotPaint: Paint
        get() = _nowDotPaint.apply {
            strokeWidth = config.nowLineDotRadius.toFloat()
            color = config.nowLineDotColor
        }

    var timeColumnWidth: Float = UNINITIALIZED

    private val _eventTextPaint: TextPaint = TextPaint(ANTI_ALIAS_FLAG or LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val eventTextPaint: TextPaint
        get() = _eventTextPaint.apply {
            color = config.eventTextColor
            textSize = config.eventTextSize.toFloat()
            typeface = config.typeface
        }

    var adaptiveEventTextSize: Boolean
        get() = config.adaptiveEventTextSize
        set(value) {
            config.adaptiveEventTextSize = value
        }

    private val _allDayEventTextPaint: TextPaint = TextPaint(ANTI_ALIAS_FLAG or LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val allDayEventTextPaint: TextPaint
        get() = _allDayEventTextPaint.apply {
            color = config.eventTextColor
            textSize = config.allDayEventTextSize.toFloat()
            typeface = config.typeface
        }

    private val _timeColumnBackgroundPaint: Paint = Paint()

    val timeColumnBackgroundPaint: Paint
        get() = _timeColumnBackgroundPaint.apply {
            color = config.timeColumnBackgroundColor
        }

    var hasEventInHeader: Boolean = false

    var newHourHeight: Float = UNINITIALIZED

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

    var numberOfVisibleDays: Int
        get() = config.numberOfVisibleDays
        set(value) {
            config.numberOfVisibleDays = value
        }

    val restoreNumberOfVisibleDays: Boolean
        get() = config.restoreNumberOfVisibleDays

    var hourHeight: Float
        get() = config.hourHeight
        set(value) {
            config.hourHeight = value
        }

    var minHourHeight: Int
        get() = config.minHourHeight
        set(value) {
            config.minHourHeight = value
        }

    var maxHourHeight: Int
        get() = config.maxHourHeight
        set(value) {
            config.maxHourHeight = value
        }

    private var showCurrentTimeFirst: Boolean
        get() = config.showCurrentTimeFirst
        set(value) {
            config.showCurrentTimeFirst = value
        }

    var showNowLine: Boolean
        get() = config.showNowLine
        set(value) {
            config.showNowLine = value
        }

    var showNowLineDot: Boolean
        get() = config.showNowLineDot
        set(value) {
            config.showNowLineDot = value
        }

    var showHourSeparators: Boolean
        get() = config.showHourSeparator
        set(value) {
            config.showHourSeparator = value
        }

    var showDaySeparators: Boolean
        get() = config.showDaySeparator
        set(value) {
            config.showDaySeparator = value
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

    var minHour: Int
        get() = config.minHour
        set(value) {
            config.minHour = value
        }

    var maxHour: Int
        get() = config.maxHour
        set(value) {
            config.maxHour = value
        }

    @Deprecated("No longer used")
    var xScrollingSpeed: Float
        get() = config.xScrollingSpeed
        set(value) {
            config.xScrollingSpeed = value
        }

    @Deprecated("No longer used")
    var horizontalFlingEnabled: Boolean
        get() = config.horizontalFlingEnabled
        set(value) {
            config.horizontalFlingEnabled = value
        }

    var horizontalScrollingEnabled: Boolean
        get() = config.horizontalScrollingEnabled
        set(value) {
            config.horizontalScrollingEnabled = value
        }

    @Deprecated("No longer used")
    var verticalFlingEnabled: Boolean
        get() = config.verticalFlingEnabled
        set(value) {
            config.verticalFlingEnabled = value
        }

    @Deprecated("No longer used")
    var scrollDuration: Int
        get() = config.scrollDuration
        set(value) {
            config.scrollDuration = value
        }

    val totalDayHeight: Float
        get() {
            val dayHeight = hourHeight * hoursPerDay
            return dayHeight + headerHeight
        }

    val totalDayWidth: Float
        get() = widthPerDay + columnGap

    var effectiveMinHourHeight: Int
        get() = config.effectiveMinHourHeight
        set(value) {
            config.effectiveMinHourHeight = value
        }

    var timeColumnPadding: Int
        get() = config.timeColumnPadding
        set(value) {
            config.timeColumnPadding = value
        }

    var columnGap: Int
        get() = config.columnGap
        set(value) {
            config.columnGap = value
        }

    var firstDayOfWeek: Int
        get() = config.firstDayOfWeek ?: now().firstDayOfWeek
        set(value) {
            config.firstDayOfWeek = value
        }

    var showFirstDayOfWeekFirst: Boolean
        get() = config.showFirstDayOfWeekFirst
        set(value) {
            config.showFirstDayOfWeekFirst = value
        }

    var showHeaderRowBottomLine: Boolean
        get() = config.showHeaderRowBottomLine
        set(value) {
            config.showHeaderRowBottomLine = value
        }

    val headerRowBottomLineWidth: Float
        get() = if (showHeaderRowBottomLine) headerRowBottomLinePaint.strokeWidth else 0f

    var showHeaderRowBottomShadow: Boolean
        get() = config.showHeaderRowBottomShadow
        set(value) {
            config.showHeaderRowBottomShadow = value
        }

    var headerRowBottomShadowColor: Int
        get() = config.headerRowBottomShadowColor
        set(value) {
            config.headerRowBottomShadowColor = value
        }

    var headerRowBottomShadowRadius: Int
        get() = config.headerRowBottomShadowRadius
        set(value) {
            config.headerRowBottomShadowRadius = value
        }

    var showWeekNumber: Boolean
        get() = config.showWeekNumber
        set(value) {
            config.showWeekNumber = value
        }

    var weekNumberTextColor: Int
        get() = config.weekNumberTextColor
        set(value) {
            config.weekNumberTextColor = value
        }
    var weekNumberTextSize: Int
        get() = config.weekNumberTextSize
        set(value) {
            config.weekNumberTextSize = value
        }
    var weekNumberBackgroundColor: Int
        get() = config.weekNumberBackgroundColor
        set(value) {
            config.weekNumberBackgroundColor = value
        }
    var weekNumberBackgroundCornerRadius: Int
        get() = config.weekNumberBackgroundCornerRadius
        set(value) {
            config.weekNumberBackgroundCornerRadius = value
        }

    private val _weekNumberBounds: RectF = RectF()

    val weekNumberBounds: RectF
        get() = _weekNumberBounds.apply {
            left = 0f
            top = 0f
            right = timeColumnWidth
            bottom = headerHeight
        }

    private val _weekNumberTextPaint: Paint = TextPaint(ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val weekNumberTextPaint: Paint
        get() = _weekNumberTextPaint.apply {
            color = weekNumberTextColor
            textSize = weekNumberTextSize.toFloat()
            typeface = this@WeekViewConfigWrapper.typeface
        }

    private val _weekNumberBackgroundPaint: Paint = Paint(ANTI_ALIAS_FLAG)

    val weekNumberBackgroundPaint: Paint
        get() = _weekNumberBackgroundPaint.apply {
            color = weekNumberBackgroundColor
        }

    var todayHeaderTextColor: Int
        get() = config.todayHeaderTextColor
        set(value) {
            todayHeaderTextPaint.color = value
            config.todayHeaderTextColor = value
        }

    var timeColumnTextColor: Int
        get() = config.timeColumnTextColor
        set(value) {
            timeTextPaint.color = value
            config.timeColumnTextColor = value
        }

    var showCompleteDay: Boolean
        get() = config.showCompleteDay
        set(value) {
            config.showCompleteDay = value
        }

    var timeColumnBackgroundColor: Int
        get() = config.timeColumnBackgroundColor
        set(value) {
            timeColumnBackgroundPaint.color = value
            config.timeColumnBackgroundColor = value
        }

    var timeColumnTextSize: Int
        get() = config.timeColumnTextSize
        set(value) {
            config.timeColumnTextSize = value
        }

    var showMidnightHour: Boolean
        get() = config.showMidnightHour
        set(value) {
            config.showMidnightHour = value
        }

    var showTimeColumnHourSeparator: Boolean
        get() = config.showTimeColumnHourSeparator
        set(value) {
            config.showTimeColumnHourSeparator = value
        }

    var timeColumnHoursInterval: Int
        get() = config.timeColumnHoursInterval
        set(value) {
            config.timeColumnHoursInterval = value
        }

    var showTimeColumnSeparator: Boolean
        get() = config.showTimeColumnSeparator
        set(value) {
            config.showTimeColumnSeparator = value
        }

    var timeColumnSeparatorColor: Int
        get() = config.timeColumnSeparatorColor
        set(value) {
            config.timeColumnSeparatorColor = value
        }

    var timeColumnSeparatorStrokeWidth: Int
        get() = config.timeColumnSeparatorStrokeWidth
        set(value) {
            config.timeColumnSeparatorStrokeWidth = value
        }

    var headerRowPadding: Int
        get() = config.headerRowPadding
        set(value) {
            config.headerRowPadding = value
        }

    var headerRowBackgroundColor: Int
        get() = config.headerRowBackgroundColor
        set(value) {
            config.headerRowBackgroundColor = value
            headerBackgroundPaint.color = value
        }

    var headerRowTextColor: Int
        get() = config.headerRowTextColor
        set(value) {
            config.headerRowTextColor = value
            headerTextPaint.color = value
        }

    var headerRowTextSize: Int
        get() = config.headerRowTextSize
        set(value) {
            config.headerRowTextSize = value
            headerTextPaint.textSize = value.toFloat()
            todayHeaderTextPaint.textSize = value.toFloat()
        }

    val hoursPerDay: Int
        get() = config.maxHour - config.minHour

    val minutesPerDay: Int
        get() = (hoursPerDay * Constants.MINUTES_PER_HOUR).toInt()

    val timeRange: IntRange
        get() {
            val includeMidnightHour = showTimeColumnHourSeparator && showMidnightHour
            val padding = if (includeMidnightHour) 0 else timeColumnHoursInterval
            val startHour = minHour + padding
            return startHour until maxHour
        }

    var eventCornerRadius: Int
        get() = config.eventCornerRadius
        set(value) {
            config.eventCornerRadius = value
        }

    var eventPaddingHorizontal: Int
        get() = config.eventPaddingHorizontal
        set(value) {
            config.eventPaddingHorizontal = value
        }

    var eventPaddingVertical: Int
        get() = config.eventPaddingVertical
        set(value) {
            config.eventPaddingVertical = value
        }

    var defaultEventColor: Int
        get() = config.defaultEventColor
        set(value) {
            config.defaultEventColor = value
        }

    var overlappingEventGap: Int
        get() = config.overlappingEventGap
        set(value) {
            config.overlappingEventGap = value
        }

    var eventMarginVertical: Int
        get() = config.eventMarginVertical
        set(value) {
            config.eventMarginVertical = value
        }

    var eventMarginHorizontal: Int
        get() = config.eventMarginHorizontal
        set(value) {
            config.eventMarginHorizontal = value
        }

    var showDistinctWeekendColor: Boolean
        get() = config.showDistinctWeekendColor
        set(value) {
            config.showDistinctWeekendColor = value
        }

    var showDistinctPastFutureColor: Boolean
        get() = config.showDistinctPastFutureColor
        set(value) {
            config.showDistinctPastFutureColor = value
        }

    var typeface: Typeface
        get() = config.typeface
        set(value) {
            config.typeface = value
        }

    fun update() {
        refreshAfterZooming()
        updateVerticalOrigin()
    }

    fun calculateTimeColumnWidth() {
        timeColumnWidth = timeTextWidth + timeColumnPadding * 2
    }

    fun calculateWidthPerDay() {
        val viewWidth = view.width.toFloat()
        val availableWidth = viewWidth - timeColumnWidth - columnGap * numberOfVisibleDays
        widthPerDay = availableWidth / numberOfVisibleDays
    }

    fun getXOriginForDate(date: Calendar): Float {
        return -1f * date.daysFromToday * totalDayWidth
    }

    fun updateAllDayEventHeight(height: Int) {
        currentAllDayEventHeight = height
        refreshHeaderHeight()
    }

    fun moveCurrentOriginIfFirstDraw() {
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
            scrollToCurrentTime(view)
        }

        // Overwrites the origin when today is out of date range
        currentOrigin.x = min(currentOrigin.x, maxX)
        currentOrigin.x = max(currentOrigin.x, minX)
    }

    private fun scrollToCurrentTime(view: WeekView<*>) {
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
        val desiredOffset = totalDayHeight - view.height

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

        val height = view.height
        val dayHeight = hourHeight * hoursPerDay

        val isNotFillingEntireHeight = dayHeight < height
        val didZoom = newHourHeight > 0

        if (isNotFillingEntireHeight || didZoom) {
            newHourHeight = max(newHourHeight, effectiveMinHourHeight.toFloat())
            newHourHeight = min(newHourHeight, maxHourHeight.toFloat())

            // Compute a minimum hour height so that users can't zoom out further
            // than the desired hours per day
            val minHourHeight = (height - headerHeight) / hoursPerDay
            newHourHeight = max(newHourHeight, minHourHeight)

            currentOrigin.y = currentOrigin.y / hourHeight * newHourHeight
            hourHeight = newHourHeight
            newHourHeight = UNINITIALIZED
        }
    }

    private fun updateVerticalOrigin() {
        // If the new currentOrigin.y is invalid, make it valid.
        val dayHeight = hourHeight * hoursPerDay
        val potentialNewVerticalOrigin = view.height - (dayHeight + headerHeight)

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

    fun updateHourHeight(viewHeight: Int) {
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
            hourHeight = (view.height - headerHeight) / hoursPerDay
            newHourHeight = hourHeight
        }
    }

    /**
     * Initializes the time column width with the widest hour label.
     */
//    private fun initTextTimeWidth() {
//        timeTextWidth = (0 until hoursPerDay)
//            .map { _timeFormatter(it) }
//            .map { timeTextPaint.measureText(it) }
//            .max() ?: 0f
//    }

    private fun initTimeColumnTextBounds() {
        val textLayouts = timeRange
            .map { _timeFormatter(it) }
            .map { it.toTextLayout(timeTextPaint, width = Int.MAX_VALUE) }

        timeTextWidth = textLayouts.map { it.maxLineLength }.max() ?: 0f
        timeTextHeight = textLayouts.map { it.height.toFloat() }.max() ?: 0f
    }
}
