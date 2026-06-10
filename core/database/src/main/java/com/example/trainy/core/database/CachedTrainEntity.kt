package com.example.trainy.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_trains")
data class CachedTrainEntity(
    @PrimaryKey val id: String,
    val name: String,
)
