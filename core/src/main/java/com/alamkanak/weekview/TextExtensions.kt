package com.alamkanak.weekview

import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.style.StyleSpan

internal val StaticLayout.lineHeight: Int
    get() = height / lineCount

internal fun SpannableStringBuilder.setSpan(
    styleSpan: StyleSpan
) = setSpan(styleSpan, 0, length, 0)
