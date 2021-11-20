package com.alamkanak.weekview

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

internal sealed class ColorResource {
    data class Value(@ColorInt val color: Int) : ColorResource()
    data class Id(@ColorRes val resId: Int) : ColorResource()

    fun resolve(context: Context): Int = when (this) {
        is Id -> ContextCompat.getColor(context, resId)
        is Value -> color
    }
}

internal sealed class TextResource {
    data class Value(val text: CharSequence) : TextResource()
    data class Id(@StringRes val resId: Int) : TextResource()

    fun resolve(context: Context, semibold: Boolean): CharSequence = when (this) {
        is Id -> {
            val text = context.getString(resId)
            if (semibold) text.semibold() else SpannableString(text)
        }
        is Value -> when (text) {
            // We don't change the existing style of SpannableStrings.
            is SpannableString -> text
            is SpannableStringBuilder -> text.build()
            else -> if (semibold) text.semibold() else SpannableString(text)
        }
    }
}

internal sealed class DimenResource {
    data class Value(val value: Int) : DimenResource()
    data class Id(@DimenRes val resId: Int) : DimenResource()

    fun resolve(context: Context): Int = when (this) {
        is Id -> context.resources.getDimensionPixelSize(resId)
        is Value -> value
    }
}
