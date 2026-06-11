package me.cniekirk.trainy.core.network.source

import me.cniekirk.trainy.core.network.generated.model.HealthResponse
import me.cniekirk.trainy.core.network.generated.model.MetaResponse
import me.cniekirk.trainy.core.network.retrofit.RttProxyApi
import dev.zacsweers.metro.Inject

@Inject
class RetrofitRttProxyStatusNetworkDataSource(
    private val api: RttProxyApi,
) : RttProxyStatusNetworkDataSource {
    override suspend fun getMeta(): MetaResponse = api.getMeta()

    override suspend fun getLiveHealth(): HealthResponse = api.getLiveHealth()

    override suspend fun getReadyHealth(): HealthResponse = api.getReadyHealth()
}
