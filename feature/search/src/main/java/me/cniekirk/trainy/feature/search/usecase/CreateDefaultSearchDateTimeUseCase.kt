package me.cniekirk.trainy.feature.search.usecase

import dev.zacsweers.metro.Inject
import me.cniekirk.trainy.feature.search.SearchDateTime
import me.cniekirk.trainy.feature.search.time.CurrentTimeProvider
import me.cniekirk.trainy.feature.search.time.SearchDateTimeFormatter

@Inject
class CreateDefaultSearchDateTimeUseCase(
    private val formatter: SearchDateTimeFormatter,
    private val currentTimeProvider: CurrentTimeProvider,
) {
    operator fun invoke(): SearchDateTime =
        formatter.format(currentTimeProvider.currentTimeMillis())
}
