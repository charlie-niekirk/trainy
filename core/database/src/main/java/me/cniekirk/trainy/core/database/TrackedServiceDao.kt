package me.cniekirk.trainy.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedServiceDao {
    @Query("SELECT * FROM tracked_services ORDER BY trackedAtEpochMillis DESC")
    fun observeAll(): Flow<List<TrackedServiceEntity>>

    @Query("SELECT serviceId FROM tracked_services") fun observeIds(): Flow<List<String>>

    @Upsert suspend fun upsert(service: TrackedServiceEntity)

    @Query("DELETE FROM tracked_services WHERE serviceId = :serviceId")
    suspend fun delete(serviceId: String)
}
