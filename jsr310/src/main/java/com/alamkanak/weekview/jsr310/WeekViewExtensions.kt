package com.alamkanak.weekview.jsr310

import com.alamkanak.weekview.OnEmptyViewClickListener
import com.alamkanak.weekview.OnEmptyViewLongClickListener
import com.alamkanak.weekview.OnLoadMoreListener
import com.alamkanak.weekview.OnMonthChangeListener
import com.alamkanak.weekview.ScrollListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

fun <T : Any> WeekViewEvent.Builder<T>.setStartTime(startTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setStartTime(startTime.toCalendar())
}

fun <T : Any> WeekViewEvent.Builder<T>.setEndTime(endTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setEndTime(endTime.toCalendar())
}

val <T : Any> WeekView<T>.adapter: WeekViewAdapter<T>
    get() = WeekViewAdapter(this)

fun <T : Any> WeekView<T>.setOnMonthChangeListener(
    block: (
        startDate: LocalDate,
        endDate: LocalDate
    ) -> List<WeekViewDisplayable<T>>
) {
    onMonthChangeListener = object : OnMonthChangeListener<T> {
        override fun onMonthChange(
            startDate: Calendar,
            endDate: Calendar
        ): List<WeekViewDisplayable<T>> {
            return block(startDate.toLocalDate(), endDate.toLocalDate())
        }
    }
}

fun <T : Any> WeekView<T>.setOnLoadMoreListener(
    block: (startDate: LocalDate, endDate: LocalDate) -> Unit
) {
    onLoadMoreListener = object : OnLoadMoreListener {
        override fun onLoadMore(
            startDate: Calendar,
            endDate: Calendar
        ) = block(startDate.toLocalDate(), endDate.toLocalDate())
    }
}

fun <T : Any> WeekView<T>.goToDate(date: LocalDate) {
    goToDate(date.toCalendar())
}

fun <T : Any> WeekView<T>.setScrollListener(block: (date: LocalDate) -> Unit) {
    scrollListener = object : ScrollListener {
        override fun onFirstVisibleDateChanged(
            date: Calendar
        ) {
            block(date.toLocalDate())
        }
    }
}

fun <T : Any> WeekView<T>.setOnEmptyViewClickListener(
    block: (time: LocalDateTime) -> Unit
) {
    onEmptyViewClickListener = object : OnEmptyViewClickListener {
        override fun onEmptyViewClicked(time: Calendar) {
            block(time.toLocalDateTime())
        }
    }
}

fun <T : Any> WeekView<T>.setOnEmptyViewLongClickListener(
    block: (time: LocalDateTime) -> Unit
) {
    onEmptyViewLongClickListener = object : OnEmptyViewLongClickListener {
        override fun onEmptyViewLongClick(time: Calendar) {
            block(time.toLocalDateTime())
        }
    }
}
