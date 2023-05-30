package com.thenotesgiver.smooth_share.viewholder


import com.thenotesgiver.smooth_share.adapter.FileAdapter
import com.thenotesgiver.smooth_share.adapter.custom.EditableListAdapter
import com.thenotesgiver.smooth_share.databinding.ListFileBinding

import com.thenotesgiver.smooth_share.model.FileModel
import com.thenotesgiver.smooth_share.viewmodel.content.FileContentViewModel

class FileViewHolder(
    private val binding: ListFileBinding,
    private val clickListener: (FileModel, FileAdapter.ClickType) -> Unit,
) : EditableListAdapter.EditableViewHolder(binding.root) {
    fun bind(fileModel: FileModel) {
        binding.viewModel = FileContentViewModel(fileModel)
        binding.root.setOnClickListener {
            clickListener(fileModel, FileAdapter.ClickType.Default)
        }
        binding.selection.setOnClickListener {
            fileModel.isSelected = !fileModel.isSelected
            it.isSelected = fileModel.isSelected
            clickListener(fileModel, FileAdapter.ClickType.ToggleSelect)
        }
        binding.selection.isSelected = fileModel.isSelected
        binding.executePendingBindings()
    }
}