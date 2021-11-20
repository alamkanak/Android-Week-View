package com.alamkanak.weekview.sample.util

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM
import java.time.format.FormatStyle.SHORT

val defaultDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT)
