package me.cniekirk.trainy.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_stations")
data class CachedStationEntity(
    @PrimaryKey val crsCode: String,
    val name: String,
    val fetchedAtEpochMillis: Long,
)
