package me.cniekirk.trainy.core.data

import kotlinx.coroutines.flow.Flow

interface TrainRepository {
    suspend fun getServices(query: ServiceListQuery): List<TrainService>

    suspend fun getServiceDetails(serviceId: String): TrainServiceDetails

    fun observeTrackedServices(): Flow<List<TrackedTrainService>>

    fun observeTrackedServiceIds(): Flow<Set<String>>

    suspend fun trackService(service: TrainService)

    suspend fun untrackService(serviceId: String)
}

data class ServiceListQuery(
    val stationCode: String,
    val filterStationCode: String?,
    val isArrivals: Boolean,
    val dateTimeMillis: Long,
)

data class TrainService(
    val id: String,
    val time: String,
    val destination: String,
    val platform: String?,
    val isPlatformConfirmed: Boolean,
    val operatorName: String,
)

data class TrackedTrainService(
    val serviceId: String,
    val time: String,
    val destination: String,
    val platform: String?,
    val isPlatformConfirmed: Boolean,
    val operatorName: String,
    val trackedAtEpochMillis: Long,
)
