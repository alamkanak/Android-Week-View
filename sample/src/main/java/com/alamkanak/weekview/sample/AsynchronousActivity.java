package com.alamkanak.weekview.sample;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.sample.apiclient.Event;
import com.alamkanak.weekview.sample.apiclient.MyJsonService;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An example of how events can be fetched from network and be displayed on the week view.
 * Created by Raquib-ul-Alam Kanak on 1/3/2014.
 * Website: http://alamkanak.github.io
 */
public class AsynchronousActivity extends BaseActivity implements Callback<List<Event>> {

  private List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
  //boolean calledNetwork = false;
  /**
   * ATTENTION: This was auto-generated to implement the App Indexing API.
   * See https://g.co/AppIndexing/AndroidStudio for more information.
   */
  private GoogleApiClient client;

  @Override
  public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
    this.events.clear();
    for (WeekViewEvent event : events) {
      this.events.add(event);
    }
    getWeekView().notifyDatasetChanged();

  }

  @Override
  public void onFailure(Call<List<Event>> call, Throwable t) {
    t.printStackTrace();
    Toast.makeText(this, R.string.async_error, Toast.LENGTH_SHORT).show();
  }

  @Override
  public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {

    // Download events from network if it hasn't been done already. To understand how events are
    // downloaded using retrofit, visit http://square.github.io/retrofit
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.myjson.com/bins")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    MyJsonService service = retrofit.create(MyJsonService.class);

    Call<List<Event>> call = service.listEvents();
    call.enqueue(this);
    // Return only the events that matches newYear and newMonth.
    List<WeekViewEvent> matchedEvents = new ArrayList<WeekViewEvent>();
    for (WeekViewEvent event : events) {
      if (eventMatches(event, newYear, newMonth)) {
        matchedEvents.add(event);
      }
    }
    return matchedEvents;
  }

  /**
   * Checks if an event falls into a specific year and month.
   *
   * @param event The event to check for.
   * @param year  The year.
   * @param month The month.
   * @return True if the event matches the year and month.
   */
  private boolean eventMatches(WeekViewEvent event, int year, int month) {
    return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month - 1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

  }

  @Override
  public void onStart() {
    super.onStart();

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    client.connect();
    Action viewAction = Action.newAction(
        Action.TYPE_VIEW, // TODO: choose an action type.
        "Asynchronous Page", // TODO: Define a title for the content shown.
        // TODO: If you have web page content that matches this app activity's content,
        // make sure this auto-generated web page URL is correct.
        // Otherwise, set the URL to null.
        Uri.parse("http://host/path"),
        // TODO: Make sure this auto-generated app URL is correct.
        Uri.parse("android-app://com.alamkanak.weekview.sample/http/host/path")
    );
    AppIndex.AppIndexApi.start(client, viewAction);
  }

  @Override
  public void onStop() {
    super.onStop();

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    Action viewAction = Action.newAction(
        Action.TYPE_VIEW, // TODO: choose an action type.
        "Asynchronous Page", // TODO: Define a title for the content shown.
        // TODO: If you have web page content that matches this app activity's content,
        // make sure this auto-generated web page URL is correct.
        // Otherwise, set the URL to null.
        Uri.parse("http://host/path"),
        // TODO: Make sure this auto-generated app URL is correct.
        Uri.parse("android-app://com.alamkanak.weekview.sample/http/host/path")
    );
    AppIndex.AppIndexApi.end(client, viewAction);
    client.disconnect();
  }
}
