package com.alamkanak.weekview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.StaticLayout
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

        val rect = checkNotNull(eventChip.rect)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        if (event.style.hasBorder) {
            setBorderPaint(event, paint)
            val borderWidth = event.style.borderWidth

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
            // Remove top rounded corners by drawing a rectangle
            val borderStartY = rect.top
            val borderEndY = borderStartY + borderWidth
            val newRect = RectF(borderStartX, borderStartY, borderEndX, borderEndY)
            canvas.drawRect(newRect, backgroundPaint)
        }

        if (event.endsOnLaterDay(originalEvent)) {
            // Remove bottom rounded corners by drawing a rectangle
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

        val location = when (val resource = event.locationResource) {
            is WeekViewEvent.TextResource.Id -> context.getString(resource.resId)
            is WeekViewEvent.TextResource.Value -> resource.text
            null -> null
        }

        val text = SpannableStringBuilder(title)
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
        location?.let {
            text.appendln().append(it)
        }

        val chipHeight = (rect.bottom - rect.top - (config.eventPadding * 2f)).toInt()
        val chipWidth = (rect.right - rect.left - (config.eventPadding * 2f)).toInt()

        if (chipHeight == 0 || chipWidth == 0) {
            return
        }

        val didAvailableAreaChange = eventChip.didAvailableAreaChange(rect, config.eventPadding)
        val isCached = textLayoutCache.containsKey(event.id)

        if (didAvailableAreaChange || !isCached) {
            textLayoutCache[event.id] = textFitter.fit(eventChip, text, chipHeight, chipWidth)
            eventChip.updateAvailableArea(chipWidth, chipHeight)
        }

        val textLayout = textLayoutCache[event.id] ?: return
        if (textLayout.height <= chipHeight) {
            drawEventTitle(eventChip, textLayout, canvas)
        }
    }

    private val textFitter = TextFitter<T>(context, config)

    private fun setBackgroundPaint(
        event: WeekViewEvent<T>,
        paint: Paint
    ) {
        val resource = event.style.getBackgroundColorOrDefault(config)
        paint.color = when (resource) {
            is WeekViewEvent.ColorResource.Id -> ContextCompat.getColor(context, resource.resId)
            is WeekViewEvent.ColorResource.Value -> resource.color
        }
        paint.isAntiAlias = true
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
        paint.isAntiAlias = true
        paint.strokeWidth = event.style.borderWidth.toFloat()
        paint.style = Paint.Style.STROKE
    }
}
