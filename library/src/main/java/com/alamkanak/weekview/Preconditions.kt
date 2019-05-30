package com.alamkanak.weekview

internal object Preconditions {

    @JvmOverloads
    @JvmStatic
    fun <T> checkNotNull(value: T?, message: String? = null): T {
        if (value == null) {
            throw NullPointerException(message)
        }
        return value
    }

    @JvmStatic
    @JvmOverloads
    fun checkState(value: Boolean, message: String? = null) {
        if (!value) {
            throw IllegalStateException(message)
        }
    }

}
