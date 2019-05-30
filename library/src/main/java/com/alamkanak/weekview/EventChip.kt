package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
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

        if (event.hasBorder) {
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

        if (!event.hasBorder) {
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

        val negativeWidth = rect.right - rect.left - (config.eventPadding * 2).toFloat() < 0
        val negativeHeight = rect.bottom - rect.top - (config.eventPadding * 2).toFloat() < 0
        if (negativeWidth || negativeHeight) {
            return
        }

        // Prepare the name of the event.
        val stringBuilder = SpannableStringBuilder(event.title)
        stringBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, stringBuilder.length, 0)

        // Prepare the location of the event.
        if (event.location != null) {
            stringBuilder.append(' ')
            stringBuilder.append(event.location)
        }

        val availableHeight = (rect.bottom - rect.top - (config.eventPadding * 2).toFloat()).toInt()
        val availableWidth = (rect.right - rect.left - (config.eventPadding * 2).toFloat()).toInt()

        // Get text dimensions.
        val didAvailableAreaChange = availableWidth != availableWidthCache || availableHeight != availableHeightCache
        val isCached = layoutCache != null

        if (didAvailableAreaChange || !isCached) {
            val textPaint = event.getTextPaint(config)
            var textLayout = StaticLayout(stringBuilder,
                textPaint, availableWidth, ALIGN_NORMAL, 1.0f, 0.0f, false)

            val lineHeight = textLayout.height / textLayout.lineCount

            if (availableHeight >= lineHeight) {
                var availableLineCount = availableHeight / lineHeight
                do {
                    // TODO: Don't truncate
                    // Ellipsize text to fit into event rect.
                    val availableArea = availableLineCount * availableWidth
                    val ellipsized = TextUtils.ellipsize(stringBuilder,
                        textPaint, availableArea.toFloat(), TextUtils.TruncateAt.END)

                    val width = (rect.right - rect.left - (config.eventPadding * 2).toFloat()).toInt()
                    textLayout = StaticLayout(ellipsized, textPaint, width, ALIGN_NORMAL, 1.0f, 0.0f, false)

                    // Repeat until text is short enough.
                    availableLineCount--
                } while (textLayout.height > availableHeight)
            }

            availableWidthCache = availableWidth
            availableHeightCache = availableHeight
            layoutCache = textLayout
        }

        layoutCache?.let {
            drawEventTitle(config, it, canvas)
        }
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
        paint.color = event.getColorOrDefault(config)
        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
    }

    private fun setBorderPaint(paint: Paint) {
        paint.color = event.style.borderColor
        paint.strokeWidth = event.style.borderWidth.toFloat()
        paint.style = Paint.Style.STROKE
    }

    fun isHit(e: MotionEvent): Boolean {
        return rect?.let {
            e.x > it.left && e.x < it.right && e.y > it.top && e.y < it.bottom
        } ?: false
    }

}
