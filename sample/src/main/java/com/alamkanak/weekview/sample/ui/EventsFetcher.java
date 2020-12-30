package com.alamkanak.weekview.sample.ui;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.alamkanak.weekview.sample.data.EventsDatabase;
import com.alamkanak.weekview.sample.data.model.CalendarEntity;
import com.alamkanak.weekview.sample.util.ExtensionsKt;

import org.threeten.bp.LocalDate;

import java.util.Calendar;
import java.util.List;

class EventsFetcher {

    protected final EventsDatabase database;

    public EventsFetcher(Context context) {
        database = new EventsDatabase(context);
    }

    void fetch(LocalDate startDate, LocalDate endDate, Listener listener) {
        fetch(ExtensionsKt.toCalendar(startDate), ExtensionsKt.toCalendar(endDate), listener);
    }

    void fetch(Calendar startDate, Calendar endDate, Listener listener) {
        HandlerThread thread = new HandlerThread("events-fetcher");
        thread.start();

        Looper looper = thread.getLooper();
        Handler handler = new Handler(looper);

        handler.post(() -> {
            List<CalendarEntity.Event> events = database.getEventsInRange(startDate, endDate);
            listener.onEventsFetched(events);
        });
    }

    interface Listener {
        void onEventsFetched(List<CalendarEntity.Event> events);
    }
}
