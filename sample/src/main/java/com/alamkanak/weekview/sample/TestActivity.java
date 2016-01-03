package com.alamkanak.weekview.sample;

import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by alam on 1/3/16.
 */
public class TestActivity extends BaseActivity implements WeekView.EmptyViewClickListener {

    private List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
    private long count = 1;
    private String[] colors = new String[]{
            "#59dbe0",
            "#f57f68",
            "#87d288",
            "#f8b552"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWeekView().setEmptyViewClickListener(this);
        getWeekView().setEmptyViewLongPressListener(this);
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {

        List<WeekViewEvent> matchedEvents = new ArrayList<WeekViewEvent>();
        for (WeekViewEvent event : events) {
            if (eventMatches(event, newYear, newMonth)) {
                matchedEvents.add(event);
            }
        }

        return matchedEvents;
    }

    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month - 1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        WeekViewEvent newEvent = new WeekViewEvent(count, "Event " + count, time.get(Calendar.YEAR), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH), time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.YEAR), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH), time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
        Calendar endTime = newEvent.getEndTime();

        int random = (int )(Math.random() * 3 + 1);

        endTime.add(Calendar.HOUR, random == 1 ? 2 : 1);
        newEvent.setEndTime(endTime);
        newEvent.setColor(Color.parseColor(colors[((int) (count % 4))]));
        this.events.add(newEvent);
        count++;
        getWeekView().notifyDatasetChanged();
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        onEmptyViewClicked(event.getStartTime());
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        String json = "[";
        for (WeekViewEvent event : events) {
            json += String.format(
                    "{" +
                        "\"name\": \"%s\"," +
                        "\"dayOfMonth\": %d," +
                        "\"startTime\": \"%02d:%02d\"," +
                        "\"endTime\": \"%02d:%02d\"," +
                        "\"color\": \"%s\"" +
                    "},",
                    event.getName(),
                    event.getStartTime().get(Calendar.DAY_OF_MONTH),
                    event.getStartTime().get(Calendar.HOUR_OF_DAY), event.getStartTime().get(Calendar.MINUTE),
                    event.getEndTime().get(Calendar.HOUR_OF_DAY), event.getEndTime().get(Calendar.MINUTE),
                    String.format("#%06X", (0xFFFFFF & event.getColor()))
            );
        }
        json += "]";
        Log.d("TEST", json);
        super.onEmptyViewLongPress(time);
    }
}
