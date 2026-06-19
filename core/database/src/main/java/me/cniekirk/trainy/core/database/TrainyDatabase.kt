package me.cniekirk.trainy.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedTrainEntity::class, CachedStationEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true,
)
abstract class TrainyDatabase : RoomDatabase() {
    abstract fun cachedTrainDao(): CachedTrainDao

    abstract fun cachedStationDao(): CachedStationDao
}
