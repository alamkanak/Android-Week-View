package com.alamkanak.weekview.sample.ui.async

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.sample.data.EventsApi
import com.alamkanak.weekview.sample.databinding.ActivityBasicBinding
import com.alamkanak.weekview.sample.util.isVisible
import com.alamkanak.weekview.sample.util.setupWithWeekView

class AsyncActivity : AppCompatActivity() {

    private val viewModel: AsyncViewModel by lazy {
        AsyncViewModel(EventsApi(this))
    }

    private val binding: ActivityBasicBinding by lazy {
        ActivityBasicBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbarContainer.toolbar.setupWithWeekView(binding.weekView)

        val adapter = AsyncActivityWeekViewAdapter(eventClickHandler = viewModel::remove)
        binding.weekView.adapter = adapter

        viewModel.viewState.observe(this) { viewState ->
            binding.blockingProgressIndicator.isVisible = viewState.isLoading
            adapter.submitList(viewState.events)
        }
    }
}
