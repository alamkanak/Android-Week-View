package com.alamkanak.weekview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils.TruncateAt
import android.text.TextUtils.ellipsize
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat

internal class EventChipDrawer<T>(
    private val context: Context,
    private val config: WeekViewConfigWrapper
) {

    private val textLayoutCache = mutableMapOf<Long, StaticLayout>()

    internal fun draw(
        eventChip: EventChip<T>,
        canvas: Canvas,
        paint: Paint,
        textLayout: StaticLayout? = null
    ) {
        val event = eventChip.event

        val cornerRadius = config.eventCornerRadius.toFloat()
        setBackgroundPaint(event, paint)

        eventChip.rect?.let {
            canvas.drawRoundRect(it, cornerRadius, cornerRadius, paint)
        }

        if (event.style.hasBorder) {
            setBorderPaint(event, paint)
            val borderWidth = event.style.borderWidth

            val rect = checkNotNull(eventChip.rect)
            val adjustedRect = RectF(
                rect.left + borderWidth / 2f,
                rect.top + borderWidth / 2f,
                rect.right - borderWidth / 2f,
                rect.bottom - borderWidth / 2f)
            canvas.drawRoundRect(adjustedRect, cornerRadius, cornerRadius, paint)
        }

        if (event.isNotAllDay) {
            drawCornersForMultiDayEvents(eventChip, paint, cornerRadius, canvas)
        }

        textLayout?.let {
            // The text height has already been calculated
            drawEventTitle(eventChip, it, canvas)
        } ?: calculateTextHeightAndDrawTitle(eventChip, canvas)
    }

    private fun drawCornersForMultiDayEvents(
        eventChip: EventChip<T>,
        backgroundPaint: Paint,
        cornerRadius: Float,
        canvas: Canvas
    ) {
        val event = eventChip.event
        val originalEvent = eventChip.originalEvent
        val rect = checkNotNull(eventChip.rect)

        if (event.startsOnEarlierDay(originalEvent)) {
            val topRect = RectF(rect.left, rect.top, rect.right, rect.top + cornerRadius)
            canvas.drawRect(topRect, backgroundPaint)
        }

        if (event.endsOnLaterDay(originalEvent)) {
            val bottomRect = RectF(rect.left, rect.bottom - cornerRadius, rect.right, rect.bottom)
            canvas.drawRect(bottomRect, backgroundPaint)
        }

        if (!event.style.hasBorder) {
            return
        }

        val borderWidth = event.style.borderWidth.toFloat()
        val innerWidth = rect.width() - borderWidth * 2

        val borderStartX = rect.left + borderWidth
        val borderEndX = borderStartX + innerWidth

        if (event.startsOnEarlierDay(originalEvent)) {
            // Remove top border stroke
            val borderStartY = rect.top
            val borderEndY = borderStartY + borderWidth
            val newRect = RectF(borderStartX, borderStartY, borderEndX, borderEndY)
            canvas.drawRect(newRect, backgroundPaint)
        }

        if (event.endsOnLaterDay(originalEvent)) {
            // Remove bottom border stroke
            val borderEndY = rect.bottom
            val borderStartY = borderEndY - borderWidth
            val newRect = RectF(borderStartX, borderStartY, borderEndX, borderEndY)
            canvas.drawRect(newRect, backgroundPaint)
        }
    }

    private fun drawEventTitle(
        eventChip: EventChip<T>,
        textLayout: StaticLayout,
        canvas: Canvas
    ) {
        val rect = checkNotNull(eventChip.rect)
        canvas.apply {
            save()
            translate(rect.left + config.eventPadding, rect.top + config.eventPadding)
            textLayout.draw(this)
            restore()
        }
    }

    private fun calculateTextHeightAndDrawTitle(
        eventChip: EventChip<T>,
        canvas: Canvas
    ) {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.rect)

        val negativeWidth = rect.right - rect.left - (config.eventPadding * 2f) < 0
        val negativeHeight = rect.bottom - rect.top - (config.eventPadding * 2f) < 0
        if (negativeWidth || negativeHeight) {
            return
        }

        val title = when (val resource = event.titleResource) {
            is WeekViewEvent.TextResource.Id -> context.getString(resource.resId)
            is WeekViewEvent.TextResource.Value -> resource.text
            null -> throw IllegalStateException("Invalid title resource: $resource")
        }

        val text = SpannableStringBuilder(title)
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)

        val location = when (val resource = event.locationResource) {
            is WeekViewEvent.TextResource.Id -> context.getString(resource.resId)
            is WeekViewEvent.TextResource.Value -> resource.text
            null -> null
        }

        location?.let {
            text.append(' ')
            text.append(it)
        }

        val availableHeight = (rect.bottom - rect.top - (config.eventPadding * 2f)).toInt()
        val availableWidth = (rect.right - rect.left - (config.eventPadding * 2f)).toInt()

        if (availableHeight == 0 || availableWidth == 0) {
            return
        }

        // Get text dimensions.
        val didAvailableAreaChange = eventChip.didAvailableAreaChange(rect, config.eventPadding)
        val isCached = textLayoutCache.containsKey(event.id)

        if (didAvailableAreaChange || !isCached) {
            val textPaint = event.getTextPaint(context, config)
            val textLayout = TextLayoutBuilder.build(text, textPaint, availableWidth)
            val lineHeight = textLayout.lineHeight

            val finalTextLayout = if (availableHeight >= lineHeight) {
                // The text fits into the chip, so we just need to ellipsize it
                ellipsizeTextToFitChip(eventChip, text, textLayout, availableHeight, availableWidth)
            } else if (config.adaptiveEventTextSize) {
                // The text doesn't fit into the chip, so we need to gradually reduce its size
                // until it does
                scaleTextIntoChip(eventChip, text, textLayout, availableHeight, availableWidth)
            } else {
                textLayout
            }

            textLayoutCache[event.id] = finalTextLayout
            eventChip.updateAvailableArea(availableWidth, availableHeight)
        }

        val textLayout = textLayoutCache[event.id] ?: return
        if (textLayout.height <= availableHeight) {
            drawEventTitle(eventChip, textLayout, canvas)
        }
    }

    private fun ellipsizeTextToFitChip(
        eventChip: EventChip<T>,
        text: CharSequence,
        staticLayout: StaticLayout,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.rect)

        // The text fits into the chip, so we just need to ellipsize it
        var textLayout = staticLayout

        val textPaint = event.getTextPaint(context, config)
        val lineHeight = textLayout.lineHeight

        var availableLineCount = availableHeight / lineHeight

        do {
            // Ellipsize text to fit into event rect.
            val availableArea = availableLineCount * availableWidth
            val ellipsized = ellipsize(text, textPaint, availableArea.toFloat(), TruncateAt.END)

            val width = (rect.right - rect.left - (config.eventPadding * 2).toFloat()).toInt()
            textLayout = TextLayoutBuilder.build(ellipsized, textPaint, width)

            // Repeat until text is short enough.
            availableLineCount--
        } while (textLayout.height > availableHeight)

        return textLayout
    }

    private fun scaleTextIntoChip(
        eventChip: EventChip<T>,
        text: CharSequence,
        staticLayout: StaticLayout,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.rect)

        // The text doesn't fit into the chip, so we need to gradually reduce its size until it does
        var textLayout = staticLayout
        val textPaint = event.getTextPaint(context, config)

        do {
            textPaint.textSize -= 1f

            val adaptiveLineCount = availableHeight / textLayout.lineHeight
            val availableArea = adaptiveLineCount * availableWidth
            val ellipsized = ellipsize(text, textPaint, availableArea.toFloat(), TruncateAt.END)

            val width = (rect.right - rect.left - (config.eventPadding * 2).toFloat()).toInt()
            textLayout = TextLayoutBuilder.build(ellipsized, textPaint, width)
        } while (availableHeight <= textLayout.lineHeight)

        return textLayout
    }

    private fun setBackgroundPaint(
        event: WeekViewEvent<T>,
        paint: Paint
    ) {
        val resource = event.style.getBackgroundColorOrDefault(config)
        paint.color = when (resource) {
            is WeekViewEvent.ColorResource.Id -> ContextCompat.getColor(context, resource.resId)
            is WeekViewEvent.ColorResource.Value -> resource.color
        }
        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
    }

    private fun setBorderPaint(
        event: WeekViewEvent<T>,
        paint: Paint
    ) {
        paint.color = when (val resource = event.style.borderColorResource) {
            is WeekViewEvent.ColorResource.Id -> ContextCompat.getColor(context, resource.resId)
            is WeekViewEvent.ColorResource.Value -> resource.color
            null -> 0
        }
        paint.strokeWidth = event.style.borderWidth.toFloat()
        paint.style = Paint.Style.STROKE
    }

}
