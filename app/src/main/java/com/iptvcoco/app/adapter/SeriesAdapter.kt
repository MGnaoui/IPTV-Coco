package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.iptvcoco.app.R
import com.iptvcoco.app.model.Series
import com.iptvcoco.app.util.TvFocusHelper

class SeriesAdapter(
    private val onSeriesClick: (Series) -> Unit,
    private val onFavoriteClick: ((String, Boolean) -> Unit)? = null,
    private val isFavorite: (String) -> Boolean = { false }
) : ListAdapter<Series, SeriesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val poster: ImageView = itemView.findViewById(R.id.ivPoster)
        private val title: TextView = itemView.findViewById(R.id.tvTitle)

        init {
            TvFocusHelper.apply(itemView)
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onSeriesClick(getItem(pos))
                }
            }
        }

        fun bind(series: Series) {
            title.text = series.title
            if (!series.poster.isNullOrBlank()) {
                Glide.with(poster.context)
                    .load(series.poster)
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_tv)
                    .error(R.drawable.ic_tv)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(poster)
            } else {
                poster.setImageResource(R.drawable.ic_tv)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Series>() {
        override fun areItemsTheSame(oldItem: Series, newItem: Series) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Series, newItem: Series) = oldItem == newItem
    }
}
