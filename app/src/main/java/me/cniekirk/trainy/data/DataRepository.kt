package me.cniekirk.trainy.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface DataRepository {
    val data: Flow<List<String>>
}

@Inject
@ContributesBinding(AppScope::class)
class DefaultDataRepository : DataRepository {
    override val data: Flow<List<String>> = flow { emit(listOf("Android")) }
}
