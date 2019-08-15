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
        title: String,
        location: String?,
        chipHeight: Int,
        chipWidth: Int
    ): StaticLayout {
        val text = createText(title, location, isMultiLine = true)
        val textPaint = eventChip.event.getTextPaint(context, config)
        val textLayout = TextLayoutBuilder.build(text, textPaint, chipWidth)

        val fitsIntoChip = chipHeight >= textLayout.height
        if (fitsIntoChip) {
            return ellipsize(eventChip, textLayout, text, chipHeight, chipWidth)
        }

        val modifiedText = createText(title, location, isMultiLine = false)
        val modifiedTextLayout = TextLayoutBuilder.build(text, textPaint, chipWidth)

        val fitsIntoChipNow = chipHeight >= modifiedTextLayout.height
        val isAdaptive = config.adaptiveEventTextSize

        // TODO: Refactor adaptiveTextSize and ellipsize behavior

        return when {
            fitsIntoChipNow || !isAdaptive -> {
                ellipsize(eventChip, modifiedTextLayout, modifiedText, chipHeight, chipWidth)
            }
            isAdaptive -> scaleToFit(eventChip, modifiedText, chipHeight)
            else -> modifiedTextLayout
        }
    }

    private fun createText(
        title: String,
        location: String?,
        isMultiLine: Boolean
    ): SpannableStringBuilder {
        val text = SpannableStringBuilder(title)
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
        location?.let {
            if (isMultiLine) {
                text.appendln()
            } else {
                text.append(" ")
            }
            text.append(it)
        }
        return text
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
}
