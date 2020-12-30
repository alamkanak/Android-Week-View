package com.alamkanak.weekview.sample.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.databinding.ListItemSampleBinding

data class Sample(
    val labelResId: Int,
    val details: String,
    val activity: Class<out AppCompatActivity>
)

class SamplesAdapter(
    private val samples: List<Sample>,
    private val onItemClick: (Sample) -> Unit
) : RecyclerView.Adapter<SamplesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_sample, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(samples[position], onItemClick)
    }

    override fun getItemCount() = samples.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ListItemSampleBinding by lazy {
            ListItemSampleBinding.bind(itemView)
        }

        fun bind(sample: Sample, onClick: (Sample) -> Unit) {
            binding.title.text = itemView.context.getString(sample.labelResId)
            binding.details.text = sample.details
            binding.root.setOnClickListener { onClick(sample) }
        }
    }
}
