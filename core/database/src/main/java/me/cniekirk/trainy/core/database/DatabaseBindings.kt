package me.cniekirk.trainy.core.database

import android.app.Application
import androidx.room.Room
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object DatabaseBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(application: Application): TrainyDatabase =
        Room.databaseBuilder(application, TrainyDatabase::class.java, "trainy.db").build()

    @Provides
    fun provideCachedStationDao(database: TrainyDatabase): CachedStationDao =
        database.cachedStationDao()
}
