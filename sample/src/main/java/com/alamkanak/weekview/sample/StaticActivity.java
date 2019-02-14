package com.alamkanak.weekview.sample;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alamkanak.weekview.EmptyViewLongPressListener;
import com.alamkanak.weekview.EventClickListener;
import com.alamkanak.weekview.EventLongPressListener;
import com.alamkanak.weekview.MonthChangeListener;
import com.alamkanak.weekview.ScrollListener;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.sample.apiclient.Event;
import com.alamkanak.weekview.sample.database.EventsDatabase;
import com.alamkanak.weekview.sample.database.FakeEventsDatabase;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StaticActivity extends AppCompatActivity
        implements EventClickListener<Event>, MonthChangeListener<Event>,
        EventLongPressListener<Event>, EmptyViewLongPressListener {

    private WeekView<Event> mWeekView;
    private EventsDatabase mDatabase;

    private TextView mDateTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static);

        mDatabase = new FakeEventsDatabase(this);
        mDateTV = findViewById(R.id.dates);

        mWeekView = findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setEmptyViewLongPressListener(this);

        ImageView left = findViewById(R.id.left_arrow);
        left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = mWeekView.getFirstVisibleDay();
                cal.add(Calendar.DAY_OF_MONTH, -7);
                mWeekView.goToDate(cal);
                updateDateText();
            }
        });

        ImageView right = findViewById(R.id.right_arrow);
        right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = mWeekView.getFirstVisibleDay();
                cal.add(Calendar.DAY_OF_MONTH, 7);
                mWeekView.goToDate(cal);
                updateDateText();
            }
        });

        mWeekView.setScrollListener(new ScrollListener() {
            @Override
            public void onFirstVisibleDayChanged(@NonNull Calendar newFirstVisibleDay,
                                                 @Nullable Calendar oldFirstVisibleDay) {
                updateDateText();
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

    @NotNull
    @Override
    public List<WeekViewDisplayable<Event>> onMonthChange(@NotNull Calendar startDate,
                                                          @NotNull Calendar endDate) {
        return mDatabase.getEventsInRange(startDate, endDate);
    }

    @Override
    public void onEventClick(Event event, @NotNull RectF eventRect) {
        Toast.makeText(this, "Clicked " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(Event event, @NotNull RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongPress(@NotNull Calendar time) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show();
    }

    private void updateDateText() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedFirstDay = format.format(mWeekView.getFirstVisibleDay().getTime());
        String formattedLastDay = format.format(mWeekView.getLastVisibleDay().getTime());
        mDateTV.setText(getString(R.string.date_infos, formattedFirstDay, formattedLastDay));
    }
}
