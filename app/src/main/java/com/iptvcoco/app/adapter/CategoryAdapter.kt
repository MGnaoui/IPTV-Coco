package com.iptvcoco.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iptvcoco.app.R
import com.iptvcoco.app.model.Category

class CategoryAdapter(
    private val onCategorySelected: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var items = listOf<Category>()
    private var selectedPosition = 0

    fun submitList(list: List<Category>) {
        items = list
        notifyDataSetChanged()
    }

    fun setSelected(position: Int) {
        val old = selectedPosition
        selectedPosition = position
        notifyItemChanged(old)
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tvCategoryName)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    setSelected(pos)
                    onCategorySelected(items[pos])
                }
            }
        }

        fun bind(category: Category, isSelected: Boolean) {
            name.text = category.name
            itemView.isSelected = isSelected
            itemView.setBackgroundResource(R.drawable.bg_category_item)
            if (isSelected) {
                name.setTextColor(itemView.context.getColor(R.color.white))
            } else {
                name.setTextColor(itemView.context.getColor(R.color.gray_text))
            }
        }
    }
}
