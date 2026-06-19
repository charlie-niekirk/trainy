package me.cniekirk.trainy.core.network.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Qualifier
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json
import me.cniekirk.trainy.core.network.NetworkSerialization
import me.cniekirk.trainy.core.network.RetrofitFactory
import me.cniekirk.trainy.core.network.retrofit.RttProxyApi
import me.cniekirk.trainy.core.network.source.ClientTokensNetworkDataSource
import me.cniekirk.trainy.core.network.source.JourneyDataNetworkDataSource
import me.cniekirk.trainy.core.network.source.RetrofitClientTokensNetworkDataSource
import me.cniekirk.trainy.core.network.source.RetrofitJourneyDataNetworkDataSource
import me.cniekirk.trainy.core.network.source.RetrofitRttProxyStatusNetworkDataSource
import me.cniekirk.trainy.core.network.source.RttProxyStatusNetworkDataSource
import okhttp3.OkHttpClient
import retrofit2.Retrofit

private const val RTT_PROXY_BASE_URL = "https://api.cniekirk.online/"

@Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class RttProxyBaseUrl

@ContributesTo(AppScope::class)
@BindingContainer
object NetworkBindings {

    @Provides @RttProxyBaseUrl fun provideRttProxyBaseUrl(): String = RTT_PROXY_BASE_URL

    @Provides @SingleIn(AppScope::class) fun provideNetworkJson(): Json = NetworkSerialization.json

    @Provides
    @SingleIn(AppScope::class)
    fun provideRetrofit(
        @RttProxyBaseUrl baseUrl: String,
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        RetrofitFactory.create(
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
        dataSource: RetrofitClientTokensNetworkDataSource
    ): ClientTokensNetworkDataSource = dataSource

    @Provides
    fun provideJourneyDataNetworkDataSource(
        dataSource: RetrofitJourneyDataNetworkDataSource
    ): JourneyDataNetworkDataSource = dataSource

    @Provides
    fun provideRttProxyStatusNetworkDataSource(
        dataSource: RetrofitRttProxyStatusNetworkDataSource
    ): RttProxyStatusNetworkDataSource = dataSource
}
