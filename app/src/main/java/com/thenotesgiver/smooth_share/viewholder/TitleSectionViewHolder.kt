package com.thenotesgiver.smooth_share.viewholder


import com.thenotesgiver.smooth_share.adapter.custom.EditableListAdapter
import com.thenotesgiver.smooth_share.databinding.ListSectionTitleBinding

import com.thenotesgiver.smooth_share.model.TitleSectionContentModel
import com.thenotesgiver.smooth_share.viewmodel.content.TitleSectionContentViewModel

class TitleSectionViewHolder(val binding: ListSectionTitleBinding) : EditableListAdapter.EditableViewHolder(binding.root) {
    fun bind(contentModel: TitleSectionContentModel) {
        binding.viewModel = TitleSectionContentViewModel(contentModel)
        binding.executePendingBindings()
    }
}