package com.alamkanak.weekview

import android.content.Context
import android.graphics.Paint
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import java.util.Calendar
import kotlin.math.roundToInt

data class WeekViewEvent<T> internal constructor(
    var id: Long = 0L,
    internal var titleResource: TextResource? = null,
    var startTime: Calendar = now(),
    var endTime: Calendar = now(),
    internal var locationResource: TextResource? = null,
    var isAllDay: Boolean = false,
    var style: Style = Style(),
    var data: T? = null
) : WeekViewDisplayable<T>, Comparable<WeekViewEvent<T>> {

    internal val isNotAllDay: Boolean
        get() = isAllDay.not()

    internal val durationInMinutes: Int
        get() = ((endTime.timeInMillis - startTime.timeInMillis).toFloat() / 60_000).roundToInt()

    internal val isMultiDay: Boolean
        get() = startTime.isSameDate(endTime).not()

    internal fun isWithin(minHour: Int, maxHour: Int): Boolean {
        return startTime.hour >= minHour && endTime.hour <= maxHour
    }

    internal fun getTextPaint(
        context: Context,
        config: WeekViewConfigWrapper
    ): TextPaint {
        val textPaint = if (isAllDay) {
            config.allDayEventTextPaint
        } else {
            config.eventTextPaint
        }

        textPaint.color = when (val resource = style.textColorResource) {
            is ColorResource.Id -> ContextCompat.getColor(context, resource.resId)
            is ColorResource.Value -> resource.color
            null -> config.eventTextPaint.color
        }

        if (style.isTextStrikeThrough) {
            textPaint.flags = textPaint.flags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        return textPaint
    }

    internal fun collidesWith(other: WeekViewEvent<T>): Boolean {
        if (isAllDay != other.isAllDay) {
            return false
        }

        if (startTime.isEqual(other.startTime) && endTime.isEqual(other.endTime)) {
            // Complete overlap
            return true
        }

        // Resolve collisions by shortening the preceding event by 1 ms
        if (endTime.isEqual(other.startTime)) {
            endTime = endTime.minusMillis(1)
            return false
        } else if (startTime.isEqual(other.endTime)) {
            other.endTime = other.endTime.minusMillis(1)
        }

        return !startTime.isAfter(other.endTime) && !endTime.isBefore(other.startTime)
    }

    internal fun startsOnEarlierDay(originalEvent: WeekViewEvent<T>): Boolean {
        return startTime.isNotEqual(originalEvent.startTime)
    }

    internal fun endsOnLaterDay(originalEvent: WeekViewEvent<T>): Boolean {
        return endTime.isNotEqual(originalEvent.endTime)
    }

    override fun compareTo(other: WeekViewEvent<T>): Int {
        var comparator = startTime.compareTo(other.startTime)
        if (comparator == 0) {
            comparator = endTime.compareTo(other.endTime)
        }
        return comparator
    }

    override fun toWeekViewEvent(): WeekViewEvent<T> = this

    internal sealed class ColorResource {
        data class Value(@ColorInt val color: Int) : ColorResource()
        data class Id(@ColorRes val resId: Int) : ColorResource()
    }

    internal sealed class TextResource {
        data class Value(val text: CharSequence) : TextResource()
        data class Id(@StringRes val resId: Int) : TextResource()

        fun resolve(context: Context): CharSequence = when (this) {
            is Id -> context.getString(resId)
            is Value -> text
        }
    }

    internal sealed class DimenResource {
        data class Value(val value: Int) : DimenResource()
        data class Id(@DimenRes val resId: Int) : DimenResource()
    }

    class Style {

        internal var backgroundColorResource: ColorResource? = null
        internal var textColorResource: ColorResource? = null
        internal var isTextStrikeThrough: Boolean = false
        internal var borderWidthResource: DimenResource? = null
        internal var borderColorResource: ColorResource? = null

        internal val hasBorder: Boolean
            get() = borderWidthResource != null

        internal fun getBackgroundColorOrDefault(config: WeekViewConfigWrapper): ColorResource {
            return backgroundColorResource ?: ColorResource.Value(config.defaultEventColor)
        }

        internal fun getBorderWidth(
            context: Context
        ): Int = when (val resource = borderWidthResource) {
            is DimenResource.Id -> context.resources.getDimensionPixelSize(resource.resId)
            is DimenResource.Value -> resource.value
            null -> throw IllegalStateException("Invalid border width resource: $resource")
        }

        class Builder {

            private val style = Style()

            fun setBackgroundColor(@ColorInt color: Int): Builder {
                style.backgroundColorResource = ColorResource.Value(color)
                return this
            }

            fun setBackgroundColorResource(@ColorRes resId: Int): Builder {
                style.backgroundColorResource = ColorResource.Id(resId)
                return this
            }

            fun setTextColor(@ColorInt color: Int): Builder {
                style.textColorResource = ColorResource.Value(color)
                return this
            }

            fun setTextColorResource(@ColorRes resId: Int): Builder {
                style.textColorResource = ColorResource.Id(resId)
                return this
            }

            fun setTextStrikeThrough(strikeThrough: Boolean): Builder {
                style.isTextStrikeThrough = strikeThrough
                return this
            }

            fun setBorderWidth(width: Int): Builder {
                style.borderWidthResource = DimenResource.Value(width)
                return this
            }

            fun setBorderWidthResource(@DimenRes resId: Int): Builder {
                style.borderWidthResource = DimenResource.Id(resId)
                return this
            }

            fun setBorderColor(@ColorInt color: Int): Builder {
                style.borderColorResource = ColorResource.Value(color)
                return this
            }

            fun setBorderColorResource(@ColorRes resId: Int): Builder {
                style.borderColorResource = ColorResource.Id(resId)
                return this
            }

            fun build(): Style = style
        }
    }

    class Builder<T>(data: T) {

        private val event = WeekViewEvent(data = data)

        fun setId(id: Long): Builder<T> {
            event.id = id
            return this
        }

        fun setTitle(title: CharSequence): Builder<T> {
            event.titleResource = TextResource.Value(title)
            return this
        }

        fun setTitle(resId: Int): Builder<T> {
            event.titleResource = TextResource.Id(resId)
            return this
        }

        fun setStartTime(startTime: Calendar): Builder<T> {
            event.startTime = startTime
            return this
        }

        fun setEndTime(endTime: Calendar): Builder<T> {
            event.endTime = endTime
            return this
        }

        fun setLocation(location: CharSequence): Builder<T> {
            event.locationResource = TextResource.Value(location)
            return this
        }

        fun setLocation(resId: Int): Builder<T> {
            event.locationResource = TextResource.Id(resId)
            return this
        }

        fun setStyle(style: Style): Builder<T> {
            event.style = style
            return this
        }

        fun setAllDay(isAllDay: Boolean): Builder<T> {
            event.isAllDay = isAllDay
            return this
        }

        fun build(): WeekViewEvent<T> = event
    }
}
