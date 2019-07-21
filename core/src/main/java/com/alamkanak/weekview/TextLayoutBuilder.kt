package com.alamkanak.weekview

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

@Suppress("DEPRECATION")
internal object TextLayoutBuilder {

    internal fun build(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        spacingMultiplier: Float = 1f,
        spacingExtra: Float = 0f,
        includePadding: Boolean = false
    ) = if (SDK_INT >= M) {
        StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, width)
            .setAlignment(alignment)
            .setLineSpacing(spacingExtra, spacingMultiplier)
            .setIncludePad(includePadding)
            .build()
    } else {
        StaticLayout(text, textPaint, width, alignment, spacingMultiplier, spacingExtra, includePadding)
    }
}
