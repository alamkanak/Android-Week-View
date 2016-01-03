package com.alamkanak.weekview.sample;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.List;

public class AsynchronousActivity extends BaseActivity {

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        return null;
    }
}
