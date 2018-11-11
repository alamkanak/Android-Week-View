package com.alamkanak.weekview.sample;

import android.app.ProgressDialog;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alamkanak.weekview.data.MonthLoader;
import com.alamkanak.weekview.listeners.EmptyViewLongPressListener;
import com.alamkanak.weekview.listeners.EventClickListener;
import com.alamkanak.weekview.listeners.EventLongPressListener;
import com.alamkanak.weekview.model.WeekViewDisplayable;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.sample.apiclient.ApiEvent;
import com.alamkanak.weekview.sample.apiclient.MyJsonService;
import com.alamkanak.weekview.ui.WeekView;
import com.alamkanak.weekview.utils.DateTimeInterpreter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AsyncActivity extends AppCompatActivity
        implements EventClickListener<ApiEvent>, MonthLoader.MonthChangeListener<ApiEvent>,
        EventLongPressListener<ApiEvent>, EmptyViewLongPressListener, Callback<List<ApiEvent>> {

    private List<WeekViewEvent<ApiEvent>> events = new ArrayList<>();
    boolean calledNetwork = false;

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;

    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView<ApiEvent> mWeekView;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setMessage("Loading events ...");
        mProgressDialog.show();

        mWeekView = findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setEmptyViewLongPressListener(this);

        setupDateTimeInterpreter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setupDateTimeInterpreter();
        switch (id) {
            case R.id.action_today:
                mWeekView.goToToday();
                return true;
            case R.id.action_day_view:
                openDayView(item);
                return true;
            case R.id.action_three_day_view:
                openThreeDayView(item);
                return true;
            case R.id.action_week_view:
                openWeekView(item);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openDayView(MenuItem item) {
        if (mWeekViewType == TYPE_DAY_VIEW) {
            return;
        }
        item.setChecked(!item.isChecked());
        mWeekViewType = TYPE_DAY_VIEW;
        mWeekView.setNumberOfVisibleDays(1);

        // Lets change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTimeColumnTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    private void openThreeDayView(MenuItem item) {
        if (mWeekViewType == TYPE_THREE_DAY_VIEW) {
            return;
        }

        item.setChecked(!item.isChecked());
        mWeekViewType = TYPE_THREE_DAY_VIEW;
        mWeekView.setNumberOfVisibleDays(3);

        // Lets change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTimeColumnTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    private void openWeekView(MenuItem item) {
        if (mWeekViewType == TYPE_WEEK_VIEW) {
            return;
        }

        item.setChecked(!item.isChecked());
        mWeekViewType = TYPE_WEEK_VIEW;
        mWeekView.setNumberOfVisibleDays(7);

        // Lets change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mWeekView.setTimeColumnTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
    }

    private void setupDateTimeInterpreter() {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {

            SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

            @Override
            public String interpretShortDate(Calendar date) {
                String weekday = weekdayNameFormat.format(date.getTime());
                weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretDate(Calendar date) {
                String weekday = weekdayNameFormat.format(date.getTime());
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    protected String getEventTitle(Calendar time) {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);
        int month = time.get(Calendar.MONTH) + 1;
        int dayOfMonth = time.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.getDefault(), "Event of %02d:%02d %s/%d", hour, minute, month, dayOfMonth);
    }

    @Override
    public List<WeekViewDisplayable<ApiEvent>> onMonthChange(Calendar startDate, Calendar endDate) {
        final int newYear = startDate.get(Calendar.YEAR);
        final int newMonth = startDate.get(Calendar.MONTH);

        // Download events from network if it hasn't been done already. To understand how events are
        // downloaded using retrofit, visit http://square.github.io/retrofit
        if (!calledNetwork) {
            RestAdapter retrofit = new RestAdapter.Builder()
                    .setEndpoint("https://api.myjson.com/bins")
                    .build();

            MyJsonService service = retrofit.create(MyJsonService.class);
            service.listEvents(this);
            calledNetwork = true;
        }

        // Return only the events that matches newYear and newMonth.
        List<WeekViewDisplayable<ApiEvent>> matchedEvents = new ArrayList<>();
        for (WeekViewEvent<ApiEvent> event : events) {
            if (eventMatches(event, newYear, newMonth)) {
                matchedEvents.add(event);
            }
        }
        return matchedEvents;
    }

    /**
     * Checks if an event falls into a specific year and month.
     * @param event The event to check for.
     * @param year The year.
     * @param month The month.
     * @return True if the event matches the year and month.
     */
    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month - 1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }

    @Override
    public void success(List<ApiEvent> events, Response response) {
        this.events.clear();
        for (ApiEvent event : events) {
            this.events.add(event.toWeekViewEvent());
        }

        mProgressDialog.dismiss();
        mWeekView.notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError error) {
        error.printStackTrace();
        mProgressDialog.dismiss();
        Toast.makeText(this, R.string.async_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventClick(ApiEvent event, RectF eventRect) {
        Toast.makeText(this, "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(ApiEvent event, RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show();
    }

}
