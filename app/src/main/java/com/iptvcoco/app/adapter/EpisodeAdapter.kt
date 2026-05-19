package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iptvcoco.app.R
import com.iptvcoco.app.model.Episode
import com.iptvcoco.app.util.TvFocusHelper

class EpisodeAdapter(
    private val onEpisodeClick: (Episode) -> Unit
) : ListAdapter<Episode, EpisodeAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val number: TextView = itemView.findViewById(R.id.tvEpisodeNumber)
        private val title: TextView = itemView.findViewById(R.id.tvEpisodeTitle)

        init {
            TvFocusHelper.apply(itemView, scale = 1.03f)
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onEpisodeClick(getItem(pos))
                }
            }
        }

        fun bind(episode: Episode) {
            number.text = "E${episode.number}"
            title.text = episode.title
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Episode, newItem: Episode) = oldItem == newItem
    }
}
