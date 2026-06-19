package me.cniekirk.trainy.core.data

interface StationRepository {
    suspend fun getStations(): List<Station>
}
