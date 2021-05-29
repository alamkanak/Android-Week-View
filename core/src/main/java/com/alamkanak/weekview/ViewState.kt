package com.alamkanak.weekview

import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.TextPaint
import android.view.View
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

internal data class DragState(
    val eventId: Long,
    val dragStartTime: Calendar,
    val draggedEventStartTime: Calendar,
)

internal class ViewState {

    // View
    var viewWidth: Int = 0
    var viewHeight: Int = 0

    var isLtr: Boolean = true

    // Calendar state
    var firstVisibleDate: Calendar = today()
    var pendingScroll: Calendar? = null

    private var isFirstDraw: Boolean = true

    // Calendar configuration
    var numberOfVisibleDays: Int = 3
    var restoreNumberOfVisibleDays: Boolean = true
    var showFirstDayOfWeekFirst: Boolean = false
    var showCurrentTimeFirst: Boolean = false
    var arrangeAllDayEventsVertically: Boolean = true

    // Drawing context
    private var startPixel: Float = 0f
    val startPixels: MutableList<Float> = mutableListOf()
    val dateRange: MutableList<Calendar> = createDateRange(firstVisibleDate).validate(this).toMutableList()
    val dateRangeWithStartPixels: MutableList<Pair<Calendar, Float>> = mutableListOf()

    // Drag & drop
    var dragState: DragState? = null

    // Time column
    var timeColumnPadding: Int = 0
    var timeColumnHoursInterval: Int = 0

    var headerPadding: Float = 0f

    var showWeekNumber: Boolean = false
    var weekNumberBackgroundCornerRadius: Float = 0f

    var eventCornerRadius: Int = 0
    var adaptiveEventTextSize: Boolean = false
    var eventPaddingHorizontal: Int = 0
    var eventPaddingVertical: Int = 0
    var defaultEventColor: Int = 0

    var columnGap: Int = 0
    var overlappingEventGap: Int = 0
    var eventMarginVertical: Int = 0
    var singleDayHorizontalPadding: Int = 0

    var hourHeight: Float = 0f
    var minHourHeight: Float = 0f
    var maxHourHeight: Float = 0f
    var effectiveMinHourHeight: Float = 0f
    var showCompleteDay: Boolean = false

    var showNowLine: Boolean = false
    var showNowLineDot: Boolean = false
    var showHourSeparators: Boolean = false
    var showDaySeparators: Boolean = false
    var showTimeColumnSeparator: Boolean = false
    var showTimeColumnHourSeparators: Boolean = false
    var showHeaderBottomLine: Boolean = false
    var showHeaderBottomShadow: Boolean = false

    var horizontalScrollingEnabled: Boolean = false

    @Deprecated("No longer used")
    var xScrollingSpeed: Float = 0f
    @Deprecated("No longer used")
    var verticalFlingEnabled: Boolean = false
    @Deprecated("No longer used")
    var horizontalFlingEnabled: Boolean = false
    @Deprecated("No longer used")
    var scrollDuration: Int = 0

    var minHour: Int = 0
    var maxHour: Int = 24

    var typeface: Typeface = Typeface.DEFAULT

    var timeColumnWidth: Float = 0f
    var timeColumnTextHeight: Float = 0f

    private val _timeColumnTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    val timeColumnTextPaint: TextPaint
        get() = _timeColumnTextPaint.apply {
            textAlign = if (isLtr) Paint.Align.RIGHT else Paint.Align.LEFT
        }

    val headerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val todayHeaderTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val weekendHeaderTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val headerBottomLinePaint = Paint()

    var dateLabelHeight: Float = 0f

    var headerHeight: Float = 0f

    var currentAllDayEventHeight: Int = 0

    var maxNumberOfAllDayEvents: Int = 0

    var allDayEventsExpanded: Boolean = false

    val showAllDayEventsToggleArrow: Boolean
        get() = arrangeAllDayEventsVertically && maxNumberOfAllDayEvents > 2

    // In LTR: Dates in the past have origin.x > 0, dates in the future have origin.x < 0
    // In RTL: Dates in the past have origin.x < 0, dates in the future have origin.x > 0
    var currentOrigin = PointF(0f, 0f)

