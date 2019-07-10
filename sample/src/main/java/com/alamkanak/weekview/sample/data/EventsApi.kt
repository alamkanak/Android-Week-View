package com.alamkanak.weekview.sample.data

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import com.alamkanak.weekview.sample.apiclient.ApiEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Thread.sleep

interface EventsApi {
    fun fetchEvents(onSuccess: (List<ApiEvent>) -> Unit)
}

class FakeEventsApi(
    private val context: Context
) : EventsApi {

    private val responseType = object : TypeToken<List<ApiEvent>>() {}.type
    private val gson = Gson()

    override fun fetchEvents(
        onSuccess: (List<ApiEvent>) -> Unit
    ) {
        AsyncTask.execute {
            sleep(2_000)
            val inputStream = context.assets.open("events.json")
            val json = inputStream.reader().readText()

            val activity = context as Activity
            activity.runOnUiThread {
                onSuccess(gson.fromJson(json, responseType))
            }
        }
    }

}
