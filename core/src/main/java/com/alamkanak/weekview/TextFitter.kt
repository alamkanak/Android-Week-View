package com.alamkanak.weekview

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.TextUtils.TruncateAt
import android.text.style.StyleSpan

internal class TextFitter<T>(
    private val context: Context,
    private val config: WeekViewConfigWrapper
) {

    fun fit(
        eventChip: EventChip<T>,
        text: SpannableStringBuilder,
        chipHeight: Int,
        chipWidth: Int
    ): StaticLayout {
        val textPaint = eventChip.event.getTextPaint(context, config)
        val textLayout = TextLayoutBuilder.build(text, textPaint, chipWidth)

        val fitsIntoChip = chipHeight >= textLayout.height
        if (fitsIntoChip) {
            return ellipsize(eventChip, textLayout, text, chipHeight, chipWidth)
        }

        val isMultiLine = text.contains("\n")
        val finalText = if (isMultiLine) text.replaceNewLineWithSpace() else text

        val fitsIntoChipNow = chipHeight >= textLayout.height
        val isAdaptive = config.adaptiveEventTextSize

        // TODO: Refactor adaptiveTextSize and ellipsize behavior

        return when {
            fitsIntoChipNow || !isAdaptive -> {
                ellipsize(eventChip, textLayout, finalText, chipHeight, chipWidth)
            }
            isAdaptive -> scaleToFit(eventChip, finalText, chipHeight)
            else -> textLayout
        }
    }

    private fun ellipsize(
        eventChip: EventChip<T>,
        textLayout: StaticLayout,
        text: SpannableStringBuilder,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.rect)

        // The text fits into the chip, so we just need to ellipsize it
        var newTextLayout = textLayout
        val textPaint = event.getTextPaint(context, config)

        var availableLineCount = availableHeight / newTextLayout.lineHeight
        val fullHorizontalPadding = config.eventPaddingHorizontal * 2f
        val width = (rect.right - rect.left - fullHorizontalPadding).toInt()

        do {
            // Ellipsize text to fit into event rect
            val availableArea = availableLineCount * availableWidth * 1f
            val ellipsized = TextUtils.ellipsize(text, textPaint, availableArea, TruncateAt.END)
            newTextLayout = TextLayoutBuilder.build(ellipsized, textPaint, width)
            availableLineCount--
        } while (newTextLayout.height > availableHeight && availableLineCount > 0)

        return newTextLayout
    }

    private fun SpannableStringBuilder.replaceNewLineWithSpace(): SpannableStringBuilder {
        val (title, location) = split("\n").toPair()
        val modifiedText = SpannableStringBuilder(title)
        modifiedText.setSpan(StyleSpan(Typeface.BOLD))

        if (location.isNotEmpty()) {
            modifiedText.append(" ")
            modifiedText.append(location)
        }

        return modifiedText
    }

    private fun scaleToFit(
        eventChip: EventChip<T>,
        text: SpannableStringBuilder,
        availableHeight: Int
    ): StaticLayout {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.rect)

        val textPaint = event.getTextPaint(context, config)
        val fullHorizontalPadding = config.eventPaddingHorizontal * 2f
        val width = (rect.right - rect.left - fullHorizontalPadding).toInt()

        var textLayout: StaticLayout

        do {
            // The text doesn't fit into the chip, so we need to gradually
            // reduce its size until it does
            textPaint.reduceSize()
            textLayout = TextLayoutBuilder.build(text, textPaint, width)
        } while (availableHeight < textLayout.height)

        return textLayout
    }

    private fun TextPaint.reduceSize() {
        textSize -= 1
    }

    private fun <T> List<T>.toPair(): Pair<T, T> {
        check(size == 2)
        return first() to last()
    }
}
