package com.alamkanak.weekview.base

typealias TextProcessor = (CharSequence) -> CharSequence

object TextProcessors {
    private val textProcessors = mutableSetOf<TextProcessor>()

    fun register(textProcessor: TextProcessor) {
        textProcessors += textProcessor
    }

    fun process(text: CharSequence): CharSequence {
        var result = text
        textProcessors.forEach { result = it(result) }
        return result
    }
}
