package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.iptvcoco.app.R
import com.iptvcoco.app.model.Series

class SeriesAdapter(
    private val onSeriesClick: (Series) -> Unit,
    private val onFavoriteClick: ((String, Boolean) -> Unit)? = null,
    private val isFavorite: (String) -> Boolean = { false }
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
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.ivFavorite)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onSeriesClick(items[pos])
                }
            }
            favoriteIcon.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val series = items[pos]
                    val fav = !isFavorite(series.id)
                    onFavoriteClick?.invoke(series.id, fav)
                    updateFavoriteIcon(fav)
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
            updateFavoriteIcon(isFavorite(series.id))
        }

        private fun updateFavoriteIcon(isFav: Boolean) {
            favoriteIcon.setImageResource(
                if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
            )
            favoriteIcon.setColorFilter(
                if (isFav) itemView.context.getColor(R.color.favorite_active)
                else itemView.context.getColor(R.color.favorite_inactive)
            )
        }
    }
}
