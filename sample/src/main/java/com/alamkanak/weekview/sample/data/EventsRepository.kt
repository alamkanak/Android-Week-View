package com.alamkanak.weekview.sample.data

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.alamkanak.weekview.sample.data.model.ApiBlockedTime
import com.alamkanak.weekview.sample.data.model.ApiEvent
import com.alamkanak.weekview.sample.data.model.ApiResult
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.YearMonth

class EventsRepository(private val context: Context) {

    private val eventResponseType = object : TypeToken<List<ApiEvent>>() {}.type
    private val blockedTimeResponseType = object : TypeToken<List<ApiBlockedTime>>() {}.type

    private val gson = Gson()

    fun fetch(
        yearMonths: List<YearMonth>,
        onSuccess: (List<CalendarEntity>) -> Unit
    ) {
        val handlerThread = HandlerThread("events-fetching")
        handlerThread.start()

        val backgroundHandler = Handler(handlerThread.looper)
        val mainHandler = Handler(Looper.getMainLooper())

        backgroundHandler.post {
            val apiEntities = fetchEvents() + fetchBlockedTimes()

            val calendarEntities = yearMonths.flatMap { yearMonth ->
                apiEntities.mapNotNull { it.toCalendarEntity(yearMonth) }
            }

            mainHandler.post {
                onSuccess(calendarEntities)
            }
        }
    }

    private fun fetchEvents(): List<ApiResult> {
        val inputStream = context.assets.open("events.json")
        val json = inputStream.reader().readText()
        return gson.fromJson(json, eventResponseType)
    }

    private fun fetchBlockedTimes(): List<ApiResult> {
        val inputStream = context.assets.open("blocked_times.json")
        val json = inputStream.reader().readText()
        return gson.fromJson(json, blockedTimeResponseType)
    }
}
