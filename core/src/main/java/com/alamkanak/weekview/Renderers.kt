package com.alamkanak.weekview

import android.graphics.Canvas

internal interface Updater {
    fun isRequired(): Boolean
    fun update()
}

internal interface Drawer {
    fun draw(canvas: Canvas)
}

// TODO

internal interface Renderer {
    fun onSizeChanged(width: Int, height: Int)
    fun render(canvas: Canvas)
}
