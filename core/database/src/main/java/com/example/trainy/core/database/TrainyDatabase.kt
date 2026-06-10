package com.example.trainy.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedTrainEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TrainyDatabase : RoomDatabase() {
    abstract fun cachedTrainDao(): CachedTrainDao
}