    val headerBackgroundPaint = Paint()

    val headerBackgroundWithShadowPaint = Paint()

    val dayWidth: Float
        get() = (viewWidth - timeColumnWidth) / numberOfVisibleDays

    val drawableDayWidth: Float
        get() = dayWidth - columnGap

    val dayBackgroundPaint = Paint()

    val hourSeparatorPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    val daySeparatorPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    var todayBackgroundPaint: Paint? = null

    var futureBackgroundPaint: Paint? = null

    var pastBackgroundPaint: Paint? = null

    var futureWeekendBackgroundPaint: Paint? = null

    var pastWeekendBackgroundPaint: Paint? = null

    val timeColumnSeparatorPaint = Paint()

    val nowLinePaint = Paint()

    val nowDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val eventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val allDayEventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val timeColumnBackgroundPaint = Paint()

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

    val dayHeight: Float
        get() = (hourHeight * hoursPerDay) + headerHeight

    private val _headerBounds: RectF = RectF()

    val headerBounds: RectF
        get() = _headerBounds.apply {
            left = if (isLtr) timeColumnWidth else 0f
            top = 0f
            right = if (isLtr) viewWidth.toFloat() else (viewWidth - timeColumnWidth)
            bottom = headerHeight
        }

    private val _timeColumnBounds: RectF = RectF()

    val timeColumnBounds: RectF
        get() = _timeColumnBounds.apply {
            left = if (isLtr) 0f else (viewWidth - timeColumnWidth)
            top = _headerBounds.bottom
            right = if (isLtr) timeColumnWidth else viewWidth.toFloat()
            bottom = viewHeight.toFloat()
        }

    private val _calendarGridBounds: RectF = RectF()

    val calendarGridBounds: RectF
        get() = _calendarGridBounds.apply {
            left = if (isLtr) timeColumnWidth else 0f
            top = headerHeight
            right = if (isLtr) viewWidth.toFloat() else viewWidth.toFloat() - timeColumnWidth
            bottom = viewHeight.toFloat()
        }

    private val _weekNumberBounds: RectF = RectF()

    val weekNumberBounds: RectF
        get() = _weekNumberBounds.apply {
            left = if (isLtr) 0f else (viewWidth - timeColumnWidth)
            top = 0f
            right = if (isLtr) timeColumnWidth else viewWidth.toFloat()
            bottom = headerPadding + dateLabelHeight + headerPadding
        }

    private val _toggleAllDayEventsAreaBounds: RectF = RectF()

    val toggleAllDayEventsAreaBounds: RectF
        get() = _toggleAllDayEventsAreaBounds.apply {
            left = if (isLtr) 0f else (viewWidth - timeColumnWidth)
            top = weekNumberBounds.bottom
            right = if (isLtr) timeColumnWidth else viewWidth.toFloat()
            bottom = headerHeight
        }

    val weekNumberTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val weekNumberBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    val hoursPerDay: Int
        get() = maxHour - minHour

    val minutesPerDay: Int
        get() = hoursPerDay * 60

    private val timeRange: IntRange
        get() {
            val includeMidnightHour = showTimeColumnHourSeparators
            val padding = if (includeMidnightHour) 0 else timeColumnHoursInterval
            val startHour = minHour + padding
            return startHour until maxHour
        }

    val displayedHours: IntProgression
        get() = timeRange step timeColumnHoursInterval

    private val _firstVisibleHour: Float
        get() = minHour + (currentOrigin.y * -1 / hourHeight)

    private val visibleHours: Float
        get() = calendarGridBounds.height() / hourHeight

    val firstVisibleHour: Int
        get() = _firstVisibleHour.toInt()

    val firstFullyVisibleHour: Int
        get() = ceil(_firstVisibleHour).toInt()

    val lastVisibleHour: Int
        get() = ceil(_firstVisibleHour + visibleHours).toInt()

    val lastFullyVisibleHour: Int
        get() = (_firstVisibleHour + visibleHours).toInt()

    fun getXOriginForDate(date: Calendar): Float {
        return if (isLtr) (date.daysFromToday * dayWidth * -1f) else (date.daysFromToday * dayWidth)
    }

