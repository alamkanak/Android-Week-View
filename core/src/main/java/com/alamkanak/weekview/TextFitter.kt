package com.alamkanak.weekview

import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
import android.text.TextUtils.TruncateAt

internal class TextFitter(
    private val viewState: ViewState
) {

    private val spannableStringBuilder = SpannableStringBuilder()

    fun fit(
        eventChip: EventChip,
        title: CharSequence,
        location: CharSequence?,
        chipHeight: Int,
        chipWidth: Int
    ): StaticLayout {
        val text = combineTitleAndLocation(title, location, isMultiLine = true)
        val textPaint = viewState.getTextPaint(eventChip.event)
        val textLayout = text.toTextLayout(textPaint, chipWidth)

        val fitsIntoChip = chipHeight >= textLayout.height
        if (fitsIntoChip) {
            return ellipsize(eventChip, textLayout, text, chipHeight, chipWidth)
        }

        val modifiedText = combineTitleAndLocation(title, location, isMultiLine = false)
        val modifiedTextLayout = text.toTextLayout(textPaint, chipWidth)

        val fitsIntoChipNow = chipHeight >= modifiedTextLayout.height
        val isAdaptive = viewState.adaptiveEventTextSize

        return when {
            fitsIntoChipNow || !isAdaptive -> {
                ellipsize(eventChip, modifiedTextLayout, modifiedText, chipHeight, chipWidth)
            }
            isAdaptive -> scaleToFit(eventChip, modifiedText, chipHeight)
            else -> modifiedTextLayout
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

    private fun ellipsize(
        eventChip: EventChip,
        textLayout: StaticLayout,
        text: CharSequence,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.bounds)

        // The text fits into the chip, so we just need to ellipsize it
        var newTextLayout = textLayout
        val textPaint = viewState.getTextPaint(event)

        var availableLineCount = availableHeight / newTextLayout.lineHeight
        val fullHorizontalPadding = viewState.eventPaddingHorizontal * 2f
        val width = (rect.right - rect.left - fullHorizontalPadding).toInt()

        do {
            // Ellipsize text to fit into event rect
            val availableArea = availableLineCount * availableWidth * 1f
            val ellipsized = TextUtils.ellipsize(text, textPaint, availableArea, TruncateAt.END)
            newTextLayout = ellipsized.toTextLayout(textPaint, width)
            availableLineCount--
        } while (newTextLayout.height > availableHeight && availableLineCount > 0)

        return newTextLayout
    }

    private fun scaleToFit(
        eventChip: EventChip,
        text: CharSequence,
        availableHeight: Int
    ): StaticLayout {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.bounds)

        val textPaint = viewState.getTextPaint(event)
        val fullHorizontalPadding = viewState.eventPaddingHorizontal * 2f
        val width = (rect.right - rect.left - fullHorizontalPadding).toInt()

        var textLayout: StaticLayout

        do {
            // The text doesn't fit into the chip, so we need to gradually
            // reduce its size until it does
            textPaint.textSize -= 1
            textLayout = text.toTextLayout(textPaint, width)
        } while (availableHeight < textLayout.height)

        return textLayout
    }
}
