package com.alamkanak.weekview

import android.content.Context
import java.util.Calendar
import kotlin.math.roundToInt

internal fun <T> WeekViewDisplayable<T>.toResolvedWeekViewEvent(
    context: Context
) = toWeekViewEvent().resolve(context)

internal fun <T> WeekViewEvent<T>.resolve(
    context: Context
) = ResolvedWeekViewEvent(
    id = id,
    title = titleResource.resolve(context, semibold = true),
    startTime = startTime.withLocalTimeZone(),
    endTime = endTime.withLocalTimeZone(),
    location = locationResource?.resolve(context, semibold = false),
    isAllDay = isAllDay,
    style = style.resolve(context),
    data = data
)

internal fun WeekViewEvent.Style.resolve(
    context: Context
) = ResolvedWeekViewEvent.Style(
    backgroundColor = backgroundColorResource?.resolve(context),
    borderColor = borderColorResource?.resolve(context),
    borderWidth = borderWidthResource?.resolve(context),
    textColor = textColorResource?.resolve(context),
    isTextStrikeThrough = isTextStrikeThrough
)

internal data class ResolvedWeekViewEvent<T>(
    val id: Long,
    val title: CharSequence,
    val startTime: Calendar,
    val endTime: Calendar,
    val location: CharSequence?,
    val isAllDay: Boolean,
    val style: Style,
    val data: T
) {

    data class Style(
        val backgroundColor: Int? = null,
        val borderColor: Int? = null,
        val borderWidth: Int? = null,
        val textColor: Int? = null,
        @Deprecated("No longer used.")
        val isTextStrikeThrough: Boolean = false
    )

    internal val isNotAllDay: Boolean = isAllDay.not()

    internal val durationInMinutes: Int
        get() = ((endTime.timeInMillis - startTime.timeInMillis).toFloat() / 60_000).roundToInt()

    internal val isMultiDay: Boolean
        get() = startTime.isSameDate(endTime).not()

    internal fun isWithin(
        minHour: Int,
        maxHour: Int
    ): Boolean = startTime.hour >= minHour && endTime.hour <= maxHour

    internal fun collidesWith(other: ResolvedWeekViewEvent<*>): Boolean {
        if (isAllDay != other.isAllDay) {
            return false
        }

        if (startTime.isEqual(other.startTime) && endTime.isEqual(other.endTime)) {
            // Complete overlap
            return true
        }

        // Resolve collisions by shortening the preceding event by 1 ms
        if (endTime.isEqual(other.startTime)) {
            endTime -= Millis(1)
            return false
        } else if (startTime.isEqual(other.endTime)) {
            other.endTime -= Millis(1)
        }

        return !startTime.isAfter(other.endTime) && !endTime.isBefore(other.startTime)
    }

    internal fun startsOnEarlierDay(
        originalEvent: ResolvedWeekViewEvent<*>
    ): Boolean = startTime.isNotEqual(originalEvent.startTime)

    internal fun endsOnLaterDay(
        originalEvent: ResolvedWeekViewEvent<*>
    ): Boolean = endTime.isNotEqual(originalEvent.endTime)
}
