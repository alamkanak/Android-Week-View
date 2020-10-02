package com.alamkanak.weekview

import android.text.SpannableStringBuilder
import android.text.StaticLayout
import kotlin.math.roundToInt

internal class TextFitter(
    private val viewState: ViewState
) {

    private val spannableStringBuilder = SpannableStringBuilder()

    fun fit(eventChip: EventChip): StaticLayout {
        return if (eventChip.event.isAllDay) {
            fitAllDayEvent(eventChip)
        } else {
            fitSingleEvent(eventChip)
        }
    }

    private fun fitAllDayEvent(eventChip: EventChip): StaticLayout {
        // TODO Ellipsize if RTL?
        val textPaint = viewState.getTextPaint(eventChip.event)
        return eventChip.getText().toTextLayout(textPaint, width = Int.MAX_VALUE)
    }

    private fun fitSingleEvent(eventChip: EventChip): StaticLayout {
        val bounds = eventChip.bounds
        val availableHeight = bounds.height().roundToInt() - viewState.eventPaddingVertical * 2
        val availableWidth = bounds.width().roundToInt() - viewState.eventPaddingVertical * 2
        return eventChip.fitText(availableWidth, availableHeight)
    }

    private fun EventChip.fitText(availableWidth: Int, availableHeight: Int): StaticLayout {
        val textPaint = viewState.getTextPaint(event)

        var text = getText(includeLocation = true)
        var textLayout = text.toTextLayout(textPaint, width = availableWidth)

        val fitsCompletely = textLayout.height <= availableHeight
        if (fitsCompletely) {
            return textLayout
        }

        text = getText(includeLocation = false)
        textLayout = text.toTextLayout(textPaint, width = availableWidth)

        val titleOnlyFits = textLayout.height <= availableHeight
        if (titleOnlyFits) {
            return textLayout
        }

        while (textLayout.height > availableHeight && textLayout.lineCount > 1) {
            // Remove the last lines until there's only a single line left. If it doesn't fit
            // by that point, we need to reduce the text size.
            val startOfLastLine = textLayout.getLineStart(textLayout.lineCount)
            text = text.substring(startIndex = 0, endIndex = startOfLastLine - 1).trim()
            textLayout = text.toTextLayout(textPaint, width = availableWidth)
        }

        while (textLayout.height > availableHeight && viewState.adaptiveEventTextSize) {
            // Even a single line doesn't fit. We need to reduce the text size.
            textPaint.textSize -= 1
            textLayout = text.toTextLayout(textPaint, width = Int.MAX_VALUE)
        }

        return textLayout
    }

    private fun EventChip.getText(includeLocation: Boolean = false): CharSequence {
        return if (event.isAllDay) {
            val title = event.title.emojified
            combineTitleAndLocation(title, location = null, isMultiLine = false)
        } else {
            val title = event.title.emojified
            val location = event.location?.emojified.takeIf { includeLocation }
            combineTitleAndLocation(title, location, isMultiLine = true)
        }
    }

    private fun combineTitleAndLocation(
        title: CharSequence,
        location: CharSequence?,
        isMultiLine: Boolean
    ): CharSequence = when (location) {
        null -> title
        else -> {
            val separator = if (isMultiLine) "\n" else " "
            spannableStringBuilder.clear()
            spannableStringBuilder
                .append(title)
                .append(separator)
                .append(location)
                .build()
        }
    }
}
