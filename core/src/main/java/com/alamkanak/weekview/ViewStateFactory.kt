package com.alamkanak.weekview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

internal object ViewStateFactory {

    fun create(context: Context, attrs: AttributeSet?): ViewState {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        val viewState = ViewState()

        val customTypeface = a.getCustomTypeface()
        viewState.typeface = customTypeface ?: Typeface.DEFAULT

        viewState.timeColumnTextPaint.apply {
            textSize = a.getDimension(R.styleable.WeekView_timeColumnTextSize, context.defaultTextSize)
            color = a.getColor(R.styleable.WeekView_timeColumnTextColor, context.textColorPrimary)
            typeface = customTypeface ?: Typeface.DEFAULT
        }

        val headerTextTypeface = customTypeface?.let {
            Typeface.create(it, Typeface.NORMAL)
        } ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)

        viewState.headerRowTextPaint.apply {
            color = a.getColor(R.styleable.WeekView_headerRowTextColor, context.textColorPrimary)
            textSize = a.getDimension(R.styleable.WeekView_headerRowTextSize, context.defaultTextSize)
            typeface = headerTextTypeface
        }

        viewState.headerRowBottomLinePaint.apply {
            color = a.getColor(R.styleable.WeekView_headerRowBottomLineColor, context.lineColor)
            strokeWidth = a.getDimension(R.styleable.WeekView_headerRowBottomLineWidth, 1f)
        }

        viewState.todayHeaderTextPaint.apply {
            color = a.getColor(R.styleable.WeekView_todayHeaderTextColor, context.colorAccent)
            textSize = a.getDimension(R.styleable.WeekView_headerRowTextSize, context.defaultTextSize)
            typeface = headerTextTypeface
        }

