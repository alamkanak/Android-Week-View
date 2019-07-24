package com.alamkanak.weekview.sample;

import android.graphics.RectF;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alamkanak.weekview.OnEmptyViewLongPressListener;
import com.alamkanak.weekview.OnEventClickListener;
import com.alamkanak.weekview.OnEventLongPressListener;
import com.alamkanak.weekview.OnMonthChangeListener;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.sample.apiclient.Event;
import com.alamkanak.weekview.sample.data.EventsDatabase;
import com.alamkanak.weekview.sample.data.FakeEventsDatabase;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static java.util.Calendar.DAY_OF_MONTH;

/**
 * This is a base activity which contains week view and all the codes necessary to initialize the
 * week view.
 * Added Min and Max Date
 * Created by cs8898 on 2/11/2019.
 */
public class LimitedActivity extends AppCompatActivity
        implements OnEventClickListener<Event>, OnMonthChangeListener<Event>,
        OnEventLongPressListener<Event>, OnEmptyViewLongPressListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;

    private int mWeekViewType = TYPE_THREE_DAY_VIEW;

    private WeekView<Event> mWeekView;
    private EventsDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limited);

        mDatabase = new FakeEventsDatabase(this);

        mWeekView = findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setOnMonthChangeListener(this);
        mWeekView.setOnEventLongPressListener(this);
        mWeekView.setOnEmptyViewLongPressListener(this);

        Calendar now = Calendar.getInstance();

        Calendar min = (Calendar) now.clone();
        min.set(DAY_OF_MONTH, 1);
        mWeekView.setMinDate(min);

        Calendar max = (Calendar) now.clone();
        max.set(DAY_OF_MONTH, max.getActualMaximum(DAY_OF_MONTH));
        mWeekView.setMaxDate(max);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
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
    }

    private void openThreeDayView(MenuItem item) {
        if (mWeekViewType == TYPE_THREE_DAY_VIEW) {
            return;
        }

        item.setChecked(!item.isChecked());
        mWeekViewType = TYPE_THREE_DAY_VIEW;
        mWeekView.setNumberOfVisibleDays(3);
    }

    private void openWeekView(MenuItem item) {
        if (mWeekViewType == TYPE_WEEK_VIEW) {
            return;
        }

        item.setChecked(!item.isChecked());
        mWeekViewType = TYPE_WEEK_VIEW;
        mWeekView.setNumberOfVisibleDays(7);
    }

    @NotNull
    @Override
    public List<WeekViewDisplayable<Event>> onMonthChange(@NonNull Calendar startDate,
                                                          @NonNull Calendar endDate) {
        return mDatabase.getEventsInRange(startDate, endDate);
    }

    @Override
    public void onEventClick(@NonNull Event event, @NonNull RectF eventRect) {
        Toast.makeText(this, "Clicked " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(@NonNull Event event, @NonNull RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongPress(@NonNull Calendar time) {
        DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
        Toast.makeText(this, "Empty view long pressed: "
                + sdf.format(time.getTime()), Toast.LENGTH_SHORT).show();
    }

}
