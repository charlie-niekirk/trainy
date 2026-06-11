package com.example.trainy.core.network.source

import com.example.trainy.core.network.generated.model.HealthResponse
import com.example.trainy.core.network.generated.model.MetaResponse
import com.example.trainy.core.network.retrofit.RttProxyApi
import dev.zacsweers.metro.Inject

@Inject
class RetrofitRttProxyStatusNetworkDataSource(
    private val api: RttProxyApi,
) : RttProxyStatusNetworkDataSource {
    override suspend fun getMeta(): MetaResponse = api.getMeta()

    override suspend fun getLiveHealth(): HealthResponse = api.getLiveHealth()

    override suspend fun getReadyHealth(): HealthResponse = api.getReadyHealth()
}
