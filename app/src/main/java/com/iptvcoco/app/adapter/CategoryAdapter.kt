package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.iptvcoco.app.R
import com.iptvcoco.app.model.Category
import com.iptvcoco.app.util.TvFocusHelper

class CategoryAdapter(
    private val onCategorySelected: (Category) -> Unit,
    private val layoutRes: Int = R.layout.item_category
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var items = listOf<Category>()
    private var selectedPosition = 0

    fun submitList(list: List<Category>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = list.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = items[oldPos].id == list[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = items[oldPos] == list[newPos]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = list
        diffResult.dispatchUpdatesTo(this)
    }

    fun setSelected(position: Int) {
        val old = selectedPosition
        selectedPosition = position
        notifyItemChanged(old)
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tvCategoryName)

        init {
            TvFocusHelper.apply(itemView, scale = 1.04f)
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    setSelected(pos)
                    onCategorySelected(items[pos])
                }
            }
        }

        fun bind(category: Category, isSelected: Boolean) {
            name.text = category.name
            itemView.isSelected = isSelected
            if (layoutRes != R.layout.item_category_chip) {
                itemView.setBackgroundResource(R.drawable.bg_category_item)
            }
        }
    }
}
