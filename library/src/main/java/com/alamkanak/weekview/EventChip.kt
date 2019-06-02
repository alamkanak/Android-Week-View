package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils.TruncateAt.END
import android.text.TextUtils.ellipsize
import android.text.style.StyleSpan
import android.view.MotionEvent

/**
 * A class to hold reference to the events and their visual representation. An EventRect is
 * actually the rectangle that is drawn on the calendar for a given event. There may be more
 * than one rectangle for a single event (an event that expands more than one day). In that
 * case two instances of the EventRect will be used for a single event. The given event will be
 * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
 * instance will be stored in "event".
 */
internal class EventChip<T>(
    val event: WeekViewEvent<T>,
    val originalEvent: WeekViewEvent<T>,
    var rect: RectF?
) {

    var left = 0f
    var width = 0f
    var top = 0f
    var bottom = 0f

    private var layoutCache: StaticLayout? = null
    private var availableWidthCache: Int = 0
    private var availableHeightCache: Int = 0

    fun clearCache() {
        rect = null
        availableWidthCache = 0
        availableHeightCache = 0
    }

    internal fun draw(
        config: WeekViewConfigWrapper,
        canvas: Canvas,
        paint: Paint
    ) {
        draw(config, null, canvas, paint)
    }

    internal fun draw(
        config: WeekViewConfigWrapper,
        textLayout: StaticLayout?,
        canvas: Canvas,
        paint: Paint
    ) {
        val cornerRadius = config.eventCornerRadius.toFloat()
        setBackgroundPaint(config, paint)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        if (event.style.hasBorder) {
            setBorderPaint(paint)
            val borderWidth = event.style.borderWidth

            val rect = checkNotNull(rect)
            val adjustedRect = RectF(
                rect.left + borderWidth / 2f,
                rect.top + borderWidth / 2f,
                rect.right - borderWidth / 2f,
                rect.bottom - borderWidth / 2f)
            canvas.drawRoundRect(adjustedRect, cornerRadius, cornerRadius, paint)
        }

        if (event.isNotAllDay) {
            drawCornersForMultiDayEvents(paint, cornerRadius, canvas)
        }

        textLayout?.let {
            // The text height has already been calculated
            drawEventTitle(config, it, canvas)
        } ?: calculateTextHeightAndDrawTitle(config, canvas)
    }

    private fun drawCornersForMultiDayEvents(
        backgroundPaint: Paint,
        cornerRadius: Float,
        canvas: Canvas
    ) {
        val rect = checkNotNull(rect)

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

    private fun calculateTextHeightAndDrawTitle(
        config: WeekViewConfigWrapper,
        canvas: Canvas
    ) {
        val rect = checkNotNull(rect)

        val negativeWidth = rect.right - rect.left - (config.eventPadding * 2f) < 0
        val negativeHeight = rect.bottom - rect.top - (config.eventPadding * 2f) < 0
        if (negativeWidth || negativeHeight) {
            return
        }

        // Prepare the name of the event.
        val text = SpannableStringBuilder(event.title)
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)

        // Prepare the location of the event.
        if (event.location != null) {
            text.append(' ')
            text.append(event.location)
        }

        val availableHeight = (rect.bottom - rect.top - (config.eventPadding * 2f)).toInt()
        val availableWidth = (rect.right - rect.left - (config.eventPadding * 2f)).toInt()

        // Get text dimensions.
        val didAvailableAreaChange =
            availableWidth != availableWidthCache || availableHeight != availableHeightCache
        val isCached = layoutCache != null

        if (didAvailableAreaChange || !isCached) {
            val textPaint = event.getTextPaint(config)
            val textLayout = StaticLayout(text,
                textPaint, availableWidth, ALIGN_NORMAL, 1.0f, 0.0f, false)

            val lineHeight = textLayout.lineHeight

            val finalTextLayout = if (availableHeight >= lineHeight) {
                // The text fits into the chip, so we just need to ellipsize it
                ellipsizeTextToFitChip(text, textLayout, config, availableHeight, availableWidth)
            } else if (config.adaptiveEventTextSize) {
                // The text doesn't fit into the chip, so we need to gradually reduce its size
                // until it does
                scaleTextIntoChip(text, textLayout, config, availableHeight, availableWidth)
            } else {
                textLayout
            }

            availableWidthCache = availableWidth
            availableHeightCache = availableHeight
            layoutCache = finalTextLayout
        }

        layoutCache?.let {
            if (it.height <= availableHeight) {
                drawEventTitle(config, it, canvas)
            }
        }
    }

    private fun ellipsizeTextToFitChip(
        text: CharSequence,
        staticLayout: StaticLayout,
        config: WeekViewConfigWrapper,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        // The text fits into the chip, so we just need to ellipsize it
        var textLayout = staticLayout

        val textPaint = event.getTextPaint(config)
        val lineHeight = textLayout.lineHeight

        var availableLineCount = availableHeight / lineHeight
        val rect = checkNotNull(rect)

        do {
            // Ellipsize text to fit into event rect.
            val availableArea = availableLineCount * availableWidth
            val ellipsized = ellipsize(text, textPaint, availableArea.toFloat(), END)

            val width = (rect.right - rect.left - (config.eventPadding * 2).toFloat()).toInt()
            textLayout = StaticLayout(ellipsized, textPaint, width, ALIGN_NORMAL, 1.0f, 0.0f, false)

            // Repeat until text is short enough.
            availableLineCount--
        } while (textLayout.height > availableHeight)

        return textLayout
    }

    private fun scaleTextIntoChip(
        text: CharSequence,
        staticLayout: StaticLayout,
        config: WeekViewConfigWrapper,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        // The text doesn't fit into the chip, so we need to gradually reduce its size until it does
        var textLayout = staticLayout
        val textPaint = event.getTextPaint(config)
        val rect = checkNotNull(rect)

        do {
            textPaint.textSize -= 1f

            val adaptiveLineCount = availableHeight / textLayout.lineHeight
            val availableArea = adaptiveLineCount * availableWidth
            val ellipsized = ellipsize(text, textPaint, availableArea.toFloat(), END)

            val width = (rect.right - rect.left - (config.eventPadding * 2).toFloat()).toInt()
            textLayout = StaticLayout(ellipsized, textPaint, width, ALIGN_NORMAL, 1.0f, 0.0f, false)
        } while (availableHeight <= textLayout.lineHeight)

        return textLayout
    }

    private fun drawEventTitle(
        config: WeekViewConfigWrapper,
        textLayout: StaticLayout,
        canvas: Canvas
    ) {
        val rect = checkNotNull(rect)
        canvas.apply {
            save()
            translate(rect.left + config.eventPadding, rect.top + config.eventPadding)
            textLayout.draw(this)
            restore()
        }
    }

    private fun setBackgroundPaint(config: WeekViewConfigWrapper, paint: Paint) {
        paint.color = event.style.getBackgroundColorOrDefault(config)
        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
    }

    private fun setBorderPaint(paint: Paint) {
        paint.color = event.style.borderColor
        paint.strokeWidth = event.style.borderWidth.toFloat()
        paint.style = Paint.Style.STROKE
    }

    fun calculateTopAndBottom(config: WeekViewConfigWrapper) {
        if (event.isNotAllDay) {
            top = event.getEffectiveStartMinutes(config).toFloat()
            bottom = event.getEffectiveEndMinutes(config).toFloat()
        } else {
            // TODO
            top = 0f
            bottom = 100f
        }
    }

    fun isHit(e: MotionEvent): Boolean {
        return rect?.let {
            e.x > it.left && e.x < it.right && e.y > it.top && e.y < it.bottom
        } ?: false
    }

}
