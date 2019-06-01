package com.alamkanak.weekview

import android.text.StaticLayout

internal val StaticLayout.lineHeight: Int
    get() = height / lineCount
