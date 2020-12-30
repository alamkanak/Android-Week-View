package com.alamkanak.weekview.sample.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.TypefaceSpan;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;
import com.alamkanak.weekview.sample.R;
import com.alamkanak.weekview.sample.data.model.CalendarEntity;
import com.alamkanak.weekview.sample.databinding.ActivityBasicBinding;
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

        ActivityBasicBinding binding = ActivityBasicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ToolbarUtils.setupWithWeekView(binding.toolbarContainer.toolbar, binding.weekView);

        weekViewAdapter = new WeekViewAdapter(this::onLoadMore);
        eventsFetcher = new EventsFetcher(this);

        binding.weekView.setAdapter(weekViewAdapter);
    }

    private void onLoadMore(@NotNull Calendar startDate, @NotNull Calendar endDate) {
        eventsFetcher.fetch(startDate, endDate, weekViewAdapter::submitList);
    }

    interface OnLoadMoreNotifier {
        void onLoadMore(Calendar startDate, Calendar endDate);
    }

    private static class WeekViewAdapter extends WeekView.PagingAdapter<CalendarEntity.Event> {

        @NonNull
        private final OnLoadMoreNotifier notifier;

        public WeekViewAdapter(@NonNull OnLoadMoreNotifier notifier) {
            super();
            this.notifier = notifier;
        }

        @NotNull
        @Override
        public WeekViewEntity onCreateEntity(CalendarEntity.Event item) {
            int backgroundColor = item.isCanceled() ? Color.WHITE : item.getColor();
            int textColor = item.isCanceled() ? item.getColor() : Color.WHITE;

            int borderWidthResId = item.isCanceled() ? R.dimen.border_width : R.dimen.no_border_width;
            int borderWidth = getContext().getResources().getDimensionPixelSize(borderWidthResId);

            WeekViewEntity.Style style = new WeekViewEntity.Style.Builder()
                    .setTextColor(textColor)
                    .setBackgroundColor(backgroundColor)
                    .setBorderWidth(borderWidth)
                    .setBorderColor(item.getColor())
                    .build();

            SpannableStringBuilder title = new SpannableStringBuilder(item.getTitle());
            TypefaceSpan titleSpan = new TypefaceSpan("sans-serif-medium");
            title.setSpan(titleSpan, 0, item.getTitle().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (item.isCanceled()) {
                StrikethroughSpan strikeSpan = new StrikethroughSpan();
                title.setSpan(strikeSpan, 0, item.getTitle().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            SpannableStringBuilder subtitle = new SpannableStringBuilder(item.getLocation());
            if (item.isCanceled()) {
                StrikethroughSpan strikeSpan = new StrikethroughSpan();
                subtitle.setSpan(strikeSpan, 0, item.getLocation().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return new WeekViewEntity.Event.Builder<>(this)
                    .setId(item.getId())
                    .setTitle(title)
                    .setStartTime(item.getStartTime())
                    .setEndTime(item.getEndTime())
                    .setSubtitle(item.getLocation())
                    .setAllDay(item.isAllDay())
                    .setStyle(style)
                    .build();
        }

        @Override
        public void onEventClick(CalendarEntity.Event data) {
            Toast.makeText(getContext(), "Clicked " + data.getTitle(), LENGTH_SHORT).show();
        }

        @Override
        public void onEmptyViewClick(@NotNull Calendar time) {
            DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
            String formattedTime = sdf.format(time.getTime());
            Toast.makeText(getContext(), "Empty view clicked: " + formattedTime, LENGTH_SHORT).show();
        }

        @Override
        public void onEventLongClick(CalendarEntity.Event data) {
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
