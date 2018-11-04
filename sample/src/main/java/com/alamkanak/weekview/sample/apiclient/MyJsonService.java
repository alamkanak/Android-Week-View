package com.alamkanak.weekview.sample.apiclient;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by Raquib-ul-Alam Kanak on 1/3/16.
 * Website: http://alamkanak.github.io
 */
public interface MyJsonService {

    @GET("/1kpjf")
    void listEvents(Callback<List<ApiEvent>> eventsCallback);

}
