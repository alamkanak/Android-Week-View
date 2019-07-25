package com.alamkanak.weekview

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils.TruncateAt
import android.text.TextUtils.ellipsize
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
            return textLayout.ellipsize(eventChip, text, chipHeight, chipWidth)
        }

        val isMultiLine = text.contains("\n")
        val finalText = if (isMultiLine) text.replaceNewLineWithSpace() else text

        val fitsIntoChipNow = chipHeight >= textLayout.height
        val isAdaptive = config.adaptiveEventTextSize

        return when {
            fitsIntoChipNow -> textLayout.ellipsize(eventChip, finalText, chipHeight, chipWidth)
            isAdaptive -> textLayout.scaleToFit(eventChip, finalText, chipHeight, chipWidth)
            else -> textLayout
        }
    }

    private fun StaticLayout.ellipsize(
        eventChip: EventChip<T>,
        text: SpannableStringBuilder,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.rect)

        // The text fits into the chip, so we just need to ellipsize it
        var textLayout = this
        val textPaint = event.getTextPaint(context, config)

        var availableLineCount = availableHeight / textLayout.lineHeight
        val width = (rect.right - rect.left - (config.eventPadding * 2).toFloat()).toInt()

        do {
            // Ellipsize text to fit into event rect
            val availableArea = availableLineCount * availableWidth * 1f
            val ellipsized = ellipsize(text, textPaint, availableArea, TruncateAt.END)
            textLayout = TextLayoutBuilder.build(ellipsized, textPaint, width)
            availableLineCount--
        } while (textLayout.height > availableHeight)

        return textLayout
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

    private fun StaticLayout.scaleToFit(
        eventChip: EventChip<T>,
        text: SpannableStringBuilder,
        availableHeight: Int,
        availableWidth: Int
    ): StaticLayout {
        val event = eventChip.event
        val rect = checkNotNull(eventChip.rect)

        // The text doesn't fit into the chip, so we need to gradually reduce its size until it does
        var textLayout = this
        val textPaint = event.getTextPaint(context, config)

        do {
            textPaint.textSize -= 1f

            val adaptiveLineCount = availableHeight / textLayout.lineHeight
            val availableArea = adaptiveLineCount * availableWidth * 1f
            val ellipsized = ellipsize(text, textPaint, availableArea, TruncateAt.END)

            val width = (rect.right - rect.left - (config.eventPadding * 2).toFloat()).toInt()
            textLayout = TextLayoutBuilder.build(ellipsized, textPaint, width)
        } while (availableHeight < textLayout.height)

        return textLayout
    }

    private fun <T> List<T>.toPair(): Pair<T, T> {
        check(size == 2)
        return first() to last()
    }
}
