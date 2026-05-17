package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iptvcoco.app.R
import com.iptvcoco.app.model.Channel

class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit,
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

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onChannelClick(items[pos])
                }
            }
        }

        fun bind(channel: Channel) {
            name.text = channel.name
            if (!channel.logo.isNullOrBlank()) {
                Glide.with(logo.context)
                    .load(channel.logo)
                    .placeholder(R.drawable.ic_live)
                    .into(logo)
            } else {
                logo.setImageResource(R.drawable.ic_live)
            }
        }
    }
}
