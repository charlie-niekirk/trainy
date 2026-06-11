package com.example.trainy.core.network.source

import com.example.trainy.core.network.generated.model.ClientTokenResponse
import com.example.trainy.core.network.retrofit.RttProxyApi
import dev.zacsweers.metro.Inject

@Inject
class RetrofitClientTokensNetworkDataSource(
    private val api: RttProxyApi,
) : ClientTokensNetworkDataSource {
    override suspend fun createClientToken(): ClientTokenResponse = api.createClientToken()
}
