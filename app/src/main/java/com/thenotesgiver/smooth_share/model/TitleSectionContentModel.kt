package com.thenotesgiver.smooth_share.model

data class TitleSectionContentModel(val title: String): ListItem {
    override val listId: Long = title.hashCode().toLong() + javaClass.hashCode()
}