package com.alamkanak.weekview.sample.apiclient;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by alam on 1/3/16.
 */
public interface MyJsonService {

    @GET("/3isor")
    void listEvents(Callback<List<Event>> eventsCallback);

}
