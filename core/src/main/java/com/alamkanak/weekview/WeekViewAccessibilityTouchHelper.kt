package com.alamkanak.weekview

import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.customview.widget.ExploreByTouchHelper
import java.text.DateFormat.LONG
import java.text.DateFormat.SHORT
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.roundToInt

internal class WeekViewAccessibilityTouchHelper(
    private val view: WeekView,
    private val viewState: ViewState,
    private val gestureHandler: WeekViewGestureHandler,
    private val touchHandler: WeekViewTouchHandler,
    private val eventChipsCache: EventChipsCache
) : ExploreByTouchHelper(view) {

    private val dateFormatter = SimpleDateFormat.getDateInstance(LONG)
    private val dateTimeFormatter = SimpleDateFormat.getDateTimeInstance(LONG, SHORT)

    private val store = VirtualViewIdStore()

    override fun getVirtualViewAt(x: Float, y: Float): Int {
        // First, we check if an event chip was hit
        val eventChip = gestureHandler.findHitEvent(x, y)
        val eventChipVirtualViewId = eventChip?.let { store[it] }
        if (eventChipVirtualViewId != null) {
            return eventChipVirtualViewId
        }

        // If no event chip was hit, we still want to inform the user what date they
        // just interacted with
        val date = touchHandler.calculateTimeFromPoint(x, y)?.atStartOfDay
        val dateVirtualViewId = date?.let { store[it] }
        if (dateVirtualViewId != null) {
            return dateVirtualViewId
        }

        return HOST_ID
    }

    override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
        val dateRange = viewState.dateRange
        val visibleEventChips = eventChipsCache.allEventChipsInDateRange(dateRange)
        virtualViewIds += store.put(visibleEventChips)
        virtualViewIds += dateRange.map { store.put(it) }
    }

    override fun onPerformActionForVirtualView(
        virtualViewId: Int,
        action: Int,
        arguments: Bundle?
    ): Boolean {
        val eventChip = store.findEventChip(virtualViewId)
        val date = store.findDate(virtualViewId)

        return when {
            eventChip != null -> onPerformActionForEventChip(virtualViewId, eventChip, action)
            date != null -> onPerformActionForDate(virtualViewId, date, action)
            else -> false
        }
    }

    private fun onPerformActionForEventChip(
        virtualViewId: Int,
        eventChip: EventChip,
        action: Int
    ): Boolean = when (action) {
        AccessibilityNodeInfoCompat.ACTION_CLICK -> {
            touchHandler.adapter?.onEventClick(id = eventChip.eventId)
            sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED)
            true
        }
        AccessibilityNodeInfoCompat.ACTION_LONG_CLICK -> {
            touchHandler.adapter?.onEventLongClick(id = eventChip.eventId)
            sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED)
            true
        }
        else -> false
    }

    private fun onPerformActionForDate(
        virtualViewId: Int,
        date: Calendar,
        action: Int
    ): Boolean = when (action) {
        AccessibilityNodeInfoCompat.ACTION_CLICK -> {
            touchHandler.adapter?.onEmptyViewClick(date)
            sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED)
            true
        }
        AccessibilityNodeInfoCompat.ACTION_LONG_CLICK -> {
            touchHandler.adapter?.onEmptyViewLongClick(date)
            sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED)
            true
        }
        else -> false
    }

    override fun onPopulateNodeForVirtualView(
        virtualViewId: Int,
        node: AccessibilityNodeInfoCompat
    ) {
        val eventChip = store.findEventChip(virtualViewId)
        if (eventChip != null) {
            populateNodeWithEventInfo(eventChip, node)
            return
        }

        val date = store.findDate(virtualViewId)
        if (date != null) {
            populateNodeWithDateInfo(date, node)
            return
        }

        throw IllegalStateException("No view found for virtualViewId $virtualViewId")
    }

    private fun populateNodeWithEventInfo(
        eventChip: EventChip,
        node: AccessibilityNodeInfoCompat
    ) {
        node.contentDescription = createDescriptionForVirtualView(eventChip.originalEvent)
        node.addAction(AccessibilityActionCompat.ACTION_CLICK)
        node.addAction(AccessibilityActionCompat.ACTION_LONG_CLICK)

        val bounds = Rect()
        eventChip.bounds?.round(bounds)
        node.setBoundsInParent(bounds)
    }

    private fun populateNodeWithDateInfo(
        date: Calendar,
        node: AccessibilityNodeInfoCompat
    ) {
        node.contentDescription = createDescriptionForVirtualView(date)

        node.addAction(AccessibilityActionCompat.ACTION_CLICK)
        node.addAction(AccessibilityActionCompat.ACTION_LONG_CLICK)

        val dateWithStartPixel = viewState.dateRangeWithStartPixels
            .firstOrNull { it.first == date } ?: return

        val left = dateWithStartPixel.second.roundToInt()
        val right = left + viewState.totalDayWidth.roundToInt()
        val top = viewState.headerHeight.roundToInt()
        val bottom = view.height

        val bounds = Rect(left, top, right, bottom)
        node.setBoundsInParent(bounds)
    }

    private fun createDescriptionForVirtualView(event: ResolvedWeekViewEvent<*>): String {
        val date = dateTimeFormatter.format(event.startTime.time)
        return "$date: ${event.title}, ${event.location}"
    }

    private fun createDescriptionForVirtualView(date: Calendar): String {
        return dateFormatter.format(date.time)
    }
}

private class VirtualViewIdStore {

    private val eventChips = mutableListOf<EventChip>()
    private val dates = mutableListOf<Calendar>()

    private val eventChipVirtualViewIds = mutableListOf<Int>()
    private val dateVirtualViewIds = mutableListOf<Int>()

    private var maximumId = 0

    operator fun get(eventChip: EventChip): Int? {
        val index = eventChips.indexOf(eventChip)
        return eventChipVirtualViewIds[index]
    }

    operator fun get(date: Calendar): Int? {
        val index = dates.indexOf(date)
        return dateVirtualViewIds[index]
    }

    fun findEventChip(
        virtualViewId: Int
    ): EventChip? {
        val index = eventChipVirtualViewIds.indexOfFirst { it == virtualViewId }
        return if (index != -1) eventChips[index] else null
    }

    fun findDate(
        virtualViewId: Int
    ): Calendar? {
        val index = dateVirtualViewIds.indexOfFirst { it == virtualViewId }
        return if (index != -1) dates[index] else null
    }

    fun put(date: Calendar): Int {
        val startOfDay = date.atStartOfDay
        val index = dates.indexOf(startOfDay)

        return if (index != -1) {
            dateVirtualViewIds[index]
        } else {
            dates += date
            dateVirtualViewIds += maximumId
            maximumId++
        }
    }

    fun put(newEventChips: List<EventChip>): List<Int> {
        val virtualViewIds = mutableListOf<Int>()

        for (eventChip in newEventChips) {
            val index = eventChips.indexOf(eventChip)
            if (index != -1) {
                // Update the list with the new EventChip – its bounds might have changed
                eventChips.removeAt(index)
                eventChips.add(index, eventChip)
                virtualViewIds += eventChipVirtualViewIds[index]
            } else {
                // Insert the previously unknown EventChip
                eventChips += eventChip
                eventChipVirtualViewIds += maximumId
                virtualViewIds += maximumId
                maximumId++
            }
        }

        return virtualViewIds
    }
}
