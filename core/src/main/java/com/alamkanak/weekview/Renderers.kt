package com.alamkanak.weekview

import android.graphics.Canvas
import java.util.Calendar

internal interface Updater {
    fun update()
}

internal interface Drawer {
    fun draw(canvas: Canvas)
}

typealias DateFormatter = (Calendar) -> String

internal interface DateFormatterDependent {
    fun onDateFormatterChanged(formatter: DateFormatter)
}

typealias TimeFormatter = (Int) -> String

internal interface TimeFormatterDependent {
    fun onTimeFormatterChanged(formatter: TimeFormatter)
}

internal interface Renderer {
    fun onSizeChanged(width: Int, height: Int) = Unit
    fun render(canvas: Canvas)
}
