package com.alamkanak.weekview.sample;

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
import com.alamkanak.weekview.sample.apiclient.Event;
import com.alamkanak.weekview.sample.database.EventsDatabase;
import com.alamkanak.weekview.sample.database.FakeEventsDatabase;
import com.alamkanak.weekview.ui.WeekView;
import com.alamkanak.weekview.utils.DateTimeInterpreter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * This is a base activity which contains week view and all the codes necessary to initialize the
 * week view.
 * Created by Raquib-ul-Alam Kanak on 1/3/2014.
 * Website: http://alamkanak.github.io
 */
public class BaseActivity extends AppCompatActivity
        implements EventClickListener<Event>, MonthLoader.MonthChangeListener<Event>,
        EventLongPressListener<Event>, EmptyViewLongPressListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;

    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView<Event> mWeekView;

    private EventsDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mDatabase = new FakeEventsDatabase(this);

        mWeekView = findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setEmptyViewLongPressListener(this);

        setupDateTimeInterpreter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
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
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
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
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     */
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
    public List<WeekViewDisplayable<Event>> onMonthChange(Calendar startDate, Calendar endDate) {
        return mDatabase.getEventsInRange(startDate, endDate);
    }

    @Override
    public void onEventClick(Event event, RectF eventRect) {
        Toast.makeText(this, "Clicked " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(Event event, RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show();
    }

}
