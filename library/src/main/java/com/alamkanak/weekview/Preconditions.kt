package com.alamkanak.weekview

internal object Preconditions {

    @JvmStatic
    fun <T> checkNotNull(value: T?): T {
        if (value == null) {
            throw NullPointerException()
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
