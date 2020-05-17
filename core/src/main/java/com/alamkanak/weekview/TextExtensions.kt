package com.alamkanak.weekview

import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.StyleSpan
import androidx.emoji.text.EmojiCompat

internal val StaticLayout.lineHeight: Int
    get() = height / lineCount

internal fun SpannableStringBuilder.setSpan(
    styleSpan: StyleSpan
) = setSpan(styleSpan, 0, length, 0)

private val emojiCompat: EmojiCompat?
    get() = try { EmojiCompat.get() } catch (e: IllegalStateException) { null }

fun CharSequence.emojify(): CharSequence = emojiCompat?.process(this) ?: this

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
    get() = (0 until lineCount).map { getLineWidth(it) }.max() ?: 0f

internal fun SpannableStringBuilder.build(): SpannableString = SpannableString.valueOf(this)

internal fun SpannableString.setSpan(
    styleSpan: StyleSpan
) = setSpan(styleSpan, 0, length, 0)

internal fun CharSequence.bold() = SpannableString(this).apply {
    setSpan(StyleSpan(Typeface.BOLD))
}

internal fun CharSequence.ellipsized(
    textPaint: TextPaint,
    availableArea: Int,
    truncateAt: TextUtils.TruncateAt = TextUtils.TruncateAt.END
): CharSequence = TextUtils.ellipsize(this, textPaint, availableArea.toFloat(), truncateAt)

internal fun WeekViewConfigWrapper.getTextPaint(event: ResolvedWeekViewEvent<*>): TextPaint {
    val textPaint = if (event.isAllDay) allDayEventTextPaint else eventTextPaint
    if (event.style.textColor != null) {
        textPaint.color = event.style.textColor
    }
    return textPaint
}
