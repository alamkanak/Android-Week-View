package com.alamkanak.weekview

import android.content.Context
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import com.alamkanak.weekview.DateUtils.today
import java.util.*
import java.util.Calendar.*
import kotlin.math.max
import kotlin.math.min


class WeekViewConfigWrapper(context: Context, private val config: WeekViewConfig) {

    var timeTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        textSize = config.timeColumnTextSize.toFloat()
        color = config.timeColumnTextColor
    }

    var timeTextWidth: Float = 0.toFloat()

    var timeTextHeight: Float = 0.toFloat()

    val headerTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = config.headerRowTextColor
        textAlign = Paint.Align.CENTER
        textSize = config.headerRowTextSize.toFloat()
        typeface = Typeface.DEFAULT_BOLD
    }

    val headerRowBottomLinePaint: Paint = Paint().apply {
        color = config.headerRowBottomLineColor
        strokeWidth = config.headerRowBottomLineWidth.toFloat()
    }

    var headerTextHeight: Float = headerTextPaint.descent() - headerTextPaint.ascent()

    var headerHeight: Float = 0.toFloat()

    val todayHeaderTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = config.headerRowTextSize.toFloat()
        typeface = Typeface.DEFAULT_BOLD
        color = config.todayHeaderTextColor
    }

    private var currentAllDayEventHeight: Int = 0

    // Dates in the past have origin.x > 0, dates in the future have origin.x < 0
    var currentOrigin = PointF(0f, 0f)

    var headerBackgroundPaint: Paint = Paint().apply {
        color = config.headerRowBackgroundColor
    }

    var widthPerDay: Float = 0f

    var dayBackgroundPaint: Paint = Paint().apply {
        color = config.dayBackgroundColor
    }

    val hourSeparatorPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = config.hourSeparatorStrokeWidth.toFloat()
        color = config.hourSeparatorColor
    }

    val daySeparatorPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = config.daySeparatorStrokeWidth.toFloat()
        color = config.daySeparatorColor
    }

    val todayBackgroundPaint: Paint = Paint().apply {
        color = config.todayBackgroundColor
    }

    private val futureBackgroundPaint: Paint  = Paint().apply {
        color = config.futureBackgroundColor
    }

    private val pastBackgroundPaint: Paint = Paint().apply {
        color = config.pastBackgroundColor
    }

    private val futureWeekendBackgroundPaint: Paint = Paint().apply {
        color = config.futureWeekendBackgroundColor
    }

    private val pastWeekendBackgroundPaint: Paint = Paint().apply {
        color = config.pastWeekendBackgroundColor
    }

    val timeColumnSeparatorPaint: Paint = Paint().apply {
        color = config.timeColumnSeparatorColor
        strokeWidth = config.timeColumnSeparatorStrokeWidth.toFloat()
    }

    val nowLinePaint: Paint = Paint().apply {
        strokeWidth = config.nowLineStrokeWidth.toFloat()
        color = config.nowLineColor
    }

    val nowDotPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = config.nowLineDotRadius.toFloat()
        color = config.nowLineDotColor
    }

    var timeColumnWidth: Float = 0.toFloat()

    val eventTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
        color = config.eventTextColor
        textSize = config.eventTextSize.toFloat()
    }

    val allDayEventTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
        color = config.eventTextColor
        textSize = config.allDayEventTextSize.toFloat()
    }

    val timeColumnBackgroundPaint: Paint = Paint().apply {
        color = config.timeColumnBackgroundColor
    }

    var hasEventInHeader: Boolean = false

    var newHourHeight = -1f

    lateinit var dateTimeInterpreter: DateTimeInterpreter

    init {
        val rect = Rect()
        timeTextPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
        timeTextHeight = rect.height().toFloat()
        initTextTimeWidth(context)
        refreshHeaderHeight()
    }

    var numberOfVisibleDays: Int
        get() = config.numberOfVisibleDays
        set(value) {
            config.numberOfVisibleDays = value
        }

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

    var showCurrentTimeFirst: Boolean
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
        get() = config.minX

    val maxX: Float
        get() = config.maxX

    val isSingleDay: Boolean
        get() = config.isSingleDay

    var minDate: Calendar?
        get() = config.minDate
        set(value) {
            config.minDate = value
        }

    var maxDate: Calendar?
        get() = config.maxDate
        set(value) {
            config.maxDate = value
        }

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

    var xScrollingSpeed: Float
        get() = config.xScrollingSpeed
        set(value) {
            config.xScrollingSpeed = value
        }

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

    var verticalFlingEnabled: Boolean
        get() = config.verticalFlingEnabled
        set(value) {
            config.verticalFlingEnabled = value
        }

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
        get() = config.firstDayOfWeek
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

    val singleLineHeader: Boolean
        get() = config.singleLineHeader

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
        get() = config.hoursPerDay

    val minutesPerDay: Int
        get() = hoursPerDay * Constants.MINUTES_PER_HOUR

    val startHour: Int
        get() = if (showMidnightHour && showTimeColumnHourSeparator) minHour else timeColumnHoursInterval

    var eventCornerRadius: Int
        get() = config.eventCornerRadius
        set(value) {
            config.eventCornerRadius = value
        }

    var eventPadding: Int
        get() = config.eventPadding
        set(value) {
            config.eventPadding = value
        }

    var defaultEventColor: Int
        get() = config.defaultEventColor
        set(value) {
            config.defaultEventColor
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

    fun setCurrentAllDayEventHeight(height: Int) {
        currentAllDayEventHeight = height
        refreshHeaderHeight()
    }

    fun getCurrentAllDayEventHeight(): Int {
        return currentAllDayEventHeight
    }

    fun moveCurrentOriginIfFirstDraw() {
        // If the week view is being drawn for the first time, then consider the first day of the week.
        val today = today()
        val isWeekView = numberOfVisibleDays >= 7
        val currentDayIsNotToday = today.get(DAY_OF_WEEK) != firstDayOfWeek

        if (isWeekView && currentDayIsNotToday && showFirstDayOfWeekFirst) {
            val difference = computeDifferenceWithFirstDayOfWeek(today)
            currentOrigin.x += (widthPerDay + columnGap) * difference
        }

        if (showCurrentTimeFirst) {
            computeDifferenceWithCurrentTime()
        }

        // Overwrites the origin when today is out of date range
        currentOrigin.x = min(currentOrigin.x, maxX)
        currentOrigin.x = max(currentOrigin.x, minX)
    }

    private fun computeDifferenceWithCurrentTime() {
        val desired = Calendar.getInstance()

        if (desired.get(HOUR_OF_DAY) > 0) {
            // Add some padding above the current time (and thus: the now line)
            desired.add(HOUR_OF_DAY, -1)
        }

        val hour = desired.get(HOUR_OF_DAY)
        val minutes = desired.get(MINUTE)
        val fraction = minutes.toFloat() / Constants.MINUTES_PER_HOUR

        var verticalOffset = hourHeight * (hour + fraction)
        val viewHeight = WeekView.getViewHeight().toDouble()

        val desiredOffset = totalDayHeight - viewHeight
        verticalOffset = min(desiredOffset.toFloat(), verticalOffset)

        currentOrigin.y = verticalOffset * -1
    }

    fun computeDifferenceWithFirstDayOfWeek(date: Calendar): Int {
        return (date.get(DAY_OF_WEEK) + 7 - firstDayOfWeek) % 7
    }

    fun refreshAfterZooming() {
        if (newHourHeight > 0 && !showCompleteDay) {
            newHourHeight = Math.max(newHourHeight, effectiveMinHourHeight.toFloat())
            newHourHeight = Math.min(newHourHeight, maxHourHeight.toFloat())

            // potentialMinHourHeight
            // the minimal height of an hour when zoomed completely out
            // needed to suppress the zooming below 24:00
            val height = WeekView.getViewHeight()
            val potentialMinHourHeight = (height - headerHeight) / hoursPerDay
            newHourHeight = Math.max(newHourHeight, potentialMinHourHeight)

            currentOrigin.y = currentOrigin.y / hourHeight * newHourHeight
            hourHeight = newHourHeight
            newHourHeight = -1f
        }
    }

    fun updateVerticalOrigin() {
        val height = WeekView.getViewHeight()

        // If the new currentOrigin.y is invalid, make it valid.
        val dayHeight = hourHeight * hoursPerDay

        val potentialNewVerticalOrigin = height - (dayHeight + headerHeight)

        currentOrigin.y = max(currentOrigin.y, potentialNewVerticalOrigin)
        currentOrigin.y = min(currentOrigin.y, 0f)
    }

    fun getHeaderBottomPosition(): Float {
        return currentOrigin.y + getTotalHeaderHeight()
    }

    fun getTotalHeaderHeight(): Float {
        return headerHeight + headerRowPadding * 2f
    }

    fun getTotalTimeColumnWidth(): Float {
        return timeTextWidth + timeColumnPadding * 2
    }

    fun resetOrigin() {
        currentOrigin = PointF(0f, 0f)
    }

    fun setTextSize(textSize: Int) {
        todayHeaderTextPaint.textSize = textSize.toFloat()
        headerTextPaint.textSize = textSize.toFloat()
        timeTextPaint.textSize = textSize.toFloat()
    }

    /*fun setHeaderRowTextColor(headerRowTextColor: Int) {
        headerTextPaint.color = headerRowTextColor
    }*/

    /*fun setHeaderRowTextSize(size: Int) {
        headerTextPaint.textSize = size.toFloat()
        todayHeaderTextPaint.textSize = size.toFloat()
    }*/

    /*fun setTimeColumnTextColor(timeColumnTextColor: Int) {
        timeTextPaint.color = timeColumnTextColor
    }*/

    fun setDateTimeInterpreter(dateTimeInterpreter: DateTimeInterpreter, context: Context) {
        this.dateTimeInterpreter = dateTimeInterpreter
        initTextTimeWidth(context)
    }

    fun getPastBackgroundPaint(useWeekendColor: Boolean): Paint {
        return if (useWeekendColor) pastWeekendBackgroundPaint else pastBackgroundPaint
    }

    fun getFutureBackgroundPaint(useWeekendColor: Boolean): Paint {
        return if (useWeekendColor) futureWeekendBackgroundPaint else futureBackgroundPaint
    }

    fun getTodayBackgroundPaint(isToday: Boolean): Paint {
        return if (isToday) todayBackgroundPaint else dayBackgroundPaint
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
            hourHeight = (WeekView.getViewHeight() - headerHeight) / hoursPerDay
            newHourHeight = hourHeight
        }
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private fun initTextTimeWidth(context: Context) {
        val interpreter = getDateTimeInterpreter(context)
        timeTextWidth = 0f

        for (i in 0 until HOUR_OF_DAY) {
            val time = interpreter.interpretTime(i)
            timeTextWidth = max(timeTextWidth, timeTextPaint.measureText(time))
        }
    }

    fun getDateTimeInterpreter(context: Context): DateTimeInterpreter {
        if (this::dateTimeInterpreter.isInitialized.not()) {
            dateTimeInterpreter = DefaultDateTimeInterpreter(context, numberOfVisibleDays)
        }

        return dateTimeInterpreter
    }

}
