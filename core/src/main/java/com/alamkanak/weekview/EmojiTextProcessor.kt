package com.alamkanak.weekview

import androidx.emoji.text.EmojiCompat

internal class EmojiTextProcessor {

    private val emojiCompat: EmojiCompat?
        get() = try {
            EmojiCompat.get()
        } catch (e: IllegalStateException) {
            // EmojiCompat is not set up in this project
            null
        }

    fun process(text: CharSequence): CharSequence = emojiCompat?.process(text) ?: text
}
