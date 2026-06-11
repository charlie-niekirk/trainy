package me.cniekirk.trainy.core.network.source

import me.cniekirk.trainy.core.network.generated.model.ClientTokenResponse

interface ClientTokensNetworkDataSource {
    suspend fun createClientToken(): ClientTokenResponse
}
