package com.example.trainy.core.network.source

import com.example.trainy.core.network.generated.model.BoardResponse
import com.example.trainy.core.network.generated.model.ServiceResponse
import com.example.trainy.core.network.generated.model.StopsResponse
import com.example.trainy.core.network.model.RttProxyBearerToken

interface JourneyDataNetworkDataSource {
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
