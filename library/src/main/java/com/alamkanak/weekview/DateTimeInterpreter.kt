package com.alamkanak.weekview

import android.content.Context
import java.util.*

/**
 * Created by Raquib on 1/6/2015.
 */
interface DateTimeInterpreter {
    fun interpretDate(date: Calendar): String
    fun interpretTime(hour: Int): String
}

class DefaultDateTimeInterpreter(
        context: Context
) : DateTimeInterpreter {

    private val sdfDate = DateUtils.dateFormat
    private val sdfTime = DateUtils.getTimeFormat(context)
    private val calendar = Calendar.getInstance()

    override fun interpretDate(date: Calendar): String {
        return sdfDate.format(date.time).toUpperCase()
    }

    override fun interpretTime(hour: Int): String {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 0)
        return sdfTime.format(calendar.time)
    }

}
