package com.alamkanak.weekview

import android.graphics.Canvas

fun Canvas.withTranslation(x: Float, y: Float, block: Canvas.() -> Unit) {
    save()
    translate(x, y)
    block()
    restore()
}

fun Canvas.drawInRect(
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
