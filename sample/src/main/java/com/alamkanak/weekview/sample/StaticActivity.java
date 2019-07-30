package com.alamkanak.weekview.sample;

import android.graphics.RectF;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alamkanak.weekview.OnEmptyViewLongClickListener;
import com.alamkanak.weekview.OnEventClickListener;
import com.alamkanak.weekview.OnEventLongClickListener;
import com.alamkanak.weekview.OnMonthChangeListener;
import com.alamkanak.weekview.OnRangeChangeListener;
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
import java.util.Locale;

import static androidx.core.util.Preconditions.checkNotNull;

public class StaticActivity extends AppCompatActivity
        implements OnEventClickListener<Event>, OnMonthChangeListener<Event>,
        OnEventLongClickListener<Event>, OnEmptyViewLongClickListener {

    WeekView<Event> mWeekView;
    private EventsDatabase mDatabase;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private TextView mDateTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static);

        mDatabase = new FakeEventsDatabase(this);
        mDateTV = findViewById(R.id.dates);

        mWeekView = findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setOnMonthChangeListener(this);
        mWeekView.setOnEventLongClickListener(this);
        mWeekView.setOnEmptyViewLongClickListener(this);

        ImageView left = findViewById(R.id.left_arrow);
        left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = checkNotNull(mWeekView.getFirstVisibleDate());
                cal.add(Calendar.DATE, -7);
                mWeekView.goToDate(cal);
            }
        });

        ImageView right = findViewById(R.id.right_arrow);
        right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = checkNotNull(mWeekView.getFirstVisibleDate());
                cal.add(Calendar.DATE, 7);
                mWeekView.goToDate(cal);
            }
        });

        mWeekView.setOnRangeChangeListener(new OnRangeChangeListener() {
            @Override
            public void onRangeChanged(@NotNull Calendar firstVisibleDate,
                                       @NotNull Calendar lastVisibleDate) {
                updateDateText(firstVisibleDate, lastVisibleDate);
            }
        });
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
    public void onEventLongClick(Event event, @NotNull RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongClick(@NotNull Calendar time) {
        DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
        Toast.makeText(this, "Empty view long pressed: "
                + sdf.format(time.getTime()), Toast.LENGTH_SHORT).show();
    }

    void updateDateText(Calendar firstVisibleDate, Calendar lastVisibleDate) {
        String formattedFirstDay = dateFormatter.format(firstVisibleDate.getTime());
        String formattedLastDay = dateFormatter.format(lastVisibleDate.getTime());
        mDateTV.setText(getString(R.string.date_infos, formattedFirstDay, formattedLastDay));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
