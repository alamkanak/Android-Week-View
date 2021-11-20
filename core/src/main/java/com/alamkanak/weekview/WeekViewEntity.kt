package com.alamkanak.weekview

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import java.util.Calendar

/**
 * Encapsulates all information necessary to render an entity in [WeekView]. This entity can be
 * either a [WeekViewEntity.Event] or a [WeekViewEntity.BlockedTime].
 */
sealed class WeekViewEntity {

    data class Event<T> internal constructor(
        internal val id: Long = 0L,
        internal val titleResource: TextResource,
        internal val startTime: Calendar = now(),
        internal val endTime: Calendar = now(),
        internal val subtitleResource: TextResource? = null,
        internal val isAllDay: Boolean = false,
        internal val style: Style = Style(),
        internal val data: T
    ) : WeekViewEntity() {

        class Builder<T>(private val data: T) {

            private var id: Long? = null
            private var title: TextResource? = null
            private var subtitle: TextResource? = null
            private var startTime: Calendar? = null
            private var endTime: Calendar? = null
            private var style: Style? = null
            private var isAllDay: Boolean = false

            @PublicApi
            fun setId(id: Long): Builder<T> {
                this.id = id
                return this
            }

            @PublicApi
            fun setTitle(title: CharSequence): Builder<T> {
                this.title = TextResource.Value(title)
                return this
            }

            @PublicApi
            fun setTitle(resId: Int): Builder<T> {
                this.title = TextResource.Id(resId)
                return this
            }

            @PublicApi
            fun setStartTime(startTime: Calendar): Builder<T> {
                this.startTime = startTime
                return this
            }

            @PublicApi
            fun setEndTime(endTime: Calendar): Builder<T> {
                this.endTime = endTime
                return this
            }

            @Deprecated(
                message = "Use setSubtitle() instead.",
                replaceWith = ReplaceWith(expression = "setSubtitle"),
                level = DeprecationLevel.ERROR
            )
            @PublicApi
            fun setLocation(location: CharSequence): Builder<T> {
                this.subtitle = TextResource.Value(location)
                return this
            }

            @Deprecated(
                message = "Use setSubtitle() instead.",
                replaceWith = ReplaceWith(expression = "setSubtitle"),
                level = DeprecationLevel.ERROR
            )
            @PublicApi
            fun setLocation(resId: Int): Builder<T> {
                this.subtitle = TextResource.Id(resId)
                return this
            }

            @PublicApi
            fun setSubtitle(subtitle: CharSequence): Builder<T> {
                this.subtitle = TextResource.Value(subtitle)
                return this
            }

            @PublicApi
            fun setSubtitle(resId: Int): Builder<T> {
                this.subtitle = TextResource.Id(resId)
                return this
            }

            @PublicApi
            fun setStyle(style: Style): Builder<T> {
                this.style = style
                return this
            }

            @PublicApi
            fun setAllDay(isAllDay: Boolean): Builder<T> {
                this.isAllDay = isAllDay
                return this
            }

            @PublicApi
            fun build(): WeekViewEntity {
                val id = checkNotNull(id) { "id == null" }
                val title = checkNotNull(title) { "title == null" }
                val startTime = checkNotNull(startTime) { "startTime == null" }
                val endTime = checkNotNull(endTime) { "endTime == null" }
                val data = checkNotNull(data) { "data == null" }
                val style = this.style ?: Style()
                return Event(id, title, startTime, endTime, subtitle, isAllDay, style, data)
            }
        }
    }

    data class BlockedTime internal constructor(
        internal val id: Long = 0L,
        internal val titleResource: TextResource,
        internal val subtitleResource: TextResource? = null,
        internal val startTime: Calendar = now(),
        internal val endTime: Calendar = now(),
        internal val style: Style = Style()
    ) : WeekViewEntity() {

        class Builder {

            private var id: Long? = null
            private var title: TextResource? = null
            private var subtitle: TextResource? = null
            private var startTime: Calendar? = null
            private var endTime: Calendar? = null
            private var style: Style? = null

            @PublicApi
            fun setId(id: Long): Builder {
                this.id = id
                return this
            }

            @PublicApi
            fun setTitle(title: String): Builder {
                this.title = TextResource.Value(title)
                return this
            }

            @PublicApi
            fun setTitle(resId: Int): Builder {
                this.title = TextResource.Id(resId)
                return this
            }

            @PublicApi
            fun setStartTime(startTime: Calendar): Builder {
                this.startTime = startTime
                return this
            }

            @PublicApi
            fun setEndTime(endTime: Calendar): Builder {
                this.endTime = endTime
                return this
            }

            @PublicApi
            fun setSubtitle(subtitle: CharSequence): Builder {
                this.subtitle = TextResource.Value(subtitle)
                return this
            }

            @PublicApi
            fun setSubtitle(resId: Int): Builder {
                this.subtitle = TextResource.Id(resId)
                return this
            }

            @PublicApi
            fun setStyle(style: Style): Builder {
                this.style = style
                return this
            }

            @PublicApi
            fun build(): WeekViewEntity {
                val id = checkNotNull(id) { "id == null" }
                val title = title ?: TextResource.Value(text = "")
                val startTime = checkNotNull(startTime) { "startTime == null" }
                val endTime = checkNotNull(endTime) { "endTime == null" }
                val style = style ?: Style()
                return BlockedTime(id, title, subtitle, startTime, endTime, style)
            }
        }
    }

