package com.thenotesgiver.smooth_share.viewmodel.content

import com.thenotesgiver.smooth_share.framework.io.LeafFile
import com.thenotesgiver.smooth_share.R


import com.thenotesgiver.smooth_share.model.FileModel
import com.thenotesgiver.smooth_share.util.MimeIcons

class FileContentViewModel(fileModel: FileModel) {
    val name = fileModel.file.getName()

    val count = fileModel.indexCount

    val isDirectory = fileModel.file.isDirectory()

    val mimeType = fileModel.file.getType()

    val icon = if (isDirectory) R.drawable.ic_folder_white_24dp else MimeIcons.loadMimeIcon(mimeType)

    val indexCount = fileModel.indexCount

    val contentDetail by lazy {
        LeafFile.formatLength(fileModel.file.getLength(), false)
    }

    val uri = fileModel.file.getUri()
}