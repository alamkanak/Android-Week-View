package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.roundToInt

internal class HeaderRowDrawer<T : Any>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper
) : Drawer {

    override fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas
    ) {
        val width = view.width.toFloat()
        canvas.drawRect(0f, 0f, width, config.headerHeight, config.headerBackgroundPaint)

        if (config.showWeekNumber) {
            canvas.drawWeekNumber(drawingContext)
        }

        if (config.showHeaderRowBottomLine) {
            val y = config.headerHeight - config.headerRowBottomLineWidth
            canvas.drawLine(0f, y, width, y, config.headerRowBottomLinePaint)
        }
    }

    private fun Canvas.drawWeekNumber(drawingContext: DrawingContext) {
        val weekNumber = drawingContext.dateRange.first().weekOfYear.toString()

        val bounds = config.weekNumberBounds
        val textPaint = config.weekNumberTextPaint

        val textHeight = textPaint.textHeight
        val textOffset = (textHeight / 2f).roundToInt() - textPaint.descent().roundToInt()

        val width = textPaint.getTextBounds("52").width() * 2.5f
        val height = textHeight * 1.5f

        val backgroundRect = RectF(
            bounds.centerX() - width / 2f,
            bounds.centerY() - height / 2f,
            bounds.centerX() + width / 2f,
            bounds.centerY() + height / 2f
        )

        drawRect(bounds, config.headerBackgroundPaint)

        val backgroundPaint = config.weekNumberBackgroundPaint
        val radius = config.weekNumberBackgroundCornerRadius.toFloat()
        drawRoundRect(backgroundRect, radius, radius, backgroundPaint)

        drawText(weekNumber, bounds.centerX(), bounds.centerY() + textOffset, textPaint)
    }
}

internal val Paint.textHeight: Int
    get() = (descent() - ascent()).roundToInt()

internal fun Paint.getTextBounds(text: String): Rect {
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)
    return rect
}
