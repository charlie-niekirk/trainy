package me.cniekirk.trainy.core.network.source

import me.cniekirk.trainy.core.network.generated.model.BoardResponse
import me.cniekirk.trainy.core.network.generated.model.ServiceResponse
import me.cniekirk.trainy.core.network.generated.model.StationsResponse
import me.cniekirk.trainy.core.network.generated.model.StopsResponse
import me.cniekirk.trainy.core.network.model.RttProxyBearerToken

interface JourneyDataNetworkDataSource {
    suspend fun getStations(bearerToken: RttProxyBearerToken): StationsResponse

    suspend fun searchStops(
        bearerToken: RttProxyBearerToken,
        query: String? = null,
        limit: Int? = null,
    ): StopsResponse

    suspend fun getBoard(
        bearerToken: RttProxyBearerToken,
        code: String,
        filterFrom: String? = null,
        filterTo: String? = null,
        timeFrom: String? = null,
        timeTo: String? = null,
        timeWindow: Int? = null,
        timeTolerance: Boolean? = null,
        stpFilter: String? = null,
    ): BoardResponse

    suspend fun getService(
        bearerToken: RttProxyBearerToken,
        identity: String,
        departureDate: String? = null,
    ): ServiceResponse

    suspend fun getServiceByUniqueIdentity(
        bearerToken: RttProxyBearerToken,
        uniqueIdentity: String,
    ): ServiceResponse
}
