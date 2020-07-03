package com.alamkanak.weekview.sample;

import android.graphics.RectF;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alamkanak.weekview.OnEmptyViewLongClickListener;
import com.alamkanak.weekview.OnEventClickListener;
import com.alamkanak.weekview.OnEventLongClickListener;
import com.alamkanak.weekview.OnLoadMoreListener;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.sample.data.model.Event;
import com.alamkanak.weekview.sample.util.ToolbarUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.widget.Toast.LENGTH_SHORT;

public class LegacyActivity extends AppCompatActivity
        implements OnEventClickListener<Event>, OnLoadMoreListener,
        OnEventLongClickListener<Event>, OnEmptyViewLongClickListener {

    private EventsFetcher eventsFetcher;
    protected WeekView<Event> weekView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        Toolbar toolbar = findViewById(R.id.toolbar);
        weekView = findViewById(R.id.weekView);
        ToolbarUtils.setupWithWeekView(toolbar, weekView);

        eventsFetcher = new EventsFetcher(this);

        weekView.setOnEventClickListener(this);
        // weekView.setOnMonthChangeListener(this);
        weekView.setOnLoadMoreListener(this);
        weekView.setOnEventLongClickListener(this);
        weekView.setOnEmptyViewLongClickListener(this);
    }

    @Override
    public void onLoadMore(@NotNull Calendar startDate, @NotNull Calendar endDate) {
        eventsFetcher.fetch(startDate, endDate, weekView::submit);
    }

    @Override
    public void onEventClick(@NonNull Event event, @NonNull RectF eventRect) {
        Toast.makeText(this, "Clicked " + event.getTitle(), LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongClick(@NonNull Event event, @NonNull RectF eventRect) {
        Toast.makeText(this, "Long-clicked event: " + event.getTitle(), LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongClick(@NonNull Calendar time) {
        DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
        String formattedTime = sdf.format(time.getTime());
        Toast.makeText(this, "Empty view long pressed: " + formattedTime, LENGTH_SHORT).show();
    }
}
