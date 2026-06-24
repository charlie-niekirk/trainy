package me.cniekirk.trainy.core.data

interface TrainRepository {
    suspend fun getServices(query: ServiceListQuery): List<TrainService>
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
