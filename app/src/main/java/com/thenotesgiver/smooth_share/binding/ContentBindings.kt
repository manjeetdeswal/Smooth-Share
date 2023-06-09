package com.thenotesgiver.smooth_share.binding

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.thenotesgiver.smooth_share.GlideApp
import com.thenotesgiver.smooth_share.fragment.TransferItem
import com.thenotesgiver.smooth_share.fragment.dialog.WebTransferContentViewModel
import com.thenotesgiver.smooth_share.util.MimeIcons
import com.thenotesgiver.smooth_share.viewmodel.content.FileContentViewModel

private fun load(imageView: ImageView, uri: Uri, circle: Boolean = false, @DrawableRes fallback: Int = 0) {
    GlideApp.with(imageView)
        .load(uri)
        .override(200)
        .also {
            if (fallback != 0) {
                it.fallback(fallback)
                    .error(fallback)
            }

            if (circle) {
                it.circleCrop()
            } else {
                it.centerCrop()
            }
        }
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
}

@BindingAdapter("thumbnailOf")
fun loadThumbnailOf(imageView: ImageView, viewModel: FileContentViewModel) {
    if (viewModel.mimeType.startsWith("image/") || viewModel.mimeType.startsWith("video/")) {
        load(imageView, viewModel.uri, circle = true)
    } else {
        imageView.setImageDrawable(null)
    }
}

@BindingAdapter("iconOf")
fun loadIconOf(imageView: ImageView, mimeType: String) {
    imageView.setImageResource(MimeIcons.loadMimeIcon(mimeType))
}

@BindingAdapter("iconOf")
fun loadIconOf(imageView: ImageView, icon: Int) {
    GlideApp.with(imageView)
        .load(icon)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
}

@BindingAdapter("thumbnailOf")
fun loadThumbnailOf(imageView: ImageView, item: TransferItem) {
    if (item.mimeType.startsWith("image/") || item.mimeType.startsWith("video/")) {
        load(imageView, Uri.parse(item.location), circle = true)
    } else {
        imageView.setImageDrawable(null)
    }
}

@BindingAdapter("thumbnailOf")
fun loadThumbnailOf(imageView: ImageView, viewModel: WebTransferContentViewModel) {
    if (viewModel.mimeType.startsWith("image/") || viewModel.mimeType.startsWith("video/")) {
        load(imageView, viewModel.uri, circle = true)
    } else {
        imageView.setImageDrawable(null)
    }
}