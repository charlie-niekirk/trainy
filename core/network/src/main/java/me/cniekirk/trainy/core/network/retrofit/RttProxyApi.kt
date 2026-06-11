package me.cniekirk.trainy.core.network.retrofit

import me.cniekirk.trainy.core.network.generated.model.BoardResponse
import me.cniekirk.trainy.core.network.generated.model.ClientTokenResponse
import me.cniekirk.trainy.core.network.generated.model.HealthResponse
import me.cniekirk.trainy.core.network.generated.model.MetaResponse
import me.cniekirk.trainy.core.network.generated.model.ServiceResponse
import me.cniekirk.trainy.core.network.generated.model.StopsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RttProxyApi {

    @POST("v1/client-tokens")
    suspend fun createClientToken(): ClientTokenResponse

    @GET("v1/stops")
    suspend fun searchStops(
        @Header("Authorization") authorization: String,
        @Query("query") query: String? = null,
        @Query("limit") limit: Int? = null,
    ): StopsResponse

    @GET("v1/boards/{code}")
    suspend fun getBoard(
        @Header("Authorization") authorization: String,
        @Path("code") code: String,
        @Query("filterFrom") filterFrom: String? = null,
        @Query("filterTo") filterTo: String? = null,
        @Query("timeFrom") timeFrom: String? = null,
        @Query("timeTo") timeTo: String? = null,
        @Query("timeWindow") timeWindow: Int? = null,
        @Query("timeTolerance") timeTolerance: Boolean? = null,
        @Query("stpFilter") stpFilter: String? = null,
    ): BoardResponse

    @GET("v1/services/{identity}")
    suspend fun getService(
        @Header("Authorization") authorization: String,
        @Path("identity") identity: String,
        @Query("departureDate") departureDate: String? = null,
    ): ServiceResponse

    @GET("v1/services/by-uid/{uniqueIdentity}")
    suspend fun getServiceByUniqueIdentity(
        @Header("Authorization") authorization: String,
        @Path("uniqueIdentity") uniqueIdentity: String,
    ): ServiceResponse

    @GET("v1/meta")
    suspend fun getMeta(): MetaResponse

    @GET("health/live")
    suspend fun getLiveHealth(): HealthResponse

    @GET("health/ready")
    suspend fun getReadyHealth(): HealthResponse
}
