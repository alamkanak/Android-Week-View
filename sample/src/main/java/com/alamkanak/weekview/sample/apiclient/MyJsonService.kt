package com.alamkanak.weekview.sample.apiclient

import retrofit.Callback
import retrofit.http.GET

/**
 * Created by Raquib-ul-Alam Kanak on 1/3/16.
 * Website: http://alamkanak.github.io
 */
interface MyJsonService {

    @GET("/1kpjf")
    fun listEvents(eventsCallback: Callback<List<Event>>)

}
