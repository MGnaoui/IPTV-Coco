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
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.util.TvFocusHelper

class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit,
    private val onFavoriteClick: ((String, Boolean) -> Unit)? = null,
    private val isFavorite: (String) -> Boolean = { false },
    private val layoutRes: Int = R.layout.item_channel
) : ListAdapter<Channel, ChannelAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val logo: ImageView = itemView.findViewById(R.id.ivChannelLogo)
        private val name: TextView = itemView.findViewById(R.id.tvChannelName)

        init {
            TvFocusHelper.apply(itemView)
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onChannelClick(getItem(pos))
                }
            }
        }

        fun bind(channel: Channel) {
            name.text = channel.name
            if (!channel.logo.isNullOrBlank()) {
                Glide.with(logo.context)
                    .load(channel.logo)
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_live)
                    .error(R.drawable.ic_live)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(logo)
            } else {
                logo.setImageResource(R.drawable.ic_live)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Channel, newItem: Channel) = oldItem == newItem
    }
}
