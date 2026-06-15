package me.cniekirk.trainy.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedTrainDao {
    @Query("SELECT * FROM cached_trains") fun observeAll(): Flow<List<CachedTrainEntity>>

    @Upsert suspend fun upsertAll(trains: List<CachedTrainEntity>)
}
