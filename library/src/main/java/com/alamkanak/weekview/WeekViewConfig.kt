package com.alamkanak.weekview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import org.threeten.bp.DayOfWeek

internal class WeekViewConfig(context: Context, attrs: AttributeSet) {

    // Calendar configuration
    var firstDayOfWeek: Int = 0
    var numberOfVisibleDays: Int = 0
    var restoreNumberOfVisibleDays: Boolean = true
    var showFirstDayOfWeekFirst: Boolean = false
    var showCurrentTimeFirst: Boolean = false

    // Header bottom line
    var showHeaderRowBottomLine: Boolean = false
    var headerRowBottomLineColor: Int = 0
    var headerRowBottomLineWidth: Int = 0

    // Time column
    var timeColumnTextColor: Int = 0
    var timeColumnBackgroundColor: Int = 0
    var timeColumnPadding: Int = 0
    var timeColumnTextSize: Int = 0
    var showMidnightHour: Boolean = false
    var showTimeColumnHourSeparator: Boolean = false
    var timeColumnHoursInterval: Int = 0

    // Time column separator
    var showTimeColumnSeparator: Boolean = false
    var timeColumnSeparatorColor: Int = 0
    var timeColumnSeparatorStrokeWidth: Int = 0

    // Header row
    var headerRowTextColor: Int = 0
    var headerRowBackgroundColor: Int = 0
    var headerRowTextSize: Int = 0
    var headerRowPadding: Int = 0
    var todayHeaderTextColor: Int = 0
    var singleLineHeader: Boolean = false

    // Event chips
    var eventCornerRadius: Int = 0
    var eventTextSize: Int = 0
    var eventTextColor: Int = 0
    var eventPadding: Int = 0
    var defaultEventColor: Int = 0
    var allDayEventTextSize: Int = 0

    // Event margins
    var columnGap: Int = 0
    var overlappingEventGap: Int = 0
    var eventMarginVertical: Int = 0
    var eventMarginHorizontal: Int = 0

    // Colors
    var dayBackgroundColor: Int = 0
    var todayBackgroundColor: Int = 0
    var showDistinctWeekendColor: Boolean = false
    var showDistinctPastFutureColor: Boolean = false
    var pastBackgroundColor: Int = 0
    var futureBackgroundColor: Int = 0
    var pastWeekendBackgroundColor: Int = 0
    var futureWeekendBackgroundColor: Int = 0

    // Hour height
    var hourHeight: Float = 0.toFloat()
    var minHourHeight: Int = 0
    var maxHourHeight: Int = 0
    var effectiveMinHourHeight: Int = 0
    var showCompleteDay: Boolean = false

    // Now line
    var showNowLine: Boolean = false
    var nowLineColor: Int = 0
    var nowLineStrokeWidth: Int = 0

    // Now line dot
    var showNowLineDot: Boolean = false
    var nowLineDotColor: Int = 0
    var nowLineDotRadius: Int = 0

    // Hour separators
    var showHourSeparator: Boolean = false
    var hourSeparatorColor: Int = 0
    var hourSeparatorStrokeWidth: Int = 0

    // Day separators
    var showDaySeparator: Boolean = false
    var daySeparatorColor: Int = 0
    var daySeparatorStrokeWidth: Int = 0

    // Scrolling
    var xScrollingSpeed: Float = 0.toFloat()
    var verticalFlingEnabled: Boolean = false
    var horizontalFlingEnabled: Boolean = false
    var horizontalScrollingEnabled: Boolean = false
    var scrollDuration: Int = 0

