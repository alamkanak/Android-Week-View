package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint

internal interface Updater {
    val isRequired: Boolean
    fun update(drawingContext: DrawingContext)
}

internal interface Drawer {
    fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas,
        paint: Paint
    ) = Unit
}

internal interface CachingDrawer : Drawer {
    fun clear()
}
