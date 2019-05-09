package com.alamkanak.weekview

import android.graphics.RectF
import java.util.*

fun <T> WeekView<T>.setMonthChangeListener(
        block: (startDate: Calendar, endDate: Calendar) -> List<WeekViewDisplayable<T>>
) {
    setMonthChangeListener(object : MonthChangeListener<T> {
        override fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<T>> {
            return block(startDate, endDate)
        }
    })
}

fun <T> WeekView<T>.setOnEventClickListener(
        block: (data: T, rect: RectF) -> Unit
) {
    setOnEventClickListener(object : EventClickListener<T> {
        override fun onEventClick(data: T, eventRect: RectF) {
            block(data, eventRect)
        }
    })
}

fun <T> WeekView<T>.setEventLongPressListener(
        block: (data: T, rect: RectF) -> Unit
) {
    setEventLongPressListener(object : EventLongPressListener<T> {
        override fun onEventLongPress(data: T, eventRect: RectF) {
            block(data, eventRect)
        }
    })
}

fun <T> WeekView<T>.setEmptyViewClickListener(
        block: (time: Calendar) -> Unit
) {
    emptyViewClickListener = object : EmptyViewClickListener {
        override fun onEmptyViewClicked(time: Calendar) {
            block(time)
        }
    }
}

fun <T> WeekView<T>.setEmptyViewLongPressListener(
        block: (time: Calendar) -> Unit
) {
    emptyViewLongPressListener = object : EmptyViewLongPressListener {
        override fun onEmptyViewLongPress(time: Calendar) {
            block(time)
        }
    }
}

fun <T> WeekView<T>.setScrollListener(
        block: (newFirstVisibleDay: Calendar, oldFirstVisibleDay: Calendar?) -> Unit
) {
    scrollListener = object : ScrollListener {
        override fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar, oldFirstVisibleDay: Calendar?) {
            block(firstVisibleDay, oldFirstVisibleDay)
        }
    }
}
