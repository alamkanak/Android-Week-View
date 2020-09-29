package com.alamkanak.weekview

import android.graphics.Canvas

internal interface Updater {
    fun update()
}

internal interface Drawer {
    fun draw(canvas: Canvas)
}

internal interface DateFormatterDependent {
    fun onDateFormatterChanged(formatter: DateFormatter)
}

internal interface TimeFormatterDependent {
    fun onTimeFormatterChanged(formatter: TimeFormatter)
}

internal interface Renderer {
    fun onSizeChanged(width: Int, height: Int) = Unit
    fun render(canvas: Canvas)
}
