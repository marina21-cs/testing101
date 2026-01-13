package com.codestudio.ide.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codestudio.ide.R
import com.codestudio.ide.model.OpenFile

class TabAdapter(
    private val onTabClick: (Int) -> Unit,
    private val onTabClose: (Int) -> Unit
) : ListAdapter<OpenFile, TabAdapter.ViewHolder>(OpenFileDiffCallback()) {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun setSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        notifyItemChanged(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.tabIcon)
        private val nameView: TextView = itemView.findViewById(R.id.tabName)
        private val closeButton: ImageView = itemView.findViewById(R.id.tabClose)
        private val modifiedIndicator: View = itemView.findViewById(R.id.modifiedIndicator)

        fun bind(item: OpenFile, isSelected: Boolean) {
            iconView.setImageResource(item.fileItem.getIcon())
            nameView.text = item.fileItem.name
            modifiedIndicator.visibility = if (item.hasUnsavedChanges) View.VISIBLE else View.GONE

            itemView.isSelected = isSelected
            itemView.setBackgroundResource(
                if (isSelected) R.drawable.tab_selected_background
                else R.drawable.tab_background
            )

            itemView.setOnClickListener {
                onTabClick(bindingAdapterPosition)
            }

            closeButton.setOnClickListener {
                onTabClose(bindingAdapterPosition)
            }
        }
    }

    class OpenFileDiffCallback : DiffUtil.ItemCallback<OpenFile>() {
        override fun areItemsTheSame(oldItem: OpenFile, newItem: OpenFile): Boolean {
            return oldItem.fileItem.path == newItem.fileItem.path
        }

        override fun areContentsTheSame(oldItem: OpenFile, newItem: OpenFile): Boolean {
            return oldItem.fileItem.path == newItem.fileItem.path &&
                    oldItem.hasUnsavedChanges == newItem.hasUnsavedChanges
        }
    }
}
