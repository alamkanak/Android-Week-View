package com.alamkanak.weekview.sample;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.sample.data.model.Event;
import com.alamkanak.weekview.sample.util.ToolbarUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.widget.Toast.LENGTH_SHORT;

public class LegacyActivity extends AppCompatActivity {

    private EventsFetcher eventsFetcher;
    private WeekViewAdapter weekViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        Toolbar toolbar = findViewById(R.id.toolbar);
        WeekView weekView = findViewById(R.id.weekView);
        ToolbarUtils.setupWithWeekView(toolbar, weekView);

        weekViewAdapter = new WeekViewAdapter(this, this::onLoadMore);
        eventsFetcher = new EventsFetcher(this);

        weekView.setAdapter(weekViewAdapter);
    }

    private void onLoadMore(@NotNull Calendar startDate, @NotNull Calendar endDate) {
        eventsFetcher.fetch(startDate, endDate, weekViewAdapter::submit);
    }

    interface OnLoadMoreNotifier {
        void onLoadMore(Calendar startDate, Calendar endDate);
    }

    private static class WeekViewAdapter extends WeekView.PagingAdapter<Event> {

        @NonNull
        private OnLoadMoreNotifier notifier;

        public WeekViewAdapter(@NotNull Context context, @NonNull OnLoadMoreNotifier notifier) {
            super(context);
            this.notifier = notifier;
        }

        @Override
        public void onEventClick(Event data) {
            Toast.makeText(getContext(), "Clicked " + data.getTitle(), LENGTH_SHORT).show();
        }

        @Override
        public void onEmptyViewClick(@NotNull Calendar time) {
            DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
            String formattedTime = sdf.format(time.getTime());
            Toast.makeText(getContext(), "Empty view clicked: " + formattedTime, LENGTH_SHORT).show();
        }

        @Override
        public void onEventLongClick(Event data) {
            Toast.makeText(getContext(), "Long-clicked event: " + data.getTitle(), LENGTH_SHORT).show();
        }

        @Override
        public void onEmptyViewLongClick(@NotNull Calendar time) {
            DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
            String formattedTime = sdf.format(time.getTime());
            Toast.makeText(getContext(), "Empty view long-clicked: " + formattedTime, LENGTH_SHORT).show();
        }

        @Override
        public void onLoadMore(@NotNull Calendar startDate, @NotNull Calendar endDate) {
            notifier.onLoadMore(startDate, endDate);
        }
    }
}
