package me.cniekirk.trainy.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedTrainEntity::class, CachedStationEntity::class, TrackedServiceEntity::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
    exportSchema = true,
)
abstract class TrainyDatabase : RoomDatabase() {
    abstract fun cachedTrainDao(): CachedTrainDao

    abstract fun cachedStationDao(): CachedStationDao

    abstract fun trackedServiceDao(): TrackedServiceDao
}
