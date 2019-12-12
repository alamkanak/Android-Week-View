package com.alamkanak.weekview

import android.graphics.RectF
import java.util.Calendar

@FunctionalInterface
interface OnEventClickListener<T> {

    /**
     * Called when an [EventChip] is clicked.
     *
     * @param data The [T] object associated with the [EventChip]'s [WeekViewEvent]
     * @param eventRect The [RectF] of the [EventChip]
     */
    fun onEventClick(data: T, eventRect: RectF)
}

@FunctionalInterface
interface OnEventLongClickListener<T> {

    /**
     * Called when an [EventChip] is long-clicked.
     *
     * @param data The [T] object associated with the [EventChip]'s [WeekViewEvent]
     * @param eventRect The [RectF] of the [EventChip]
     */
    fun onEventLongClick(data: T, eventRect: RectF)
}

@FunctionalInterface
interface OnEmptyViewClickListener {

    /**
     * Called when an empty area of [WeekView] is clicked.
     *
     * @param time A [Calendar] with the date and time of the clicked position
     */
    fun onEmptyViewClicked(time: Calendar)
}

@FunctionalInterface
interface OnEmptyViewLongClickListener {

    /**
     * Called when an empty area of [WeekView] is long-clicked.
     *
     * @param time A [Calendar] with the date and time of the clicked position
     */
    fun onEmptyViewLongClick(time: Calendar)
}

@FunctionalInterface
interface OnLoadMoreListener {

    /**
     * Called when the month displayed in [WeekView] changes.
     * @param startDate A [Calendar] representing the start date of the month
     * @param endDate A [Calendar] representing the end date of the month
     */
    fun onLoadMore(startDate: Calendar, endDate: Calendar)
}

@FunctionalInterface
interface OnRangeChangeListener {

    /**
     * Called when the range of visible days changes due to scrolling.
     *
     * @param firstVisibleDate The first visible day
     * @param lastVisibleDate The last visible day
     */
    fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar)
}

@FunctionalInterface
interface OnMonthChangeListener<T> {

    /**
     * Called when the month displayed in [WeekView] changes.
     * @param startDate A [Calendar] representing the start date of the month
     * @param endDate A [Calendar] representing the end date of the month
     * @return The list of [WeekViewDisplayable] of the provided month
     */
    fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<T>>
}

@FunctionalInterface
interface ScrollListener {

    /**
     * Called when the first visible date has changed.
     *
     * @param date The new first visible date
     */
    fun onFirstVisibleDateChanged(date: Calendar)
}
