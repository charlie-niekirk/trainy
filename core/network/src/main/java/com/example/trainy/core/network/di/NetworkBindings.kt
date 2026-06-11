package com.example.trainy.core.network.di

import com.example.trainy.core.network.NetworkSerialization
import com.example.trainy.core.network.RetrofitFactory
import com.example.trainy.core.network.retrofit.RttProxyApi
import com.example.trainy.core.network.source.ClientTokensNetworkDataSource
import com.example.trainy.core.network.source.JourneyDataNetworkDataSource
import com.example.trainy.core.network.source.RetrofitClientTokensNetworkDataSource
import com.example.trainy.core.network.source.RetrofitJourneyDataNetworkDataSource
import com.example.trainy.core.network.source.RetrofitRttProxyStatusNetworkDataSource
import com.example.trainy.core.network.source.RttProxyStatusNetworkDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Qualifier
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit

private const val RTT_PROXY_BASE_URL = "https://api.cniekirk.online/"

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RttProxyBaseUrl

@BindingContainer
object NetworkBindings {

    @Provides
    @RttProxyBaseUrl
    fun provideRttProxyBaseUrl(): String = RTT_PROXY_BASE_URL

    @Provides
    @SingleIn(AppScope::class)
    fun provideNetworkJson(): Json = NetworkSerialization.json

    @Provides
    @SingleIn(AppScope::class)
    fun provideRetrofit(
        @RttProxyBaseUrl baseUrl: String,
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = RetrofitFactory.create(
        baseUrl = baseUrl,
        okHttpClient = okHttpClient,
        json = json,
    )

    @Provides
    @SingleIn(AppScope::class)
    fun provideRttProxyApi(retrofit: Retrofit): RttProxyApi =
        retrofit.create(RttProxyApi::class.java)

    @Provides
    fun provideClientTokensNetworkDataSource(
        dataSource: RetrofitClientTokensNetworkDataSource,
    ): ClientTokensNetworkDataSource = dataSource

    @Provides
    fun provideJourneyDataNetworkDataSource(
        dataSource: RetrofitJourneyDataNetworkDataSource,
    ): JourneyDataNetworkDataSource = dataSource

    @Provides
    fun provideRttProxyStatusNetworkDataSource(
        dataSource: RetrofitRttProxyStatusNetworkDataSource,
    ): RttProxyStatusNetworkDataSource = dataSource
}
