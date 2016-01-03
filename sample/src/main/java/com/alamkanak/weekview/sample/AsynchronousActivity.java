package com.alamkanak.weekview.sample;

import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.sample.apiclient.Event;
import com.alamkanak.weekview.sample.apiclient.MyJsonService;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AsynchronousActivity extends BaseActivity implements Callback<List<Event>> {

    private List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
    boolean calledNetwork = false;

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        if (!calledNetwork) {
            RestAdapter retrofit = new RestAdapter.Builder()
                    .setEndpoint("https://api.myjson.com/bins")
                    .build();
            MyJsonService service = retrofit.create(MyJsonService.class);
            service.listEvents(this);
        }
        else {

        }
        return null;
    }

    @Override
    public void success(List<Event> events, Response response) {
        getWeekView().notifyDatasetChanged();
    }

    @Override
    public void failure(RetrofitError error) {
        Toast.makeText(this, R.string.async_error, Toast.LENGTH_SHORT).show();
    }
}
