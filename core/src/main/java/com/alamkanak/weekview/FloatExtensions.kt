package com.alamkanak.weekview

import kotlin.math.max
import kotlin.math.min

internal fun Float.limit(
    minValue: Float,
    maxValue: Float
): Float = min(max(this, minValue), maxValue)
