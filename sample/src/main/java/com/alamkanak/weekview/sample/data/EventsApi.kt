package com.alamkanak.weekview.sample.data

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import com.alamkanak.weekview.sample.data.model.ApiEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Thread.sleep

class EventsApi(
    private val context: Context
) {

    private val responseType = object : TypeToken<List<ApiEvent>>() {}.type

    fun fetchEvents(
        onSuccess: (List<ApiEvent>) -> Unit
    ) {
        AsyncTask.execute {
            sleep(2_000)
            val inputStream = context.assets.open("events.json")
            val json = inputStream.reader().readText()

            val activity = context as Activity
            activity.runOnUiThread {
                onSuccess(Gson().fromJson(json, responseType))
            }
        }
    }
}
