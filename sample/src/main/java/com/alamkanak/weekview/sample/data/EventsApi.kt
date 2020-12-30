package com.alamkanak.weekview.sample.data

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.alamkanak.weekview.sample.data.model.ApiEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Thread.sleep

class EventsApi(private val context: Context) {

    private val responseType = object : TypeToken<List<ApiEvent>>() {}.type

    fun fetchEvents(onSuccess: (List<ApiEvent>) -> Unit) {
        val handlerThread = HandlerThread("events-fetching")
        handlerThread.start()

        val backgroundHandler = Handler(handlerThread.looper)
        val mainHandler = Handler(Looper.getMainLooper())

        backgroundHandler.post {
            sleep(2_000)
            val inputStream = context.assets.open("events.json")
            val json = inputStream.reader().readText()

            mainHandler.post {
                onSuccess(Gson().fromJson(json, responseType))
            }
        }
    }
}
