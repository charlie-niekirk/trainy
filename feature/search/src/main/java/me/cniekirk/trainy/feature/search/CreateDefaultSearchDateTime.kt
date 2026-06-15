package me.cniekirk.trainy.feature.search

import dev.zacsweers.metro.Inject

@Inject
class CreateDefaultSearchDateTime(
    private val formatter: SearchDateTimeFormatter,
    private val currentTimeProvider: CurrentTimeProvider,
) {
    operator fun invoke(): SearchDateTime =
        formatter.format(currentTimeProvider.currentTimeMillis())
}
