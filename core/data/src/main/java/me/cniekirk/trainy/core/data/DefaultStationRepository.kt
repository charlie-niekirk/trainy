package me.cniekirk.trainy.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import me.cniekirk.trainy.core.database.CachedStationDao
import me.cniekirk.trainy.core.database.CachedStationEntity
import me.cniekirk.trainy.core.network.model.RttProxyBearerToken
import me.cniekirk.trainy.core.network.source.ClientTokensNetworkDataSource
import me.cniekirk.trainy.core.network.source.JourneyDataNetworkDataSource

private const val STATION_CACHE_TTL_MILLIS = 7L * 24 * 60 * 60 * 1_000

@Inject
@ContributesBinding(AppScope::class)
class DefaultStationRepository(
    private val stationDao: CachedStationDao,
    private val clientTokens: ClientTokensNetworkDataSource,
    private val journeyData: JourneyDataNetworkDataSource,
    private val clock: StationCacheClock,
) : StationRepository {
    override suspend fun getStations(): List<Station> {
        val now = clock.nowEpochMillis()
        val latestFetch = stationDao.latestFetchEpochMillis()
        val cacheIsFresh = latestFetch != null && now - latestFetch < STATION_CACHE_TTL_MILLIS

        if (!cacheIsFresh) {
            runCatching { refreshStations(now) }.onFailure { if (latestFetch == null) throw it }
        }

        return stationDao.getAll().map { Station(name = it.name, crsCode = it.crsCode) }
    }

    override suspend fun getStationDetails(crsCode: String): StationDetails {
        val token = RttProxyBearerToken(clientTokens.createClientToken().token)
        return journeyData.getStation(token, crsCode).toStationDetails()
    }

    private suspend fun refreshStations(fetchedAt: Long) {
        val token = RttProxyBearerToken(clientTokens.createClientToken().token)
        val stations =
            journeyData.getStations(token).data.map {
                CachedStationEntity(
                    crsCode = it.crsCode,
                    name = it.name,
                    fetchedAtEpochMillis = fetchedAt,
                )
            }
        stationDao.replaceAll(stations)
    }
}
