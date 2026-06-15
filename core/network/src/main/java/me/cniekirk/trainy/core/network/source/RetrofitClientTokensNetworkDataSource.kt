package me.cniekirk.trainy.core.network.source

import dev.zacsweers.metro.Inject
import me.cniekirk.trainy.core.network.generated.model.ClientTokenResponse
import me.cniekirk.trainy.core.network.retrofit.RttProxyApi

@Inject
class RetrofitClientTokensNetworkDataSource(private val api: RttProxyApi) :
    ClientTokensNetworkDataSource {
    override suspend fun createClientToken(): ClientTokenResponse = api.createClientToken()
}
