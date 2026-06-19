package me.cniekirk.trainy.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface StationCacheClock {
    fun nowEpochMillis(): Long
}

@Inject
@ContributesBinding(AppScope::class)
class SystemStationCacheClock : StationCacheClock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
