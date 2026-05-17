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
import com.iptvcoco.app.model.Channel

class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit,
    private val onFavoriteClick: ((String, Boolean) -> Unit)? = null,
    private val isFavorite: (String) -> Boolean = { false },
    private val layoutRes: Int = R.layout.item_channel
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    private var items = listOf<Channel>()

    fun submitList(list: List<Channel>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val logo: ImageView = itemView.findViewById(R.id.ivChannelLogo)
        private val name: TextView = itemView.findViewById(R.id.tvChannelName)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.ivFavorite)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onChannelClick(items[pos])
                }
            }
            favoriteIcon.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val channel = items[pos]
                    val fav = !isFavorite(channel.id)
                    onFavoriteClick?.invoke(channel.id, fav)
                    updateFavoriteIcon(fav)
                }
            }
        }

        fun bind(channel: Channel) {
            name.text = channel.name
            if (!channel.logo.isNullOrBlank()) {
                Glide.with(logo.context)
                    .load(channel.logo)
                    .placeholder(R.drawable.ic_live)
                    .error(R.drawable.ic_live)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(logo)
            } else {
                logo.setImageResource(R.drawable.ic_live)
            }
            updateFavoriteIcon(isFavorite(channel.id))
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
