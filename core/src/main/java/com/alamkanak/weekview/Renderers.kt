package com.alamkanak.weekview

import android.graphics.Canvas
import java.time.LocalDate

internal interface Updater {
    fun update()
}

internal interface Drawer {
    fun draw(canvas: Canvas)
}

typealias DateFormatter = (LocalDate) -> String

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
