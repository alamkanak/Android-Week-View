package com.alamkanak.weekview

import android.content.Context
import android.graphics.Paint
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.alamkanak.weekview.Constants.MINUTES_PER_HOUR
import java.util.Calendar

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

    val title: String?
        get() = (titleResource as? TextResource.Value)?.text

    val titleResId: Int?
        get() = (titleResource as? TextResource.Id)?.resId

    val location: String?
        get() = (locationResource as? TextResource.Value)?.text

    val locationResId: Int?
        get() = (locationResource as? TextResource.Id)?.resId

    val isNotAllDay: Boolean
        get() = isAllDay.not()

    internal val isMultiDay: Boolean
        get() = isSameDay(endTime).not()

    internal fun getEffectiveStartMinutes(config: WeekViewConfigWrapper): Int {
        val startHour = startTime.hour - config.minHour
        return startHour * MINUTES_PER_HOUR.toInt() + startTime.minute
    }

    internal fun getEffectiveEndMinutes(config: WeekViewConfigWrapper): Int {
        val endHour = endTime.hour - config.minHour
        return endHour * MINUTES_PER_HOUR.toInt() + endTime.minute
    }

    internal fun isSameDay(other: Calendar): Boolean {
        return startTime.isSameDate(other)
    }

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
        data class Value(val color: Int) : ColorResource()
        data class Id(val resId: Int) : ColorResource()
    }

    internal sealed class TextResource {
        data class Value(val text: String) : TextResource()
        data class Id(val resId: Int) : TextResource()
    }

    class Style {

        internal var backgroundColorResource: ColorResource? = null
        internal var textColorResource: ColorResource? = null
        internal var isTextStrikeThrough: Boolean = false
        internal var borderWidth: Int = 0
        internal var borderColorResource: ColorResource? = null

        internal val hasBorder: Boolean
            get() = borderWidth > 0

        internal fun getBackgroundColorOrDefault(config: WeekViewConfigWrapper): ColorResource {
            return backgroundColorResource ?: ColorResource.Value(config.defaultEventColor)
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
                style.borderWidth = width
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

            fun build(): Style {
                return style
            }

        }

    }

    class Builder<T> {

        private val event = WeekViewEvent<T>()

        fun setId(id: Long): Builder<T> {
            event.id = id
            return this
        }

        fun setTitle(title: String): Builder<T> {
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

        fun setLocation(location: String): Builder<T> {
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

        fun setData(data: T): Builder<T> {
            event.data = data
            return this
        }

        fun build(): WeekViewEvent<T> {
            return event
        }

    }

}