    private fun scrollToFirstDayOfWeek(navigationListener: Navigator.NavigationListener) {
        // If the week view is being drawn for the first time, consider the first day of the week.
        val today = today()
        val isWeekView = numberOfVisibleDays >= 7
        val currentDayIsNotStartOfWeek = today.dayOfWeek != today.firstDayOfWeek

        if (isWeekView && currentDayIsNotStartOfWeek) {
            val difference = today.computeDifferenceWithFirstDayOfWeek()
            val factor = if (isLtr) 1 else -1
            currentOrigin.x += dayWidth * difference * factor
        }

        currentOrigin.x = currentOrigin.x.coerceIn(minimumValue = minX, maximumValue = maxX)
        navigationListener.onHorizontalScrollingFinished()
    }

    private fun renderCurrentTime() {
        val desired = now()
        if (desired.hour > minHour) {
            // Add some padding above the current time (and thus: the now line)
            desired -= Hours(1)
        } else {
            desired -= Minutes(desired.minute)
        }

        desired.hour = desired.hour.coerceIn(minimumValue = minHour, maximumValue = maxHour)

        val fraction = desired.minute / 60f
        val verticalOffset = hourHeight * (desired.hour + fraction)
        val desiredOffset = dayHeight - viewHeight

        currentOrigin.y = min(desiredOffset, verticalOffset) * -1
    }

