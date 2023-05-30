package com.thenotesgiver.smooth_share.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thenotesgiver.smooth_share.database.model.*

@Database(
    entities = [
        FavFolder::class,
        WebTransfer::class,
    ],
    views = [],
    version = 1
)
@TypeConverters(
    UriTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favFolderDao(): FavFolderDao

    abstract fun webTransferDao(): WebTransferDao
}