package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.alamkanak.weekview.WeekViewEntity.Style.Pattern
import com.alamkanak.weekview.WeekViewEntity.Style.Pattern.Lined.Direction
import com.alamkanak.weekview.WeekViewEntity.Style.Pattern.Lined.Direction.EndToStart
import com.alamkanak.weekview.WeekViewEntity.Style.Pattern.Lined.Direction.StartToEnd
import kotlin.math.sqrt

private data class Line(val startX: Float, val startY: Float, val stopX: Float, val stopY: Float)

internal fun Canvas.drawPattern(
    pattern: Pattern,
    bounds: RectF,
    isLtr: Boolean,
    paint: Paint
) {
    paint.color = pattern.color
    paint.strokeWidth = pattern.strokeWidth.toFloat()

    when (pattern) {
        is Pattern.Lined -> drawDiagonalLines(bounds, pattern.spacing, isLtr, pattern.direction, paint)
        is Pattern.Dotted -> drawDots(bounds, pattern.spacing, paint)
    }
}

internal fun Canvas.drawDiagonalLines(
    bounds: RectF,
    spacing: Int,
    isLtr: Boolean,
    direction: Direction,
    paint: Paint
) {
    val mustStartLeft = (isLtr && direction == StartToEnd) || (!isLtr && direction == EndToStart)
    val lines = mutableListOf<Line>()
    var startX = if (mustStartLeft) bounds.left else bounds.right

    // Draw all the lines to the right of the top-left corner (flipped for RTL)
    if (mustStartLeft) {
        while (startX <= bounds.right) {
            lines += calculateDiagonalLine(
                startX = startX,
                startY = bounds.top,
                stopY = bounds.bottom,
                drawLeftToRight = mustStartLeft
            )
            startX += spacing
        }
    } else {
        while (startX >= bounds.left) {
            lines += calculateDiagonalLine(
                startX = startX,
                startY = bounds.top,
                stopY = bounds.bottom,
                drawLeftToRight = mustStartLeft
            )
            startX -= spacing
        }
    }

    // Now, draw the lines to the left of the top-left corner (flipped for RTL)
    var endX = if (mustStartLeft) bounds.right else bounds.left
    startX = if (mustStartLeft) bounds.left else bounds.right

    if (mustStartLeft) {
        while (endX >= bounds.left) {
            lines += calculateDiagonalLine(
                startX = startX,
                startY = bounds.top,
                stopY = bounds.bottom,
                drawLeftToRight = mustStartLeft
            )
            startX -= spacing
            endX -= spacing
        }
    } else {
        while (endX <= bounds.right) {
            lines += calculateDiagonalLine(
                startX = startX,
                startY = bounds.top,
                stopY = bounds.bottom,
                drawLeftToRight = mustStartLeft
            )
            startX += spacing
            endX += spacing
        }
    }

    paint.style = Paint.Style.STROKE
    for (line in lines) {
        drawLine(line.startX, line.startY, line.stopX, line.stopY, paint)
    }
}

private fun calculateDiagonalLine(
    startX: Float,
    startY: Float,
    stopY: Float,
    drawLeftToRight: Boolean
): Line {
    val height = stopY - startY
    val width = height / sqrt(2f)
    val stopX = if (drawLeftToRight) startX + width else startX - width
    return Line(startX, startY, stopX, stopY)
}

internal fun Canvas.drawDots(bounds: RectF, spacing: Int, paint: Paint) {
    paint.style = Paint.Style.FILL
    val strokeWidth = paint.strokeWidth

    val paddedDot = strokeWidth + spacing
    val horizontalDots = (bounds.width() / paddedDot).toInt()
    val verticalDots = (bounds.height() / paddedDot).toInt()

    val dotsWidth = horizontalDots * paddedDot
    val dotsHeight = verticalDots * paddedDot

    val horizontalPadding = bounds.width() - dotsWidth
    val verticalPadding = bounds.height() - dotsHeight

    val left = bounds.left + horizontalPadding / 2
    val top = bounds.top + verticalPadding / 2

    for (horizontalDot in 0 until horizontalDots) {
        for (verticalDot in 0 until verticalDots) {
            val leftBound = left + horizontalDot * paddedDot
            val topBound = top + verticalDot * paddedDot
            val radius = paint.strokeWidth / 2
            val x = leftBound + paddedDot / 2
            val y = topBound + paddedDot / 2
            drawCircle(x, y, radius, paint)
        }
    }
}
