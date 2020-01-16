package com.alamkanak.weekview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet

internal class WeekViewConfig(
    context: Context,
    attrs: AttributeSet?
) {

    // Calendar configuration
    var firstDayOfWeek: Int? = null
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
    var adaptiveEventTextSize: Boolean = false
    var eventTextColor: Int = 0

    var eventPaddingHorizontal: Int = 0
    var eventPaddingVertical: Int = 0

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
    var hourHeight: Float = 0f
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
    var xScrollingSpeed: Float = 0f
    var verticalFlingEnabled: Boolean = false
    var horizontalFlingEnabled: Boolean = false
    var horizontalScrollingEnabled: Boolean = false
    var scrollDuration: Int = 0

    // Time range
    var minHour: Int = 0
    var maxHour: Int = 0

    // Font
    var typeface: Typeface = Typeface.DEFAULT

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        a.use {
            // Calendar configuration
            if (a.hasValue(R.styleable.WeekView_firstDayOfWeek)) {
                firstDayOfWeek = getInteger(R.styleable.WeekView_firstDayOfWeek, 0)
            }

            numberOfVisibleDays = getInteger(R.styleable.WeekView_numberOfVisibleDays, 3)
            restoreNumberOfVisibleDays = getBoolean(R.styleable.WeekView_restoreNumberOfVisibleDays, true)
            showFirstDayOfWeekFirst = getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, false)
            showCurrentTimeFirst = getBoolean(R.styleable.WeekView_showCurrentTimeFirst, false)

            // Header bottom line
            showHeaderRowBottomLine = getBoolean(R.styleable.WeekView_showHeaderRowBottomLine, false)
            headerRowBottomLineColor = getColor(R.styleable.WeekView_headerRowBottomLineColor, Defaults.GRID_COLOR)
            headerRowBottomLineWidth = getDimensionPixelSize(R.styleable.WeekView_headerRowBottomLineWidth, 1)

            // Time column
            timeColumnTextColor = getColor(R.styleable.WeekView_timeColumnTextColor, Color.BLACK)
            timeColumnBackgroundColor = getColor(R.styleable.WeekView_timeColumnBackgroundColor, Color.WHITE)
            timeColumnPadding = getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, 10)
            timeColumnTextSize = getDimensionPixelSize(R.styleable.WeekView_timeColumnTextSize, Defaults.textSize(context))
            showMidnightHour = getBoolean(R.styleable.WeekView_showMidnightHour, false)
            showTimeColumnHourSeparator = getBoolean(R.styleable.WeekView_showTimeColumnHourSeparator, false)
            timeColumnHoursInterval = getInteger(R.styleable.WeekView_timeColumnHoursInterval, 1)

            // Time column separator
            showTimeColumnSeparator = getBoolean(R.styleable.WeekView_showTimeColumnSeparator, false)
            timeColumnSeparatorColor = getColor(R.styleable.WeekView_timeColumnSeparatorColor, Defaults.GRID_COLOR)
            timeColumnSeparatorStrokeWidth = getDimensionPixelSize(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, 1)

            // Time range
            minHour = getInt(R.styleable.WeekView_minHour, 0)
            maxHour = getInt(R.styleable.WeekView_maxHour, 24)

            // Header row
            headerRowTextColor = getColor(R.styleable.WeekView_headerRowTextColor, Color.BLACK)
            headerRowBackgroundColor = getColor(R.styleable.WeekView_headerRowBackgroundColor, Color.WHITE)
            headerRowTextSize = getDimensionPixelSize(R.styleable.WeekView_headerRowTextSize, Defaults.textSize(context))
            headerRowPadding = getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, 10)
            todayHeaderTextColor = getColor(R.styleable.WeekView_todayHeaderTextColor, Defaults.HIGHLIGHT_COLOR)
            singleLineHeader = getBoolean(R.styleable.WeekView_singleLineHeader, true)

            // Event chips
            eventCornerRadius = getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, 0)
            eventTextSize = getDimensionPixelSize(R.styleable.WeekView_eventTextSize, Defaults.textSize(context))
            adaptiveEventTextSize = getBoolean(R.styleable.WeekView_adaptiveEventTextSize, false)
            eventTextColor = getColor(R.styleable.WeekView_eventTextColor, Color.BLACK)
            defaultEventColor = getColor(R.styleable.WeekView_defaultEventColor, Defaults.EVENT_COLOR)
            allDayEventTextSize = getDimensionPixelSize(R.styleable.WeekView_allDayEventTextSize, eventTextSize)

            // Event padding
            eventPaddingHorizontal = getDimensionPixelSize(R.styleable.WeekView_eventPaddingHorizontal, 8)
            eventPaddingVertical = getDimensionPixelSize(R.styleable.WeekView_eventPaddingVertical, 8)

            // Event margins
            columnGap = getDimensionPixelSize(R.styleable.WeekView_columnGap, 10)
            overlappingEventGap = getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, 0)
            eventMarginVertical = getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, 2)
            eventMarginHorizontal = getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, 0)

            // Colors
            dayBackgroundColor = getColor(R.styleable.WeekView_dayBackgroundColor, Defaults.BACKGROUND_COLOR)
            todayBackgroundColor = getColor(R.styleable.WeekView_todayBackgroundColor, Defaults.BACKGROUND_COLOR)
            showDistinctPastFutureColor = getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, false)
            showDistinctWeekendColor = getBoolean(R.styleable.WeekView_showDistinctWeekendColor, false)
            pastBackgroundColor = getColor(R.styleable.WeekView_pastBackgroundColor, Defaults.PAST_BACKGROUND_COLOR)
            futureBackgroundColor = getColor(R.styleable.WeekView_futureBackgroundColor, Defaults.FUTURE_BACKGROUND_COLOR)
            pastWeekendBackgroundColor = getColor(R.styleable.WeekView_pastWeekendBackgroundColor, pastBackgroundColor)
            futureWeekendBackgroundColor = getColor(R.styleable.WeekView_futureWeekendBackgroundColor, futureBackgroundColor)

            // Hour height
            hourHeight = getDimension(R.styleable.WeekView_hourHeight, 50f)
            minHourHeight = getDimensionPixelSize(R.styleable.WeekView_minHourHeight, 0)
            maxHourHeight = getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, 400)
            effectiveMinHourHeight = minHourHeight
            showCompleteDay = getBoolean(R.styleable.WeekView_showCompleteDay, false)

            // Now line
            showNowLine = getBoolean(R.styleable.WeekView_showNowLine, false)
            nowLineColor = getColor(R.styleable.WeekView_nowLineColor, Defaults.NOW_COLOR)
            nowLineStrokeWidth = getDimensionPixelSize(R.styleable.WeekView_nowLineStrokeWidth, 5)

            // Now line dot
            showNowLineDot = getBoolean(R.styleable.WeekView_showNowLineDot, false)
            nowLineDotColor = getColor(R.styleable.WeekView_nowLineDotColor, Defaults.NOW_COLOR)
            nowLineDotRadius = getDimensionPixelSize(R.styleable.WeekView_nowLineDotRadius, 16)

            // Hour separators
            showHourSeparator = getBoolean(R.styleable.WeekView_showHourSeparator, true)
            hourSeparatorColor = getColor(R.styleable.WeekView_hourSeparatorColor, Defaults.SEPARATOR_COLOR)
            hourSeparatorStrokeWidth = getDimensionPixelSize(R.styleable.WeekView_hourSeparatorStrokeWidth, 2)

            // Day separators
            showDaySeparator = getBoolean(R.styleable.WeekView_showDaySeparator, true)
            daySeparatorColor = getColor(R.styleable.WeekView_daySeparatorColor, Defaults.SEPARATOR_COLOR)
            daySeparatorStrokeWidth = getDimensionPixelSize(R.styleable.WeekView_daySeparatorStrokeWidth, 2)

            // Scrolling
            xScrollingSpeed = getFloat(R.styleable.WeekView_xScrollingSpeed, 1f)
            horizontalFlingEnabled = getBoolean(R.styleable.WeekView_horizontalFlingEnabled, true)
            horizontalScrollingEnabled = getBoolean(R.styleable.WeekView_horizontalScrollingEnabled, true)
            verticalFlingEnabled = getBoolean(R.styleable.WeekView_verticalFlingEnabled, true)
            scrollDuration = getInt(R.styleable.WeekView_scrollDuration, 250)

            // Font
            val fontFamily = getString(R.styleable.WeekView_fontFamily)
            val typeface = getInteger(R.styleable.WeekView_typeface, 0)
            val textStyle = getInteger(R.styleable.WeekView_textStyle, 0)
            setTypefaceFromAttrs(fontFamily, typeface, textStyle)
        }
    }

    private fun setTypefaceFromAttrs(
        familyName: String?,
        typefaceIndex: Int,
        styleIndex: Int
    ) {
        if (familyName != null) {
            val tf = Typeface.create(familyName, styleIndex)
            if (tf != null) {
                typeface = tf
                return
            }
        }

        typeface = when (typefaceIndex) {
            SANS -> Typeface.SANS_SERIF
            SERIF -> Typeface.SERIF
            MONOSPACE -> Typeface.MONOSPACE
            else -> typeface
        }
    }

    private companion object {
        private const val SANS = 1
        private const val SERIF = 2
        private const val MONOSPACE = 3
    }
}

private fun TypedArray.use(block: TypedArray.() -> Unit) {
    try {
        block()
    } finally {
        recycle()
    }
}
