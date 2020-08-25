package com.alamkanak.weekview

import android.os.Build
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.TypefaceSpan
import androidx.emoji.text.EmojiCompat

private val emojiCompat: EmojiCompat?
    get() = try { EmojiCompat.get() } catch (e: IllegalStateException) { null }

internal val CharSequence.emojified: CharSequence
    get() = emojiCompat?.process(this) ?: this

internal fun CharSequence.toTextLayout(
    textPaint: TextPaint,
    width: Int,
    alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    spacingMultiplier: Float = 1f,
    spacingExtra: Float = 0f,
    includePad: Boolean = false
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    StaticLayout.Builder
        .obtain(this, 0, length, textPaint, width)
        .setAlignment(alignment)
        .setLineSpacing(spacingExtra, spacingMultiplier)
        .setIncludePad(includePad)
        .build()
} else {
    @Suppress("DEPRECATION")
    StaticLayout(this, textPaint, width, alignment, spacingMultiplier, spacingExtra, includePad)
}

internal val StaticLayout.maxLineLength: Float
    get() = (0 until lineCount).map { getLineWidth(it) }.maxOrNull() ?: 0f

internal fun SpannableStringBuilder.build(): SpannableString = SpannableString.valueOf(this)

internal fun CharSequence.semibold() = SpannableString(this).apply {
    setSpan(TypefaceSpan("sans-serif-medium"), 0, length, 0)
}

internal fun ViewState.getTextPaint(event: ResolvedWeekViewEvent<*>): TextPaint {
    val textPaint = TextPaint(if (event.isAllDay) allDayEventTextPaint else eventTextPaint)
    if (event.style.textColor != null) {
        textPaint.color = event.style.textColor
    }
    return textPaint
}
