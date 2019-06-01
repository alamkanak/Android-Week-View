package com.alamkanak.weekview.sample.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EqualSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildViewHolder(view).adapterPosition
        val itemCount = state.itemCount

        outRect.left = spacing
        outRect.right = spacing
        outRect.top = spacing
        outRect.bottom = if (position == itemCount - 1) spacing else 0
    }

}