    class Style internal constructor() {

        internal var textColorResource: ColorResource? = null
        internal var borderWidthResource: DimenResource? = null
        internal var borderColorResource: ColorResource? = null
        internal var backgroundColorResource: ColorResource? = null
        internal var cornerRadiusResource: DimenResource? = null
        internal var pattern: Pattern? = null

        @Deprecated("No longer used.")
        internal var isTextStrikeThrough: Boolean = false

        sealed class Pattern {

            abstract val color: Int
            abstract val strokeWidth: Int

            data class Lined(
                @ColorInt override val color: Int,
                @Dimension override val strokeWidth: Int,
                @Dimension val spacing: Int,
                val direction: Direction = Direction.StartToEnd
            ) : Pattern() {
                enum class Direction {
                    StartToEnd, EndToStart
                }
            }

            data class Dotted(
                @ColorInt override val color: Int,
                @Dimension override val strokeWidth: Int,
                @Dimension val spacing: Int
            ) : Pattern()
        }

        class Builder {

            private val style = Style()

            @PublicApi
            fun setTextColor(@ColorInt color: Int): Builder {
                style.textColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setTextColorResource(@ColorRes resId: Int): Builder {
                style.textColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            @Deprecated(
                message = "Use a SpannableString for the title and subtitle instead.",
                level = DeprecationLevel.ERROR
            )
            fun setTextStrikeThrough(strikeThrough: Boolean): Builder {
                style.isTextStrikeThrough = strikeThrough
                return this
            }

            @PublicApi
            fun setBorderWidth(width: Int): Builder {
                style.borderWidthResource = DimenResource.Value(width)
                return this
            }

            @PublicApi
            fun setBorderWidthResource(@DimenRes resId: Int): Builder {
                style.borderWidthResource = DimenResource.Id(resId)
                return this
            }

            @PublicApi
            fun setBorderColor(@ColorInt color: Int): Builder {
                style.borderColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setBorderColorResource(@ColorRes resId: Int): Builder {
                style.borderColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            fun setBackgroundColor(@ColorInt color: Int): Builder {
                style.backgroundColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setBackgroundColorResource(@ColorRes resId: Int): Builder {
                style.backgroundColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            fun setPattern(pattern: Pattern): Builder {
                style.pattern = pattern
                return this
            }

            @PublicApi
            fun setCornerRadius(radius: Int): Builder {
                style.cornerRadiusResource = DimenResource.Value(radius)
                return this
            }

            @PublicApi
            fun setCornerRadiusResource(@DimenRes resId: Int): Builder {
                style.cornerRadiusResource = DimenResource.Id(resId)
                return this
            }

            @PublicApi
            fun build(): Style = style
        }
    }
}

@Deprecated(
    "Use WeekViewEntity.Event instead.",
    replaceWith = ReplaceWith(expression = "WeekViewEntity.Event")
)
data class WeekViewEvent<T> internal constructor(
    internal val id: Long = 0L,
    internal val titleResource: TextResource,
    internal val startTime: Calendar = now(),
    internal val endTime: Calendar = now(),
    internal val locationResource: TextResource? = null,
    internal val isAllDay: Boolean = false,
    internal val style: Style = Style(),
    internal val data: T
) : WeekViewDisplayable<T> {

    override fun toWeekViewEvent(): WeekViewEvent<T> = this

    @Deprecated(
        "Use WeekViewEntity.Style instead.",
        replaceWith = ReplaceWith(expression = "WeekViewEntity.Style")
    )
    class Style {

        internal var backgroundColorResource: ColorResource? = null
        internal var textColorResource: ColorResource? = null
        internal var borderWidthResource: DimenResource? = null
        internal var borderColorResource: ColorResource? = null

        @Deprecated("No longer used.")
        internal var isTextStrikeThrough: Boolean = false

        class Builder {

            private val style = Style()

            @PublicApi
            fun setBackgroundColor(@ColorInt color: Int): Builder {
                style.backgroundColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setBackgroundColorResource(@ColorRes resId: Int): Builder {
                style.backgroundColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            fun setTextColor(@ColorInt color: Int): Builder {
                style.textColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setTextColorResource(@ColorRes resId: Int): Builder {
                style.textColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            @Deprecated("Use a SpannableString for the title or subtitle instead.")
            fun setTextStrikeThrough(strikeThrough: Boolean): Builder {
                style.isTextStrikeThrough = strikeThrough
                return this
            }

            @PublicApi
            fun setBorderWidth(width: Int): Builder {
                style.borderWidthResource = DimenResource.Value(width)
                return this
            }

            @PublicApi
            fun setBorderWidthResource(@DimenRes resId: Int): Builder {
                style.borderWidthResource = DimenResource.Id(resId)
                return this
            }

            @PublicApi
            fun setBorderColor(@ColorInt color: Int): Builder {
                style.borderColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setBorderColorResource(@ColorRes resId: Int): Builder {
                style.borderColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            fun build(): Style = style
        }
    }

    @Deprecated(
        "Use WeekViewEntity.Event.Builder instead.",
        replaceWith = ReplaceWith(expression = "WeekViewEntity.Event.Builder")
    )
    class Builder<T>(private val data: T) {

        private var id: Long? = null
        private var title: TextResource? = null
        private var location: TextResource? = null
        private var startTime: Calendar? = null
        private var endTime: Calendar? = null
        private var style: Style? = null
        private var isAllDay: Boolean = false

        @PublicApi
        fun setId(id: Long): Builder<T> {
            this.id = id
            return this
        }

        @PublicApi
        fun setTitle(title: CharSequence): Builder<T> {
            this.title = TextResource.Value(title)
            return this
        }

        @PublicApi
        fun setTitle(resId: Int): Builder<T> {
            this.title = TextResource.Id(resId)
            return this
        }

        @PublicApi
        fun setStartTime(startTime: Calendar): Builder<T> {
            this.startTime = startTime
            return this
        }

        @PublicApi
        fun setEndTime(endTime: Calendar): Builder<T> {
            this.endTime = endTime
            return this
        }

        @PublicApi
        fun setLocation(location: CharSequence): Builder<T> {
            this.location = TextResource.Value(location)
            return this
        }

        @PublicApi
        fun setLocation(resId: Int): Builder<T> {
            this.location = TextResource.Id(resId)
            return this
        }

        @PublicApi
        fun setStyle(style: Style): Builder<T> {
            this.style = style
            return this
        }

        @PublicApi
        fun setAllDay(isAllDay: Boolean): Builder<T> {
            this.isAllDay = isAllDay
            return this
        }

        @PublicApi
        fun build(): WeekViewEvent<T> {
            val id = checkNotNull(id) { "id == null" }
            val title = checkNotNull(title) { "title == null" }
            val startTime = checkNotNull(startTime) { "startTime == null" }
            val endTime = checkNotNull(endTime) { "endTime == null" }
            val data = checkNotNull(data) { "data == null" }
            val style = this.style ?: Style.Builder().build()
            return WeekViewEvent(id, title, startTime, endTime, location, isAllDay, style, data)
        }
    }
}

internal fun <T> WeekViewDisplayable<T>.toWeekViewEntity(
    context: Context
) = toWeekViewEvent().toWeekViewEntity(context)

internal fun <T> WeekViewEvent<T>.toWeekViewEntity(
    context: Context
): WeekViewEntity = WeekViewEntity.Event(
    id = id,
    titleResource = titleResource,
    startTime = startTime,
    endTime = endTime,
    subtitleResource = locationResource,
    isAllDay = isAllDay,
    style = style.toWeekViewEntityStyle(context),
    data = data
)

private fun WeekViewEvent.Style.toWeekViewEntityStyle(
    context: Context
): WeekViewEntity.Style {
    return WeekViewEntity.Style.Builder()
        .apply { backgroundColorResource?.let { setBackgroundColor(it.resolve(context)) } }
        .apply { textColorResource?.let { setTextColor(it.resolve(context)) } }
        .apply { borderWidthResource?.let { setBorderWidth(it.resolve(context)) } }
        .apply { borderColorResource?.let { setBorderColor(it.resolve(context)) } }
        .build()
}
