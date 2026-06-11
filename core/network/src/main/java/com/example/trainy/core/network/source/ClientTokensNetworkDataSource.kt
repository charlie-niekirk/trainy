package com.example.trainy.core.network.source

import com.example.trainy.core.network.generated.model.ClientTokenResponse

interface ClientTokensNetworkDataSource {
    suspend fun createClientToken(): ClientTokenResponse
}
