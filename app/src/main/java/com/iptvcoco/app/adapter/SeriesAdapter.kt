package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iptvcoco.app.R
import com.iptvcoco.app.model.Series

class SeriesAdapter(
    private val onSeriesClick: (Series) -> Unit
) : RecyclerView.Adapter<SeriesAdapter.ViewHolder>() {

    private var items = listOf<Series>()

    fun submitList(list: List<Series>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val poster: ImageView = itemView.findViewById(R.id.ivPoster)
        private val title: TextView = itemView.findViewById(R.id.tvTitle)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onSeriesClick(items[pos])
                }
            }
        }

        fun bind(series: Series) {
            title.text = series.title
            if (!series.poster.isNullOrBlank()) {
                Glide.with(poster.context)
                    .load(series.poster)
                    .placeholder(R.drawable.ic_tv)
                    .into(poster)
            } else {
                poster.setImageResource(R.drawable.ic_tv)
            }
        }
    }
}
