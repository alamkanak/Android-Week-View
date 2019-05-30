package com.alamkanak.weekview

internal object Preconditions {

    fun <T> checkNotNull(value: T?, message: String? = null): T {
        if (value == null) {
            throw NullPointerException(message)
        }
        return value
    }

}
