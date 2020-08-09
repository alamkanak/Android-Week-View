package com.alamkanak.weekview

import android.graphics.Canvas

internal interface Updater {
    fun isRequired(): Boolean
    fun update()
}

internal interface Drawer {
    fun draw(canvas: Canvas)
}
