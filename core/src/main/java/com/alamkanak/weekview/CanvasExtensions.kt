package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.StaticLayout

internal fun Canvas.withTranslation(x: Float, y: Float, block: Canvas.() -> Unit) {
    save()
    translate(x, y)
    block()
    restore()
}

internal fun Canvas.draw(staticLayout: StaticLayout) {
    staticLayout.draw(this)
}

internal fun Canvas.drawVerticalLine(
    horizontalOffset: Float,
    startY: Float,
    endY: Float,
    paint: Paint
) {
    drawLine(horizontalOffset, startY, horizontalOffset, endY, paint)
}

internal fun Canvas.drawHorizontalLine(
    verticalOffset: Float,
    startX: Float,
    endX: Float,
    paint: Paint
) {
    drawLine(startX, verticalOffset, endX, verticalOffset, paint)
}

internal fun Canvas.drawInBounds(
    bounds: RectF,
    block: Canvas.() -> Unit
) {
    save()
    clipRect(bounds)
    block()
    restore()
}

internal fun RectF.insetBy(inset: Float): RectF {
    return RectF(this).apply {
        inset(inset, inset)
    }
}

internal val RectF.isNotEmpty: Boolean
    get() = !isEmpty
