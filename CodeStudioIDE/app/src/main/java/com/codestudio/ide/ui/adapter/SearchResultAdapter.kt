package com.codestudio.ide.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codestudio.ide.R
import com.codestudio.ide.model.SearchResult

class SearchResultAdapter(
    private val onResultClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchResultAdapter.ViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameView: TextView = itemView.findViewById(R.id.fileName)
        private val lineNumberView: TextView = itemView.findViewById(R.id.lineNumber)
        private val lineContentView: TextView = itemView.findViewById(R.id.lineContent)

        fun bind(result: SearchResult) {
            fileNameView.text = result.file.name
            lineNumberView.text = "Line ${result.lineNumber}"
            lineContentView.text = result.lineContent.trim()

            itemView.setOnClickListener {
                onResultClick(result)
            }
        }
    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem.file.path == newItem.file.path && 
                   oldItem.lineNumber == newItem.lineNumber
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    }
}
