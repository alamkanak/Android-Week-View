package com.alamkanak.weekview

import android.content.Context
import java.util.Calendar
import kotlin.math.roundToInt

internal sealed class ResolvedWeekViewEntity {

    internal abstract val id: Long
    internal abstract val title: CharSequence
    internal abstract val subtitle: CharSequence?
    internal abstract val startTime: Calendar
    internal abstract val endTime: Calendar
    internal abstract val isAllDay: Boolean
    internal abstract val style: Style

    data class Event<T>(
        override val id: Long,
        override val title: CharSequence,
        override val startTime: Calendar,
        override val endTime: Calendar,
        override val subtitle: CharSequence?,
        override val isAllDay: Boolean,
        override val style: Style,
        val data: T?
    ) : ResolvedWeekViewEntity()

    data class BlockedTime(
        override val id: Long,
        override val title: CharSequence,
        override val subtitle: CharSequence?,
        override val startTime: Calendar,
        override val endTime: Calendar,
        override val style: Style
    ) : ResolvedWeekViewEntity() {
        override val isAllDay: Boolean = false
    }

    data class Style(
        val textColor: Int? = null,
        val backgroundColor: Int? = null,
        val pattern: WeekViewEntity.Style.Pattern? = null,
        val borderColor: Int? = null,
        val borderWidth: Int? = null,
        val cornerRadius: Int? = null
    )

    internal val isNotAllDay: Boolean
        get() = isAllDay.not()

    internal val durationInMinutes: Int
        get() = ((endTime.timeInMillis - startTime.timeInMillis).toFloat() / 60_000).roundToInt()

    internal val isMultiDay: Boolean
        get() = startTime.isSameDate(endTime).not()

    internal fun isWithin(
        minHour: Int,
        maxHour: Int
    ): Boolean = startTime.hour >= minHour && endTime.hour <= maxHour

    internal fun collidesWith(other: ResolvedWeekViewEntity): Boolean {
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
        originalEvent: ResolvedWeekViewEntity
    ): Boolean = startTime.isNotEqual(originalEvent.startTime)

    internal fun endsOnLaterDay(
        originalEvent: ResolvedWeekViewEntity
    ): Boolean = endTime.isNotEqual(originalEvent.endTime)

    internal fun createCopy(
        startTime: Calendar = this.startTime,
        endTime: Calendar = this.endTime
    ): ResolvedWeekViewEntity = when (this) {
        is Event<*> -> copy(startTime = startTime, endTime = endTime)
        is BlockedTime -> copy(startTime = startTime, endTime = endTime)
    }
}

internal fun WeekViewEntity.resolve(
    context: Context
): ResolvedWeekViewEntity = when (this) {
    is WeekViewEntity.Event<*> -> ResolvedWeekViewEntity.Event(
        id = id,
        title = titleResource.resolve(context, semibold = true),
        startTime = startTime.withLocalTimeZone(),
        endTime = endTime.withLocalTimeZone(),
        subtitle = subtitleResource?.resolve(context, semibold = false),
        isAllDay = isAllDay,
        style = style.resolve(context),
        data = data
    )
    is WeekViewEntity.BlockedTime -> ResolvedWeekViewEntity.BlockedTime(
        id = id,
        title = titleResource.resolve(context, semibold = true),
        subtitle = subtitleResource?.resolve(context, semibold = false),
        startTime = startTime.withLocalTimeZone(),
        endTime = endTime.withLocalTimeZone(),
        style = style.resolve(context)
    )
}

internal fun WeekViewEntity.Style.resolve(
    context: Context
) = ResolvedWeekViewEntity.Style(
    textColor = textColorResource?.resolve(context),
    backgroundColor = backgroundColorResource?.resolve(context),
    pattern = pattern,
    borderColor = borderColorResource?.resolve(context),
    borderWidth = borderWidthResource?.resolve(context),
    cornerRadius = cornerRadiusResource?.resolve(context)
)
