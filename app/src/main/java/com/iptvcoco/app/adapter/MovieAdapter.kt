package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.iptvcoco.app.R
import com.iptvcoco.app.model.Movie

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val onFavoriteClick: ((String, Boolean) -> Unit)? = null,
    private val isFavorite: (String) -> Boolean = { false }
) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    private var items = listOf<Movie>()

    fun submitList(list: List<Movie>) {
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
                    onMovieClick(items[pos])
                }
            }
            favoriteIcon.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val movie = items[pos]
                    val fav = !isFavorite(movie.id)
                    onFavoriteClick?.invoke(movie.id, fav)
                    updateFavoriteIcon(fav)
                }
            }
        }

        fun bind(movie: Movie) {
            title.text = movie.title
            if (!movie.poster.isNullOrBlank()) {
                Glide.with(poster.context)
                    .load(movie.poster)
                    .placeholder(R.drawable.ic_movie)
                    .error(R.drawable.ic_movie)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(poster)
            } else {
                poster.setImageResource(R.drawable.ic_movie)
            }
            updateFavoriteIcon(isFavorite(movie.id))
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