    /**
     * Returns a valid start date based on the provided [candidate]. If it falls outside the range
     * of [minDate] and [maxDate], it will be adjusted accordingly.
     *
     * @return A [Calendar] of the valid start date
     */
    fun getStartDateInAllowedRange(candidate: Calendar): Calendar {
        val minDate = minDate ?: candidate
        val maxDate = maxDate ?: candidate

        return if (candidate.isBefore(minDate)) {
            minDate
        } else if (candidate.isAfter(maxDate)) {
            maxDate - Days(numberOfVisibleDays - 1)
        } else if (numberOfVisibleDays >= 7 && showFirstDayOfWeekFirst) {
            val diff = candidate.computeDifferenceWithFirstDayOfWeek()
            candidate - Days(diff)
        } else {
            candidate
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
            val effectiveMinHourHeight = max(minHourHeight, newMinHourHeight)

            newHourHeight = newHourHeight.coerceIn(
                minimumValue = effectiveMinHourHeight,
                maximumValue = max(effectiveMinHourHeight, maxHourHeight)
            )

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

    fun getPastBackgroundPaint(date: Calendar): Paint {
        val paint = when {
            date.isToday -> todayBackgroundPaint
            date.isWeekend -> pastWeekendBackgroundPaint
            else -> pastBackgroundPaint
        }
        return paint ?: dayBackgroundPaint
    }

    fun getFutureBackgroundPaint(date: Calendar): Paint {
        val paint = when {
            date.isToday -> todayBackgroundPaint
            date.isWeekend -> futureWeekendBackgroundPaint
            else -> futureBackgroundPaint
        }
        return paint ?: dayBackgroundPaint
    }

    private fun updateHourHeight(viewHeight: Int) {
        hourHeight = (viewHeight - headerHeight) / hoursPerDay
        newHourHeight = hourHeight
    }

    fun calculateHeaderHeight(): Float {
        return if (numberOfVisibleDays > 1) {
            calculateHeaderHeightInMultiDayView()
        } else {
            calculateHeaderHeightInSingleDayView()
        }
    }

    private fun calculateHeaderHeightInSingleDayView(): Float {
        val labelHeight = headerPadding + dateLabelHeight + headerPadding
        var chipsHeight = 0f

        if (maxNumberOfAllDayEvents > 0) {
            val numberOfRows = if (arrangeAllDayEventsVertically && allDayEventsExpanded) {
                maxNumberOfAllDayEvents
            } else if (arrangeAllDayEventsVertically) {
                min(maxNumberOfAllDayEvents, 2)
            } else {
                1
            }

            val heightOfChips = numberOfRows * currentAllDayEventHeight
            val heightOfSpacing = (numberOfRows - 1) * eventMarginVertical
            chipsHeight += heightOfChips + heightOfSpacing

            // Add padding below the event chips
            chipsHeight += headerPadding
        }

        val height = max(labelHeight, chipsHeight)

        return if (showHeaderBottomLine) {
            height + headerBottomLinePaint.strokeWidth
        } else {
            height
        }
    }

    private fun calculateHeaderHeightInMultiDayView(): Float {
        var newHeight = headerPadding + dateLabelHeight + headerPadding

        if (maxNumberOfAllDayEvents > 0) {
            val numberOfRows = if (arrangeAllDayEventsVertically && allDayEventsExpanded) {
                maxNumberOfAllDayEvents
            } else if (arrangeAllDayEventsVertically) {
                min(maxNumberOfAllDayEvents, 2)
            } else {
                1
            }

            val heightOfChips = numberOfRows * currentAllDayEventHeight
            val heightOfSpacing = (numberOfRows - 1) * eventMarginVertical
            newHeight += heightOfChips + heightOfSpacing

            // Add padding below the event chips
            newHeight += headerPadding
        }

        if (showHeaderBottomLine) {
            newHeight += headerBottomLinePaint.strokeWidth
        }

        return newHeight
    }

    fun updateHeaderHeight(height: Float) {
        headerHeight = height

        if (showCompleteDay) {
            // Update the hour height to make sure the day's hours fill the full height of the view
            hourHeight = (viewHeight - headerHeight) / hoursPerDay
            newHourHeight = hourHeight
        }
    }

    fun updateTimeColumnBounds(lineLength: Float, lineHeight: Float) {
        timeColumnTextHeight = lineHeight
        timeColumnWidth = lineLength + timeColumnPadding * 2
    }

    fun update(navigationListener: Navigator.NavigationListener) {
        updateViewState(navigationListener)
        updateScrollState()
        updateDateRange()
    }

    private fun updateScrollState() {
        refreshAfterZooming()
        updateVerticalOrigin()
    }

    private fun updateViewState(navigationListener: Navigator.NavigationListener) {
        if (!isFirstDraw) {
            return
        }

        if (showFirstDayOfWeekFirst) {
            scrollToFirstDayOfWeek(navigationListener)
        }

        if (showCurrentTimeFirst) {
            renderCurrentTime()
        }

        isFirstDraw = false
    }

    private fun updateDateRange() {
        val originX = currentOrigin.x
        val daysFromOrigin = ceil(originX / dayWidth).toInt() * (-1)

        startPixel = if (isLtr) {
            timeColumnWidth + originX + dayWidth * daysFromOrigin
        } else {
            originX + dayWidth * daysFromOrigin
        }

        // If the user is scrolling, a new view becomes partially visible, so we must add an
        // additional date to the date range
        val isNotScrolling = originX % dayWidth == 0f
        val visibleDays = if (isNotScrolling) numberOfVisibleDays else numberOfVisibleDays + 1

        dateRange.clear()

        val startDate = if (isLtr) {
            today() + Days(daysFromOrigin)
        } else {
            today() + Days(numberOfVisibleDays - 1 - daysFromOrigin)
        }

        val newDateRange = createDateRange(startDate, visibleDays)
        dateRange += newDateRange.validate(viewState = this)

        startPixels.clear()
        startPixels += dateRange.indices.map { startPixel + it * dayWidth }

        dateRangeWithStartPixels.clear()
        dateRangeWithStartPixels += dateRange.zip(startPixels)
    }

    fun createDateRange(
        startDate: Calendar,
        visibleDays: Int = numberOfVisibleDays
    ) = if (isLtr) {
        (0 until visibleDays).map { startDate + Days(it) }
    } else {
        (0 until visibleDays).map { startDate - Days(it) }
    }

    fun onSizeChanged(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height

        if (showCompleteDay) {
            updateHourHeight(height)
        }
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        if (Build.VERSION.SDK_INT >= 17) {
            isLtr = newConfig.layoutDirection == View.LAYOUT_DIRECTION_LTR
        }
    }

    fun minutesFromStart(eventStartTime: Calendar): Int {
        val hoursFromStart = eventStartTime.hour - minHour
        return hoursFromStart * 60 + eventStartTime.minute
    }
}
