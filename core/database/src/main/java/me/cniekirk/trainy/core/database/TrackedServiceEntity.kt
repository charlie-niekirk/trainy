package me.cniekirk.trainy.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_services")
data class TrackedServiceEntity(
    @PrimaryKey val serviceId: String,
    val time: String,
    val destination: String,
    val platform: String?,
    val isPlatformConfirmed: Boolean,
    val operatorName: String,
    val trackedAtEpochMillis: Long,
)
