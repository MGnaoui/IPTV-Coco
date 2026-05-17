package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iptvcoco.app.R
import com.iptvcoco.app.model.EPGEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EpgAdapter : RecyclerView.Adapter<EpgAdapter.ViewHolder>() {

    private var items = listOf<EPGEntry>()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun submitList(list: List<EPGEntry>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_epg, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvEpgTime)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvEpgTitle)

        fun bind(entry: EPGEntry) {
            val start = timeFormat.format(Date(entry.startTime))
            val end = timeFormat.format(Date(entry.endTime))
            tvTime.text = "$start - $end"
            tvTitle.text = entry.title
        }
    }
}
