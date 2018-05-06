package com.alamkanak.weekview.sample.apiclient

import android.annotation.SuppressLint
import android.graphics.Color
import com.alamkanak.weekview.WeekViewEvent
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * An event model that was built for automatic serialization from json to object.
 * Created by Raquib-ul-Alam Kanak on 1/3/16.
 * Website: http://alamkanak.github.io
 */
class Event {

    @Expose
    @SerializedName("name")
    var name: String? = null
    @Expose
    @SerializedName("dayOfMonth")
    var dayOfMonth: Int = 0
    @Expose
    @SerializedName("startTime")
    var startTime: String? = null
    @Expose
    @SerializedName("endTime")
    var endTime: String? = null
    @Expose
    @SerializedName("color")
    var color: String? = null

    @SuppressLint("SimpleDateFormat")
    fun toWeekViewEvent(): WeekViewEvent {

        // Parse time.
        val sdf = SimpleDateFormat("HH:mm")
        var start = Date()
        var end = Date()
        try {
            start = sdf.parse(startTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        try {
            end = sdf.parse(endTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        // Initialize start and end time.
        val now = Calendar.getInstance()
        val startTime = now.clone() as Calendar
        startTime.timeInMillis = start.time
        startTime.set(Calendar.YEAR, now.get(Calendar.YEAR))
        startTime.set(Calendar.MONTH, now.get(Calendar.MONTH))
        startTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val endTime = startTime.clone() as Calendar
        endTime.timeInMillis = end.time
        endTime.set(Calendar.YEAR, startTime.get(Calendar.YEAR))
        endTime.set(Calendar.MONTH, startTime.get(Calendar.MONTH))
        endTime.set(Calendar.DAY_OF_MONTH, startTime.get(Calendar.DAY_OF_MONTH))

        // Create an week view event.
        val weekViewEvent = WeekViewEvent(0L,name,null,startTime,endTime)
//        weekViewEvent.name = name
//        weekViewEvent.setStartTime(startTime)
//        weekViewEvent.setEndTime(endTime)
        weekViewEvent.color = Color.parseColor(color)

        return weekViewEvent
    }
}
