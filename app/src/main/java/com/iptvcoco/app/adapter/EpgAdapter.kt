package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iptvcoco.app.R
import com.iptvcoco.app.model.EPGEntry
import com.iptvcoco.app.util.TvFocusHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EpgAdapter : ListAdapter<EPGEntry, EpgAdapter.ViewHolder>(DiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_epg, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvEpgTime)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvEpgTitle)

        init {
            TvFocusHelper.apply(itemView, scale = 1.02f)
        }

        fun bind(entry: EPGEntry) {
            val start = timeFormat.format(Date(entry.startTime))
            val end = timeFormat.format(Date(entry.endTime))
            tvTime.text = "$start - $end"
            tvTitle.text = entry.title
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<EPGEntry>() {
        override fun areItemsTheSame(oldItem: EPGEntry, newItem: EPGEntry) =
            oldItem.startTime == newItem.startTime && oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: EPGEntry, newItem: EPGEntry) = oldItem == newItem
    }
}