    // Time range
    var minHour: Int = 0
    var maxHour: Int = 0

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        try {
            // Calendar configuration
            firstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, DayOfWeek.MONDAY.value)
            numberOfVisibleDays = a.getInteger(R.styleable.WeekView_numberOfVisibleDays, 3)
            restoreNumberOfVisibleDays = a.getBoolean(R.styleable.WeekView_restoreNumberOfVisibleDays, true)
            showFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, false)
            showCurrentTimeFirst = a.getBoolean(R.styleable.WeekView_showCurrentTimeFirst, false)

            // Header bottom line
            showHeaderRowBottomLine = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomLine, false)
            headerRowBottomLineColor = a.getColor(R.styleable.WeekView_headerRowBottomLineColor, Defaults.GRID_COLOR)
            headerRowBottomLineWidth = a.getDimensionPixelSize(R.styleable.WeekView_headerRowBottomLineWidth, 1)

            // Time column
            timeColumnTextColor = a.getColor(R.styleable.WeekView_timeColumnTextColor, Color.BLACK)
            timeColumnBackgroundColor = a.getColor(R.styleable.WeekView_timeColumnBackgroundColor, Color.WHITE)
            timeColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, 10)
            timeColumnTextSize = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnTextSize, Defaults.textSize(context))
            showMidnightHour = a.getBoolean(R.styleable.WeekView_showMidnightHour, false)
            showTimeColumnHourSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnHourSeparator, false)
            timeColumnHoursInterval = a.getInteger(R.styleable.WeekView_timeColumnHoursInterval, 1)

            // Time column separator
            showTimeColumnSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnSeparator, false)
            timeColumnSeparatorColor = a.getColor(R.styleable.WeekView_timeColumnSeparatorColor, Defaults.GRID_COLOR)
            timeColumnSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, 1)

            // Time range
            minHour = a.getInt(R.styleable.WeekView_minHour, 0)
            maxHour = a.getInt(R.styleable.WeekView_maxHour, 24)

            // Header row
            headerRowTextColor = a.getColor(R.styleable.WeekView_headerRowTextColor, Color.BLACK)
            headerRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, Color.WHITE)
            headerRowTextSize = a.getDimensionPixelSize(R.styleable.WeekView_headerRowTextSize, Defaults.textSize(context))
            headerRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, 10)
            todayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, Defaults.HIGHLIGHT_COLOR)
            singleLineHeader = a.getBoolean(R.styleable.WeekView_singleLineHeader, true)

            // Event chips
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, 0)
            eventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, Defaults.textSize(context))
            eventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, Color.BLACK)
            eventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, 8)
            defaultEventColor = a.getColor(R.styleable.WeekView_defaultEventColor, Defaults.EVENT_COLOR)
            allDayEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventTextSize, eventTextSize)

            // Event margins
            columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, 10)
            overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, 0)
            eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, 3)
            eventMarginHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, 0)

            // Colors
            dayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, Defaults.BACKGROUND_COLOR)
            todayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, Defaults.BACKGROUND_COLOR)
            showDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, false)
            showDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, false)
            pastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, Defaults.PAST_BACKGROUND_COLOR)
            futureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, Defaults.FUTURE_BACKGROUND_COLOR)
            pastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, pastBackgroundColor)
            futureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, futureBackgroundColor)

            // Hour height
            hourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, 50).toFloat()
            minHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, 0)
            maxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, 250)
            effectiveMinHourHeight = minHourHeight
            showCompleteDay = a.getBoolean(R.styleable.WeekView_showCompleteDay, false)

            // Now line
            showNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, false)
            nowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, Defaults.NOW_COLOR)
            nowLineStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_nowLineStrokeWidth, 5)

            // Now line dot
            showNowLineDot = a.getBoolean(R.styleable.WeekView_showNowLineDot, false)
            nowLineDotColor = a.getColor(R.styleable.WeekView_nowLineDotColor, Defaults.NOW_COLOR)
            nowLineDotRadius = a.getDimensionPixelSize(R.styleable.WeekView_nowLineDotRadius, 16)

            // Hour separators
            showHourSeparator = a.getBoolean(R.styleable.WeekView_showHourSeparator, true)
            hourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, Defaults.SEPARATOR_COLOR)
            hourSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorStrokeWidth, 2)

            // Day separators
            showDaySeparator = a.getBoolean(R.styleable.WeekView_showDaySeparator, true)
            daySeparatorColor = a.getColor(R.styleable.WeekView_daySeparatorColor, Defaults.SEPARATOR_COLOR)
            daySeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_daySeparatorStrokeWidth, 2)

            // Scrolling
            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, 1f)
            horizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, true)
            horizontalScrollingEnabled = a.getBoolean(R.styleable.WeekView_horizontalScrollingEnabled, true)
            verticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, true)
            scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, 250)
        } finally {
            a.recycle()
        }
    }

}
