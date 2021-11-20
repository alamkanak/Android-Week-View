package com.alamkanak.weekview.threetenabp

import com.alamkanak.weekview.PublicApi
import com.alamkanak.weekview.WeekView
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.Calendar

/**
 * An abstract implementation of [WeekView.Adapter] that allows to submit a list of new elements to
 * [WeekView] and uses [LocalDate] instead of [Calendar].
 *
 * Newly submitted events are processed on a background thread and then presented in [WeekView].
 * Previously submitted events are replaced completely. If you require a paginated approach, you
 * might want to use [WeekView.PagingAdapter].
 *
 * @param T The type of elements that are displayed in the corresponding [WeekView].
 */
@PublicApi
abstract class WeekViewSimpleAdapterThreeTenAbp<T> : WeekView.SimpleAdapter<T>() {

    final override fun onEmptyViewClick(time: Calendar) {
        onEmptyViewClick(time.toLocalDateTime())
    }

    final override fun onEmptyViewLongClick(time: Calendar) {
        onEmptyViewLongClick(time.toLocalDateTime())
    }

    final override fun onDragAndDropFinished(data: T, newStartTime: Calendar, newEndTime: Calendar) {
        onDragAndDropFinished(data, newStartTime.toLocalDateTime(), newEndTime.toLocalDateTime())
    }

    final override fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) {
        onRangeChanged(firstVisibleDate.toLocalDate(), lastVisibleDate.toLocalDate())
    }

    /**
     * Returns the date and time of the location that the user clicked on.
     *
     * @param time A [LocalDateTime] with the date and time
     */
    open fun onEmptyViewClick(time: LocalDateTime) = Unit

    /**
     * Returns the date and time of the location that the user long-clicked on.
     *
     * @param time A [LocalDateTime] with the date and time
     */
    open fun onEmptyViewLongClick(time: LocalDateTime) = Unit

    /**
     * Called when a drag-&-drop gesture has finished to inform the caller of the dragged event's
     * new start and end time.
     *
     * @param data The [T] entity that is associated with the dragged event
     * @param newStartTime The new start time that the event was dragged to
     * @param newEndTime THe new end time that the event was dragged to
     */
    open fun onDragAndDropFinished(data: T, newStartTime: LocalDateTime, newEndTime: LocalDateTime) = Unit

    /**
     * Called whenever the range of dates visible in [WeekView] changes. The list of dates is
     * typically as long as [WeekView.numberOfVisibleDays], though it might contain an additional
     * date if [WeekView] is currently scrolling.
     *
     * @param firstVisibleDate A [LocalDate] representing the first visible date
     * @param lastVisibleDate A [LocalDate] representing the last visible date
     */
    open fun onRangeChanged(firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) = Unit
}

/**
 * An abstract implementation of [WeekView.Adapter] that allows to submit a list of new elements to
 * [WeekView] in a paginated way and uses [LocalDate] instead of [Calendar].
 *
 * This adapter keeps a cache of the submitted elements grouped by month. Whenever the user scrolls
 * to a different month, this adapter will check whether that month's events are present in the
 * cache. If not, it will dispatch a callback to [onLoadMore] with the start and end dates of the
 * months that need to be fetched.
 *
 * Newly submitted events are processed on a background thread and then presented in [WeekView]. To
 * clear the cache and thus refresh all events, you can call [refresh].
 *
 * @param T The type of elements that are displayed in the corresponding [WeekView].
 */
@PublicApi
abstract class WeekViewPagingAdapterThreeTenAbp<T> : WeekView.PagingAdapter<T>() {

    final override fun onEmptyViewClick(time: Calendar) {
        onEmptyViewClick(time.toLocalDateTime())
    }

    final override fun onEmptyViewLongClick(time: Calendar) {
        onEmptyViewLongClick(time.toLocalDateTime())
    }

    final override fun onDragAndDropFinished(data: T, newStartTime: Calendar, newEndTime: Calendar) {
        onDragAndDropFinished(data, newStartTime.toLocalDateTime(), newEndTime.toLocalDateTime())
    }

    final override fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) {
        onRangeChanged(firstVisibleDate.toLocalDate(), lastVisibleDate.toLocalDate())
    }

    final override fun onLoadMore(startDate: Calendar, endDate: Calendar) {
        onLoadMore(startDate.toLocalDate(), endDate.toLocalDate())
    }

    /**
     * Returns the date and time of the location that the user clicked on.
     *
     * @param time A [LocalDateTime] with the date and time
     */
    open fun onEmptyViewClick(time: LocalDateTime) = Unit

    /**
     * Returns the date and time of the location that the user long-clicked on.
     *
     * @param time A [LocalDateTime] with the date and time
     */
    open fun onEmptyViewLongClick(time: LocalDateTime) = Unit

    /**
     * Called when a drag-&-drop gesture has finished to inform the caller of the dragged event's
     * new start and end time.
     *
     * @param data The [T] entity that is associated with the dragged event
     * @param newStartTime The new start time that the event was dragged to
     * @param newEndTime THe new end time that the event was dragged to
     */
    open fun onDragAndDropFinished(data: T, newStartTime: LocalDateTime, newEndTime: LocalDateTime) = Unit

    /**
     * Called whenever the range of dates visible in [WeekView] changes. The list of dates is
     * typically as long as [WeekView.numberOfVisibleDays], though it might contain an additional
     * date if [WeekView] is currently scrolling.
     *
     * @param firstVisibleDate A [LocalDate] representing the first visible date
     * @param lastVisibleDate A [LocalDate] representing the last visible date
     */
    open fun onRangeChanged(firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) = Unit

    /**
     * Called whenever [WeekView] needs to fetch new elements of a given month in order to allow for
     * a smooth scrolling experience.
     *
     * This adapter caches submitted elements of the current month as well as its previous and next
     * month. If [WeekView] scrolls to a new month, that month as well as its surrounding months
     * need to potentially be fetched.
     *
     * @param startDate A [LocalDate] of the first date of the month that needs to be fetched
     * @param endDate A [LocalDate] of the last date of the month that needs to be fetched
     */
    open fun onLoadMore(startDate: LocalDate, endDate: LocalDate) = Unit
}
