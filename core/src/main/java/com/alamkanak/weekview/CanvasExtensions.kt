package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.RectF

fun Canvas.withTranslation(x: Float, y: Float, block: Canvas.() -> Unit) {
    save()
    translate(x, y)
    block()
    restore()
}

fun Canvas.drawInBounds(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    block: Canvas.() -> Unit
) {
    save()
    clipRect(left, top, right, bottom)
    block()
    restore()
}

fun RectF.insetBy(inset: Float): RectF {
    return RectF(this).apply {
        inset(inset, inset)
    }
}
