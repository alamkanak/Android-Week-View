package com.alamkanak.weekview.sample.ui

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.databinding.FragmentWeekBinding
import com.alamkanak.weekview.sample.util.setupWithWeekView
import java.util.Calendar

class WithFragmentActivity : AppCompatActivity(R.layout.activity_with_fragment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, WeekFragment.newInstance())
                .commit()
        }
    }
}

class WeekFragment : Fragment(R.layout.fragment_week) {

    private lateinit var binding: FragmentWeekBinding

    private val database: EventsDatabase by lazy { EventsDatabase(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbarContainer.toolbar.setupWithWeekView(binding.weekView)

        val start = getStartDate()
        val end = getEndDate()

        val adapter = FragmentWeekViewAdapter()
        binding.weekView.adapter = adapter

        // Limit WeekView to the current month
        binding.weekView.minDate = start
        binding.weekView.maxDate = end

        val events = database.getEventsInRange(start, end)
        adapter.submitList(events)
    }

    private fun getStartDate(): Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
    }

    private fun getEndDate(): Calendar = Calendar.getInstance().apply {
        val daysInMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
        set(Calendar.DAY_OF_MONTH, daysInMonth)
        set(Calendar.HOUR_OF_DAY, 23)
    }

    companion object {
        fun newInstance() = WeekFragment()
    }
}

private class FragmentWeekViewAdapter : WeekView.SimpleAdapter<CalendarEntity.Event>() {

    override fun onCreateEntity(item: CalendarEntity.Event): WeekViewEntity {
        val backgroundColor = if (!item.isCanceled) item.color else Color.WHITE
        val textColor = if (!item.isCanceled) Color.WHITE else item.color
        val borderWidthResId = if (!item.isCanceled) R.dimen.no_border_width else R.dimen.border_width

        val style = WeekViewEntity.Style.Builder()
            .setTextColor(textColor)
            .setBackgroundColor(backgroundColor)
            .setBorderWidthResource(borderWidthResId)
            .setBorderColor(item.color)
            .build()

        val title = SpannableStringBuilder(item.title).apply {
            val titleSpan = TypefaceSpan("sans-serif-medium")
            setSpan(titleSpan, 0, item.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (item.isCanceled) {
                setSpan(StrikethroughSpan(), 0, item.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val subtitle = SpannableStringBuilder(item.location).apply {
            if (item.isCanceled) {
                setSpan(StrikethroughSpan(), 0, item.location.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return WeekViewEntity.Event.Builder(item)
            .setId(item.id)
            .setTitle(title)
            .setStartTime(item.startTime)
            .setEndTime(item.endTime)
            .setSubtitle(subtitle)
            .setAllDay(item.isAllDay)
            .setStyle(style)
            .build()
    }
}
