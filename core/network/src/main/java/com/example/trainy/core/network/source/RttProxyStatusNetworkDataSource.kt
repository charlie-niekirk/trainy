package com.example.trainy.core.network.source

import com.example.trainy.core.network.generated.model.HealthResponse
import com.example.trainy.core.network.generated.model.MetaResponse

interface RttProxyStatusNetworkDataSource {
    suspend fun getMeta(): MetaResponse

    suspend fun getLiveHealth(): HealthResponse

    suspend fun getReadyHealth(): HealthResponse
}
