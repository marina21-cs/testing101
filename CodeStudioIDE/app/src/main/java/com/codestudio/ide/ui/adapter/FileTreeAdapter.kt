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
import com.codestudio.ide.model.FileItem

class FileTreeAdapter(
    private val onFileClick: (FileItem) -> Unit,
    private val onFileLongClick: (FileItem) -> Boolean,
    private val onFolderToggle: (FileItem) -> Unit
) : ListAdapter<FileItem, FileTreeAdapter.ViewHolder>(FileItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_tree, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.fileIcon)
        private val nameView: TextView = itemView.findViewById(R.id.fileName)
        private val expandIcon: ImageView = itemView.findViewById(R.id.expandIcon)

        fun bind(item: FileItem) {
            // Set indentation based on depth
            val padding = (item.depth * 24 * itemView.context.resources.displayMetrics.density).toInt()
            itemView.setPadding(padding, itemView.paddingTop, itemView.paddingRight, itemView.paddingBottom)

            // Set icon
            iconView.setImageResource(item.getIcon())

            // Set name
            nameView.text = item.name

            // Set expand/collapse icon for directories
            if (item.isDirectory) {
                expandIcon.visibility = View.VISIBLE
                expandIcon.setImageResource(
                    if (item.isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                )
                expandIcon.setOnClickListener {
                    onFolderToggle(item)
                }
            } else {
                expandIcon.visibility = View.GONE
            }

            // Click listeners
            itemView.setOnClickListener {
                if (item.isDirectory) {
                    onFolderToggle(item)
                } else {
                    onFileClick(item)
                }
            }

            itemView.setOnLongClickListener {
                onFileLongClick(item)
            }
        }
    }

    class FileItemDiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem == newItem
        }
    }
}
