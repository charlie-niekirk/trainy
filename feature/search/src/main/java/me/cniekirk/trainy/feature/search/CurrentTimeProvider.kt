package me.cniekirk.trainy.feature.search

import dev.zacsweers.metro.Inject

@Inject
class CurrentTimeProvider {
    fun currentTimeMillis(): Long = System.currentTimeMillis()
}
