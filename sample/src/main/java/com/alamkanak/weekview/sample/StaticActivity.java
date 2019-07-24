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

import com.alamkanak.weekview.OnEmptyViewLongPressListener;
import com.alamkanak.weekview.OnEventClickListener;
import com.alamkanak.weekview.OnEventLongPressListener;
import com.alamkanak.weekview.OnMonthChangeListener;
import com.alamkanak.weekview.ScrollListener;
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
        OnEventLongPressListener<Event>, OnEmptyViewLongPressListener {

    WeekView<Event> mWeekView;
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
        mWeekView.setOnMonthChangeListener(this);
        mWeekView.setOnEventLongPressListener(this);
        mWeekView.setOnEmptyViewLongPressListener(this);

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

        mWeekView.setScrollListener(new ScrollListener() {
            @Override
            public void onFirstVisibleDateChanged(@Nullable Calendar date) {
                updateDateText();
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
    public void onEventLongPress(Event event, @NotNull RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongPress(@NotNull Calendar time) {
        DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
        Toast.makeText(this, "Empty view long pressed: "
                + sdf.format(time.getTime()), Toast.LENGTH_SHORT).show();
    }

    void updateDateText() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String formattedFirstDay = "none";
        if (mWeekView.getFirstVisibleDate() != null) {
            formattedFirstDay = format.format(mWeekView.getFirstVisibleDate().getTime());
        }

        String formattedLastDay = "none";
        if (mWeekView.getLastVisibleDate() != null) {
            formattedLastDay = format.format(mWeekView.getLastVisibleDate().getTime());
        }

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
