package me.cniekirk.trainy.feature.search.time

import dev.zacsweers.metro.Inject

@Inject
class CurrentTimeProvider {
    fun currentTimeMillis(): Long = System.currentTimeMillis()
}
