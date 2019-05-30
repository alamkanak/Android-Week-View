package com.alamkanak.weekview

import android.graphics.Paint
import android.support.annotation.ColorInt
import android.text.TextPaint
import com.alamkanak.weekview.Constants.MINUTES_PER_HOUR
import java.util.*

data class WeekViewEvent<T> internal constructor(
        var id: Long = 0L,
        var title: String = "",
        var startTime: Calendar = now(),
        var endTime: Calendar = now(),
        var location: String? = null,
        var isAllDay: Boolean = false,
        var style: Style = Style(),
        var data: T? = null
): WeekViewDisplayable<T>, Comparable<WeekViewEvent<T>> {

    val isNotAllDay: Boolean
        get() = isAllDay.not()

    internal fun getEffectiveStartMinutes(config: WeekViewConfigWrapper): Int {
        val startHour = startTime.hour - config.minHour
        return startHour * MINUTES_PER_HOUR + startTime.minute
    }

    internal fun getEffectiveEndMinutes(config: WeekViewConfigWrapper): Int {
        val endHour = endTime.hour - config.minHour
        return endHour * MINUTES_PER_HOUR + endTime.minute
    }

    internal fun isSameDay(other: Calendar): Boolean {
        return startTime.isSameDate(other)
    }

    internal fun isWithin(minHour: Int, maxHour: Int): Boolean {
        return startTime.hour >= minHour && endTime.hour <= maxHour
    }

    val hasBorder: Boolean
        get() = style.borderWidth > 0

    internal fun getColorOrDefault(config: WeekViewConfigWrapper): Int {
        return if (style.backgroundColor != 0) style.backgroundColor else config.defaultEventColor
    }

    internal fun getTextPaint(config: WeekViewConfigWrapper): TextPaint {
        val textPaint = if (isAllDay) {
            config.allDayEventTextPaint
        } else {
            config.eventTextPaint
        }

        textPaint.color = if (style.textColor != 0) style.textColor else config.eventTextPaint.color

        if (style.isTextStrikeThrough) {
            textPaint.flags = textPaint.flags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        return textPaint
    }

    internal fun collidesWith(other: WeekViewEvent<T>): Boolean {
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

        return !startTime.isAfter(other.endTime) && endTime.isBefore(other.startTime)
    }

    fun startsOnEarlierDay(originalEvent: WeekViewEvent<T>): Boolean {
        return !startTime.isEqual(originalEvent.startTime)
    }

    fun endsOnLaterDay(originalEvent: WeekViewEvent<T>): Boolean {
        return !endTime.isEqual(originalEvent.endTime)
    }

    override fun compareTo(other: WeekViewEvent<T>): Int {
        var comparator = startTime.compareTo(other.startTime)
        if (comparator == 0) {
            comparator = endTime.compareTo(other.endTime)
        }
        return comparator
    }

    override fun toWeekViewEvent(): WeekViewEvent<T> = this

    class Style {

        var backgroundColor: Int = 0
            private set

        var textColor: Int = 0
            private set

        var isTextStrikeThrough: Boolean = false
            private set

        var borderWidth: Int = 0
            private set

        var borderColor: Int = 0
            private set

        class Builder {

            private val style = Style()

            fun setBackgroundColor(@ColorInt color: Int): Builder {
                style.backgroundColor = color
                return this
            }

            fun setTextColor(@ColorInt color: Int): Builder {
                style.textColor = color
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
                style.borderColor = color
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
            event.title = title
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
            event.location = location
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
