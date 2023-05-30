package com.thenotesgiver.smooth_share.database

import android.net.Uri
import androidx.room.TypeConverter

class UriTypeConverter {
    @TypeConverter
    fun fromType(uri: Uri): String = uri.toString()

    @TypeConverter
    fun toType(uriString: String): Uri = Uri.parse(uriString)
}