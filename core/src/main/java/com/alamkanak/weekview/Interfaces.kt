package com.alamkanak.weekview

import android.graphics.Canvas

internal interface Updater {
    fun isRequired(drawingContext: DrawingContext): Boolean
    fun update(drawingContext: DrawingContext)
}

internal interface Drawer {
    fun draw(drawingContext: DrawingContext, canvas: Canvas)
}

internal interface CachingDrawer : Drawer {
    fun clear()
}
