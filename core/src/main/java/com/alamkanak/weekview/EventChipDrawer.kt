package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.StaticLayout

internal class EventChipDrawer(
    private val viewState: ViewState
) {

    private val dragShadow: Int by lazy {
        Color.parseColor("#757575")
    }

    private val backgroundPaint = Paint()
    private val borderPaint = Paint()

    private val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    internal fun draw(
        eventChip: EventChip,
        canvas: Canvas,
        textLayout: StaticLayout?
    ) = with(canvas) {
        val entity = eventChip.event
        val bounds = eventChip.bounds
        val cornerRadius = (entity.style.cornerRadius ?: viewState.eventCornerRadius).toFloat()

        val isBeingDragged = entity.id == viewState.dragState?.eventId
        updateBackgroundPaint(entity, isBeingDragged, backgroundPaint)
        drawRoundRect(bounds, cornerRadius, cornerRadius, backgroundPaint)

        val pattern = entity.style.pattern
        if (pattern != null) {
            drawPattern(
                pattern = pattern,
                bounds = eventChip.bounds,
                isLtr = viewState.isLtr,
                paint = patternPaint
            )
        }

        val borderWidth = entity.style.borderWidth
        if (borderWidth != null && borderWidth > 0) {
            updateBorderPaint(entity, borderPaint)
            val borderBounds = bounds.insetBy(borderWidth / 2f)
            drawRoundRect(borderBounds, cornerRadius, cornerRadius, borderPaint)
        }

        if (entity.isMultiDay && entity.isNotAllDay) {
            drawCornersForMultiDayEvents(eventChip, cornerRadius)
        }

        if (textLayout != null) {
            drawEventTitle(eventChip, textLayout)
        }
    }

    private fun Canvas.drawCornersForMultiDayEvents(
        eventChip: EventChip,
        cornerRadius: Float
    ) {
        val event = eventChip.event
        val bounds = eventChip.bounds

        val isBeingDragged = event.id == viewState.dragState?.eventId
        updateBackgroundPaint(event, isBeingDragged, backgroundPaint)

        if (eventChip.startsOnEarlierDay) {
            val topRect = RectF(bounds)
            topRect.bottom = topRect.top + cornerRadius
            drawRect(topRect, backgroundPaint)
        }

        if (eventChip.endsOnLaterDay) {
            val bottomRect = RectF(bounds)
            bottomRect.top = bottomRect.bottom - cornerRadius
            drawRect(bottomRect, backgroundPaint)
        }

        if (event.style.borderWidth != null) {
            drawMultiDayBorderStroke(eventChip, cornerRadius)
        }
    }

    private fun Canvas.drawMultiDayBorderStroke(
        eventChip: EventChip,
        cornerRadius: Float
    ) {
        val event = eventChip.event
        val bounds = eventChip.bounds

        val borderWidth = event.style.borderWidth ?: 0
        val borderStart = bounds.left + borderWidth / 2
        val borderEnd = bounds.right - borderWidth / 2

        updateBorderPaint(event, backgroundPaint)

        if (eventChip.startsOnEarlierDay) {
            drawVerticalLine(
                horizontalOffset = borderStart,
                startY = bounds.top,
                endY = bounds.top + cornerRadius,
                paint = backgroundPaint
            )

            drawVerticalLine(
                horizontalOffset = borderEnd,
                startY = bounds.top,
                endY = bounds.top + cornerRadius,
                paint = backgroundPaint
            )
        }

        if (eventChip.endsOnLaterDay) {
            drawVerticalLine(
                horizontalOffset = borderStart,
                startY = bounds.bottom - cornerRadius,
                endY = bounds.bottom,
                paint = backgroundPaint
            )

            drawVerticalLine(
                horizontalOffset = borderEnd,
                startY = bounds.bottom - cornerRadius,
                endY = bounds.bottom,
                paint = backgroundPaint
            )
        }
    }

    private fun Canvas.drawEventTitle(
        eventChip: EventChip,
        textLayout: StaticLayout
    ) {
        val bounds = eventChip.bounds

        val horizontalOffset = if (viewState.isLtr) {
            bounds.left + viewState.eventPaddingHorizontal
        } else {
            bounds.right - viewState.eventPaddingHorizontal
        }

        val verticalOffset = if (eventChip.event.isAllDay) {
            (bounds.height() - textLayout.height) / 2f
        } else {
            viewState.eventPaddingVertical.toFloat()
        }

        withTranslation(x = horizontalOffset, y = bounds.top + verticalOffset) {
            draw(textLayout)
        }
    }

    private fun updateBackgroundPaint(
        entity: ResolvedWeekViewEntity,
        isBeingDragged: Boolean,
        paint: Paint
    ) = with(paint) {
        color = entity.style.backgroundColor ?: viewState.defaultEventColor
        isAntiAlias = true
        strokeWidth = 0f
        style = Paint.Style.FILL

        if (isBeingDragged) {
            setShadowLayer(12f, 0f, 0f, dragShadow)
        } else {
            clearShadowLayer()
        }
    }

    private fun updateBorderPaint(
        entity: ResolvedWeekViewEntity,
        paint: Paint
    ) = with(paint) {
        color = entity.style.borderColor ?: viewState.defaultEventColor
        isAntiAlias = true
        strokeWidth = entity.style.borderWidth?.toFloat() ?: 0f
        style = Paint.Style.STROKE
    }
}
