package com.alamkanak.weekview

import java.util.*

/**
 * Created by Raquib on 1/6/2015.
 */
interface DateTimeInterpreter {

    fun interpretDate(date: Calendar): String
    fun interpretTime(hour: Int): String

}
