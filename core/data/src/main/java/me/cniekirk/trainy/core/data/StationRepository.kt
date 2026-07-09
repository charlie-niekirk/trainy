package me.cniekirk.trainy.core.data

interface StationRepository {
    suspend fun getStations(): List<Station>

    suspend fun getStationDetails(crsCode: String): StationDetails
}
