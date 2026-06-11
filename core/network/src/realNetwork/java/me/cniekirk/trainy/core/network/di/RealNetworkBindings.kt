package me.cniekirk.trainy.core.network.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.OkHttpClient

@BindingContainer
object RealNetworkBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()
}
