package com.alamkanak.weekview.sample;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.sample.data.EventsDatabase;
import com.alamkanak.weekview.sample.data.model.Event;

import java.util.Calendar;
import java.util.List;

class EventsFetcher {

    protected EventsDatabase database;

    public EventsFetcher(Context context) {
        database = new EventsDatabase(context);
    }

    void fetch(Calendar startDate, Calendar endDate, Listener listener) {
        HandlerThread thread = new HandlerThread("events-fetcher");
        thread.start();

        Looper looper = thread.getLooper();
        Handler handler = new Handler(looper);

        handler.post(() -> {
            List<WeekViewDisplayable<Event>> events = database.getEventsInRange(startDate, endDate);
            listener.onEventsFetched(events);
        });
    }

    interface Listener {
        void onEventsFetched(List<WeekViewDisplayable<Event>> events);
    }
}
