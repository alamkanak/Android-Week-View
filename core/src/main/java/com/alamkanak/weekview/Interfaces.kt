package com.alamkanak.weekview

import android.graphics.Canvas

internal interface Updater {
    val isRequired: Boolean
    fun update(drawingContext: DrawingContext)
}

internal interface Drawer {
    fun draw(
        drawingContext: DrawingContext,
        canvas: Canvas
    ) = Unit
}

internal interface CachingDrawer : Drawer {
    fun clear()
}
