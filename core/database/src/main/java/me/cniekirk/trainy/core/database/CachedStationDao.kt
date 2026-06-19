package me.cniekirk.trainy.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface CachedStationDao {
    @Query("SELECT * FROM cached_stations ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<CachedStationEntity>

    @Query("SELECT MAX(fetchedAtEpochMillis) FROM cached_stations")
    suspend fun latestFetchEpochMillis(): Long?

    @Query("DELETE FROM cached_stations") suspend fun deleteAll()

    @Upsert suspend fun upsertAll(stations: List<CachedStationEntity>)

    @Transaction
    suspend fun replaceAll(stations: List<CachedStationEntity>) {
        deleteAll()
        upsertAll(stations)
    }
}
