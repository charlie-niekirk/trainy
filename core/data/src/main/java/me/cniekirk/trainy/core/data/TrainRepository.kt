package me.cniekirk.trainy.core.data

import kotlinx.coroutines.flow.Flow

interface TrainRepository {
    fun observeTrainNames(): Flow<List<String>>
}
