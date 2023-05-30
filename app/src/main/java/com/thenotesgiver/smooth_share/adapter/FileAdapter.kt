package com.thenotesgiver.smooth_share.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.thenotesgiver.smooth_share.adapter.custom.EditableListAdapter
import com.thenotesgiver.smooth_share.databinding.ListFileBinding
import com.thenotesgiver.smooth_share.databinding.ListSectionTitleBinding

import com.thenotesgiver.smooth_share.model.FileModel
import com.thenotesgiver.smooth_share.model.ListItem
import com.thenotesgiver.smooth_share.model.TitleSectionContentModel
import com.thenotesgiver.smooth_share.viewholder.FileViewHolder
import com.thenotesgiver.smooth_share.viewholder.TitleSectionViewHolder

class FileAdapter(
    context: Context,
    private val clickListener: (FileModel, ClickType) -> Unit,
) : EditableListAdapter<ListItem, EditableListAdapter.EditableViewHolder>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditableViewHolder = when (viewType) {
        VIEW_TYPE_FILE -> FileViewHolder(
            ListFileBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener
        )
        VIEW_TYPE_SECTION -> TitleSectionViewHolder(
            ListSectionTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        else -> throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: EditableViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is FileModel -> if (holder is FileViewHolder) holder.bind(item)
            is TitleSectionContentModel -> if (holder is TitleSectionViewHolder) holder.bind(item)
            else -> throw IllegalStateException()
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).listId
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is FileModel -> VIEW_TYPE_FILE
        is TitleSectionContentModel -> VIEW_TYPE_SECTION
        else -> throw IllegalStateException()
    }

    enum class ClickType {
        Default,
        ToggleSelect
    }

    companion object {
        const val VIEW_TYPE_SECTION = 0

        const val VIEW_TYPE_FILE = 1
    }
}