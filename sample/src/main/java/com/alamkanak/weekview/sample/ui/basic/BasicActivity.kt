package com.alamkanak.weekview.sample.ui.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.emoji.enableEmojiProcessing
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.databinding.ActivityBasicBinding
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showWithRetryAction
import com.alamkanak.weekview.threetenabp.setDateFormatter
import com.google.android.material.snackbar.Snackbar
import java.util.Locale
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class BasicActivity : AppCompatActivity() {

    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())

    private val binding: ActivityBasicBinding by lazy {
        ActivityBasicBinding.inflate(layoutInflater)
    }

    private val viewModel: BasicViewModel by lazy {
        BasicViewModel(database = EventsDatabase(this))
    }

    private val snackbar: Snackbar by lazy {
        Snackbar.make(binding.weekView, "Something went wrong", Snackbar.LENGTH_INDEFINITE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.weekView.enableEmojiProcessing()
        binding.toolbarContainer.toolbar.setupWithWeekView(binding.weekView)

        val adapter = BasicActivityWeekViewAdapter(
            loadMoreHandler = { params -> viewModel.fetchEvents(params) }
        )
        binding.weekView.adapter = adapter

        binding.weekView.setDateFormatter { date: LocalDate ->
            val weekdayLabel = weekdayFormatter.format(date)
            val dateLabel = dateFormatter.format(date)
            weekdayLabel + "\n" + dateLabel
        }

        viewModel.viewState.observe(this) { viewState ->
            if (viewState.error != null) {
                val params = viewState.error.loadParams
                snackbar.showWithRetryAction { viewModel.retry(params) }
            } else {
                snackbar.dismiss()
            }

            adapter.submitList(viewState.entities)
        }
    }
}
