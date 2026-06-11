package com.example.trainy.core.network.source

import com.example.trainy.core.network.generated.model.BoardResponse
import com.example.trainy.core.network.generated.model.ServiceResponse
import com.example.trainy.core.network.generated.model.StopsResponse
import com.example.trainy.core.network.model.RttProxyBearerToken
import com.example.trainy.core.network.retrofit.RttProxyApi
import dev.zacsweers.metro.Inject

@Inject
class RetrofitJourneyDataNetworkDataSource(
    private val api: RttProxyApi,
) : JourneyDataNetworkDataSource {
    override suspend fun searchStops(
        bearerToken: RttProxyBearerToken,
        query: String?,
        limit: Int?,
    ): StopsResponse = api.searchStops(
        authorization = bearerToken.authorizationHeader(),
        query = query,
        limit = limit,
    )

    override suspend fun getBoard(
        bearerToken: RttProxyBearerToken,
        code: String,
        filterFrom: String?,
        filterTo: String?,
        timeFrom: String?,
        timeTo: String?,
        timeWindow: Int?,
        timeTolerance: Boolean?,
        stpFilter: String?,
    ): BoardResponse = api.getBoard(
        authorization = bearerToken.authorizationHeader(),
        code = code,
        filterFrom = filterFrom,
        filterTo = filterTo,
        timeFrom = timeFrom,
        timeTo = timeTo,
        timeWindow = timeWindow,
        timeTolerance = timeTolerance,
        stpFilter = stpFilter,
    )

    override suspend fun getService(
        bearerToken: RttProxyBearerToken,
        identity: String,
        departureDate: String?,
    ): ServiceResponse = api.getService(
        authorization = bearerToken.authorizationHeader(),
        identity = identity,
        departureDate = departureDate,
    )

    override suspend fun getServiceByUniqueIdentity(
        bearerToken: RttProxyBearerToken,
        uniqueIdentity: String,
    ): ServiceResponse = api.getServiceByUniqueIdentity(
        authorization = bearerToken.authorizationHeader(),
        uniqueIdentity = uniqueIdentity,
    )
}
