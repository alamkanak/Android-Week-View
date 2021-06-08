package com.alamkanak.weekview.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.jsr310.maxDateAsLocalDate
import com.alamkanak.weekview.jsr310.minDateAsLocalDate
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.data.model.toWeekViewEntity
import com.alamkanak.weekview.sample.databinding.FragmentWeekBinding
import com.alamkanak.weekview.sample.util.genericViewModel
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.yearMonthsBetween
import java.time.LocalDate
import java.time.YearMonth

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
    private val viewModel by genericViewModel()

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

        val adapter = FragmentWeekViewAdapter(loadMoreHandler = viewModel::fetchEvents)
        binding.weekView.adapter = adapter

        // Limit WeekView to the current month
        binding.weekView.minDateAsLocalDate = YearMonth.now().atDay(1)
        binding.weekView.maxDateAsLocalDate = YearMonth.now().atEndOfMonth()

        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            adapter.submitList(viewState.entities)
        }
    }

    companion object {
        fun newInstance() = WeekFragment()
    }
}

private class FragmentWeekViewAdapter(
    private val loadMoreHandler: (List<YearMonth>) -> Unit
) : WeekViewPagingAdapterJsr310<CalendarEntity>() {

    override fun onCreateEntity(item: CalendarEntity): WeekViewEntity = item.toWeekViewEntity()

    override fun onLoadMore(
        startDate: LocalDate,
        endDate: LocalDate
    ) = loadMoreHandler(yearMonthsBetween(startDate, endDate))
}
