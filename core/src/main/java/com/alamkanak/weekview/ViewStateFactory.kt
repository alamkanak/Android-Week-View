package com.alamkanak.weekview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat.LAYOUT_DIRECTION_LTR
import kotlin.math.roundToInt

internal object ViewStateFactory {

    fun create(context: Context, attrs: AttributeSet?): ViewState {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        val viewState = ViewState()

        if (Build.VERSION.SDK_INT >= 17) {
            viewState.isLtr = context.resources.configuration.layoutDirection == LAYOUT_DIRECTION_LTR
        }

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

        viewState.headerTextPaint.apply {
            color = a.getColor(R.styleable.WeekView_headerTextColor, context.textColorPrimary)
            textSize = a.getDimension(R.styleable.WeekView_headerTextSize, context.defaultTextSize)
            typeface = headerTextTypeface
        }

        viewState.headerBottomLinePaint.apply {
            color = a.getColor(R.styleable.WeekView_headerBottomLineColor, context.lineColor)
            strokeWidth = a.getDimension(R.styleable.WeekView_headerBottomLineWidth, context.dp(1))
        }

        viewState.todayHeaderTextPaint.apply {
            color = a.getColor(R.styleable.WeekView_todayHeaderTextColor, context.colorAccent)
            textSize = a.getDimension(R.styleable.WeekView_headerTextSize, context.defaultTextSize)
            typeface = headerTextTypeface
        }

        viewState.weekendHeaderTextPaint.apply {
            color = a.getColor(R.styleable.WeekView_weekendHeaderTextColor, viewState.headerTextPaint.color)
            textSize = a.getDimension(R.styleable.WeekView_headerTextSize, context.defaultTextSize)
            typeface = headerTextTypeface
        }

        viewState.headerBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_headerBackgroundColor, context.colorBackground)
        }

        viewState.headerBackgroundWithShadowPaint.apply {
            color = viewState.headerBackgroundPaint.color
            val shadowColor = a.getColor(R.styleable.WeekView_headerBottomShadowColor, context.shadowColor)
            val shadowRadius = a.getDimension(R.styleable.WeekView_headerBottomShadowRadius, context.dp(2))
            setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        }

        viewState.hourSeparatorPaint.apply {
            color = a.getColor(R.styleable.WeekView_hourSeparatorColor, context.lineColor)
            strokeWidth = a.getDimension(R.styleable.WeekView_hourSeparatorStrokeWidth, context.dp(1))
        }

        viewState.daySeparatorPaint.apply {
            color = a.getColor(R.styleable.WeekView_daySeparatorColor, context.lineColor)
            strokeWidth = a.getDimension(R.styleable.WeekView_daySeparatorStrokeWidth, context.dp(1))
        }

        viewState.dayBackgroundPaint.apply {
            color = a.getColor(R.styleable.WeekView_dayBackgroundColor, context.colorBackground)
        }

        viewState.todayBackgroundPaint = a.paintFromColor(colorIndex = R.styleable.WeekView_todayBackgroundColor)
        viewState.pastBackgroundPaint = a.paintFromColor(colorIndex = R.styleable.WeekView_pastBackgroundColor)
        viewState.futureBackgroundPaint = a.paintFromColor(colorIndex = R.styleable.WeekView_futureBackgroundColor)

        viewState.pastWeekendBackgroundPaint = a.paintFromColor(colorIndex = R.styleable.WeekView_pastWeekendBackgroundColor)
        viewState.futureWeekendBackgroundPaint = a.paintFromColor(colorIndex = R.styleable.WeekView_futureWeekendBackgroundColor)

        viewState.timeColumnSeparatorPaint.apply {
            color = a.getColor(R.styleable.WeekView_timeColumnSeparatorColor, context.lineColor)
            strokeWidth = a.getDimension(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, context.dp(1))
        }

        viewState.nowLinePaint.apply {
            color = a.getColor(R.styleable.WeekView_nowLineColor, context.colorAccent)
            strokeWidth = a.getDimension(R.styleable.WeekView_nowLineStrokeWidth, context.dp(3))
        }

        viewState.nowDotPaint.apply {
            color = a.getColor(R.styleable.WeekView_nowLineDotColor, context.colorAccent)
            strokeWidth = a.getDimension(R.styleable.WeekView_nowLineDotRadius, context.dp(4))
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
            color = a.getColor(R.styleable.WeekView_timeColumnBackgroundColor, context.colorBackground)
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
            arrangeAllDayEventsVertically = a.getBoolean(R.styleable.WeekView_arrangeAllDayEventsVertically, true)
        }

        viewState.apply {
            showHeaderBottomLine = a.getBoolean(R.styleable.WeekView_showHeaderBottomLine, false)
            showHeaderBottomShadow = a.getBoolean(R.styleable.WeekView_showHeaderBottomShadow, false)
        }

        viewState.apply {
            timeColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, 10)
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
            headerPadding = a.getDimension(R.styleable.WeekView_headerPadding, context.dp(8))
        }

        viewState.apply {
            showWeekNumber = a.getBoolean(R.styleable.WeekView_showWeekNumber, false)
            weekNumberBackgroundCornerRadius = a.getDimension(R.styleable.WeekView_weekNumberBackgroundCornerRadius, context.dp(8))
        }

        viewState.apply {
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, context.dp(2).roundToInt())
            adaptiveEventTextSize = a.getBoolean(R.styleable.WeekView_adaptiveEventTextSize, false)
            defaultEventColor = a.getColor(R.styleable.WeekView_defaultEventColor, context.colorAccent)
        }

        viewState.apply {
            eventPaddingHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_eventPaddingHorizontal, context.dp(4).roundToInt())
            eventPaddingVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventPaddingVertical, context.dp(4).roundToInt())
            columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, context.dp(8).roundToInt())
            overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, 0)
            eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, context.dp(2).roundToInt())
            singleDayHorizontalPadding = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalPadding, 0)
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

private fun TypedArray.paintFromColor(colorIndex: Int): Paint? {
    return if (hasValue(colorIndex)) getColor(colorIndex, 0).toPaint() else null
}

internal fun Int.toPaint(): Paint {
    return Paint().apply { color = this@toPaint }
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

private val Context.colorBackground: Int
    get() = resolveColor(android.R.attr.colorBackground)

private val Context.defaultTextSize: Float
    get() = sp(12)

private fun Context.dp(value: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics)
}

private fun Context.sp(value: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value.toFloat(), resources.displayMetrics)
}
