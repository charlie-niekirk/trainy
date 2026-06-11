package me.cniekirk.trainy.core.network.source

import me.cniekirk.trainy.core.network.generated.model.HealthResponse
import me.cniekirk.trainy.core.network.generated.model.MetaResponse

interface RttProxyStatusNetworkDataSource {
    suspend fun getMeta(): MetaResponse

    suspend fun getLiveHealth(): HealthResponse

    suspend fun getReadyHealth(): HealthResponse
}