        viewState.headerRowBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, context.windowBackground)
        }

        viewState.headerRowBackgroundWithShadowPaint.apply {
            color = viewState.headerRowBackgroundPaint.color
            val shadowColor = a.getColor(R.styleable.WeekView_headerRowBottomShadowColor, context.shadowColor)
            val shadowRadius = a.getDimension(R.styleable.WeekView_headerRowBottomShadowRadius, 4f)
            setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        }

        viewState.dayBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_dayBackgroundColor, context.windowBackground)
        }

        viewState.hourSeparatorPaint.apply {
            color = a.getColor(R.styleable.WeekView_hourSeparatorColor, context.lineColor)
            strokeWidth = a.getDimension(R.styleable.WeekView_hourSeparatorStrokeWidth, 2f)
        }

        viewState.daySeparatorPaint.apply {
            color = a.getColor(R.styleable.WeekView_daySeparatorColor, context.lineColor)
            strokeWidth = a.getDimension(R.styleable.WeekView_daySeparatorStrokeWidth, 2f)
        }

        viewState.todayBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_todayBackgroundColor, context.windowBackground)
        }

        viewState.pastBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_pastBackgroundColor, context.windowBackground)
        }

        viewState.futureBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_futureBackgroundColor, context.windowBackground)
        }

        viewState.pastWeekendBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, viewState.pastBackgroundPaint.color)
        }

        viewState.futureWeekendBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, viewState.futureBackgroundPaint.color)
        }

        viewState.timeColumnSeparatorPaint.apply {
            color = a.getColor(R.styleable.WeekView_timeColumnSeparatorColor, context.lineColor)
            strokeWidth = a.getDimension(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, 1f)
        }

        viewState.nowLinePaint.apply {
            color = a.getColor(R.styleable.WeekView_nowLineColor, context.colorAccent)
            strokeWidth = a.getDimension(R.styleable.WeekView_nowLineStrokeWidth, 5f)
        }

        viewState.nowDotPaint.apply {
            color = a.getColor(R.styleable.WeekView_nowLineDotColor, context.colorAccent)
            strokeWidth = a.getDimension(R.styleable.WeekView_nowLineDotRadius, 16f)
        }

        viewState.eventTextPaint.apply {
            color = a.getColor(R.styleable.WeekView_eventTextColor, Color.WHITE)
            textSize = a.getDimension(R.styleable.WeekView_eventTextSize, context.defaultTextSize)
            typeface = customTypeface ?: Typeface.DEFAULT
        }

        viewState.allDayEventTextPaint.apply {
            color = a.getColor(R.styleable.WeekView_eventTextColor, Color.WHITE)
            textSize = a.getDimension(R.styleable.WeekView_allDayEventTextSize, viewState.eventTextPaint.textSize)
            typeface = customTypeface ?: Typeface.DEFAULT
        }

        viewState.timeColumnBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_timeColumnBackgroundColor, context.windowBackground)
        }

        viewState.weekNumberTextPaint.apply {
            color = a.getColor(R.styleable.WeekView_weekNumberTextColor, Color.WHITE)
            textSize = a.getDimension(R.styleable.WeekView_weekNumberTextSize, context.defaultTextSize)
            typeface = customTypeface ?: Typeface.DEFAULT
        }

        viewState.weekNumberBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_weekNumberBackgroundColor, Color.LTGRAY)
        }

        viewState.apply {
            numberOfVisibleDays = a.getInt(R.styleable.WeekView_numberOfVisibleDays, 3)
            restoreNumberOfVisibleDays = a.getBoolean(R.styleable.WeekView_restoreNumberOfVisibleDays, true)
            showFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, false)
            showCurrentTimeFirst = a.getBoolean(R.styleable.WeekView_showCurrentTimeFirst, false)
        }

        viewState.apply {
            showHeaderRowBottomLine = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomLine, false)
            showHeaderRowBottomShadow = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomShadow, false)
        }

        viewState.apply {
            timeColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, 10)
            showMidnightHour = a.getBoolean(R.styleable.WeekView_showMidnightHour, false)
            showTimeColumnHourSeparators = a.getBoolean(R.styleable.WeekView_showTimeColumnHourSeparator, false)
            timeColumnHoursInterval = a.getInteger(R.styleable.WeekView_timeColumnHoursInterval, 1)
        }

        viewState.apply {
            showTimeColumnSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnSeparator, false)
        }

        viewState.apply {
            minHour = a.getInt(R.styleable.WeekView_minHour, 0)
            maxHour = a.getInt(R.styleable.WeekView_maxHour, 24)
        }

        viewState.apply {
            headerRowPadding = a.getDimension(R.styleable.WeekView_headerRowPadding, 10f)
        }

        viewState.apply {
            showWeekNumber = a.getBoolean(R.styleable.WeekView_showWeekNumber, false)
            weekNumberBackgroundCornerRadius = a.getDimension(R.styleable.WeekView_weekNumberBackgroundCornerRadius, 0f)
        }

        viewState.apply {
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, 0)
            adaptiveEventTextSize = a.getBoolean(R.styleable.WeekView_adaptiveEventTextSize, false)
            defaultEventColor = a.getColor(R.styleable.WeekView_defaultEventColor, context.colorAccent)
        }

        viewState.apply {
            eventPaddingHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_eventPaddingHorizontal, 8)
            eventPaddingVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventPaddingVertical, 8)
            columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, 10)
            overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, 0)
            eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, 2)
            eventMarginHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, 0)
        }

        viewState.apply {
            showDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, false)
            showDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, false)
        }

        viewState.apply {
            hourHeight = a.getDimension(R.styleable.WeekView_hourHeight, 50f)
            minHourHeight = a.getDimension(R.styleable.WeekView_minHourHeight, 0f)
            maxHourHeight = a.getDimension(R.styleable.WeekView_maxHourHeight, 400f)
            showCompleteDay = a.getBoolean(R.styleable.WeekView_showCompleteDay, false)
            effectiveMinHourHeight = minHourHeight
        }

        viewState.apply {
            showNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, false)
            showNowLineDot = a.getBoolean(R.styleable.WeekView_showNowLineDot, false)
        }

        viewState.apply {
            showHourSeparators = a.getBoolean(R.styleable.WeekView_showHourSeparator, true)
            showDaySeparators = a.getBoolean(R.styleable.WeekView_showDaySeparator, true)
        }

        viewState.apply {
            horizontalScrollingEnabled = a.getBoolean(R.styleable.WeekView_horizontalScrollingEnabled, true)

            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, 1f)
            horizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, true)
            verticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, true)
            scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, 250)
        }

        a.recycle()

        return viewState
    }
}

private const val SANS = 1
private const val SERIF = 2
private const val MONOSPACE = 3

private fun TypedArray.getCustomTypeface(): Typeface? {
    val fontFamily = getString(R.styleable.WeekView_fontFamily)
    val typefaceIndex = getInteger(R.styleable.WeekView_typeface, Typeface.NORMAL)
    val textStyle = getInteger(R.styleable.WeekView_textStyle, Typeface.NORMAL)
    return getTypefaceFromAttrs(fontFamily, typefaceIndex, textStyle)
}

private fun getTypefaceFromAttrs(
    familyName: String?,
    typefaceIndex: Int,
    styleIndex: Int
): Typeface? {
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
        else -> null
    }
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

private val Context.defaultTextSize: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
