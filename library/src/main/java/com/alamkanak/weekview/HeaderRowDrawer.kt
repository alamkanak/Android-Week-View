package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint

internal class HeaderRowDrawer<T>(
        private val config: WeekViewConfigWrapper,
        private val cache: WeekViewCache<T>,
        private val viewState: WeekViewViewState
) {

    fun draw(drawingContext: DrawingContext, canvas: Canvas) {
        calculateAvailableSpaceForHeader(drawingContext)
        drawHeaderRow(canvas)
    }

    private fun calculateAvailableSpaceForHeader(drawingContext: DrawingContext) {
        config.timeColumnWidth = config.timeTextWidth + config.timeColumnPadding * 2
        refreshHeaderHeight(drawingContext)
    }

    private fun refreshHeaderHeight(drawingContext: DrawingContext) {
        val eventChips = cache.allDayEventChips
        if (eventChips.isEmpty()) {
            config.hasEventInHeader = false
            config.refreshHeaderHeight()
        }

        if (viewState.firstVisibleDay == null) {
            return
        }

        val dateRange = drawingContext.dateRange
        val visibleEvents = cache.getAllDayEventsInRange(dateRange)

        config.hasEventInHeader = visibleEvents.any { it.isAllDay }
        config.refreshHeaderHeight()
    }

    private fun drawHeaderRow(canvas: Canvas) {
        val width = WeekView.getViewWidth()

        canvas.restore()
        canvas.save()

        val headerBackground = config.headerBackgroundPaint

        // Hide everything in the top left corner
        val topLeftCornerWidth = config.timeTextWidth + config.timeColumnPadding * 2
        canvas.clipRect(0f, 0f, topLeftCornerWidth, config.headerHeight)
        canvas.drawRect(0f, 0f, topLeftCornerWidth, config.headerHeight, headerBackground)

        canvas.restore()
        canvas.save()

        // Clip to paint header row only.
        canvas.clipRect(config.timeColumnWidth, 0f, width.toFloat(), config.headerHeight)
        canvas.drawRect(0f, 0f, width.toFloat(), config.headerHeight, headerBackground)

        canvas.restore()
        canvas.save()

        if (config.showHeaderRowBottomLine) {
            drawHeaderBottomLine(width, canvas)
        }
    }

    private fun drawHeaderBottomLine(width: Int, canvas: Canvas) {
        val headerRowBottomLineWidth = config.headerRowBottomLinePaint.strokeWidth
        val topMargin = config.headerHeight - headerRowBottomLineWidth

        val paint = Paint().apply {
            strokeWidth = headerRowBottomLineWidth
            color = config.headerRowBottomLinePaint.color
        }

        canvas.drawLine(0f, topMargin, width.toFloat(), topMargin, paint)
    }

}
