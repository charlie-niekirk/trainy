package me.cniekirk.trainy.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface TrackedServiceClock {
    fun nowEpochMillis(): Long
}

@Inject
@ContributesBinding(AppScope::class)
class SystemTrackedServiceClock : TrackedServiceClock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
