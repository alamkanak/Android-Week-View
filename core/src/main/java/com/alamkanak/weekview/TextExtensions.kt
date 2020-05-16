package com.alamkanak.weekview

import android.os.Build.VERSION.SDK_INT
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan

internal val StaticLayout.lineHeight: Int
    get() = height / lineCount

internal fun SpannableStringBuilder.setSpan(
    styleSpan: StyleSpan
) = setSpan(styleSpan, 0, length, 0)

internal fun CharSequence.toTextLayout(
    textPaint: TextPaint,
    width: Int,
    alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    spacingMultiplier: Float = 1f,
    spacingExtra: Float = 0f,
    includePad: Boolean = false
) = if (SDK_INT >= 23) {
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
