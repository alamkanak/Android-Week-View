package com.alamkanak.weekview.emoji

import androidx.emoji.text.EmojiCompat
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.base.TextProcessor
import com.alamkanak.weekview.base.TextProcessors

private val emojiCompat: EmojiCompat? = try {
    EmojiCompat.get()
} catch (e: IllegalStateException) { null }

/**
 * Enables emoji processing for entity titles and subtitles in WeekView.
 */
@Suppress("unused")
fun WeekView.enableEmojiProcessing() {
    val emojiProcessor: TextProcessor = { emojiCompat?.process(it) ?: it }
    TextProcessors.register(emojiProcessor)
}
