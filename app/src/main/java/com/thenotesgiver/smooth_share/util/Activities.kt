package com.thenotesgiver.smooth_share.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.thenotesgiver.smooth_share.framework.io.DocumentFile
import com.thenotesgiver.smooth_share.R

object Activities {

    fun view(context: Context, documentFile: DocumentFile) {
        try {
            view(
                context,
                documentFile.getSecureUri(context, "com.thenotesgiver.smooth_share"),
                documentFile.getType()
            )
        } catch (e: Exception) {
            Toast.makeText(context, R.string.unknown_failure, Toast.LENGTH_LONG).show()
        }
    }

    fun view(context: Context, uri: Uri, type: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, type)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_no_activity_to_view, Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, R.string.error_content_not_found, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_LONG).show()
        }
    }
}