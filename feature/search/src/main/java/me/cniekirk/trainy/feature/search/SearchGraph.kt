package me.cniekirk.trainy.feature.search

import dev.zacsweers.metro.DependencyGraph

@DependencyGraph
interface SearchGraph {
    val searchViewModel: SearchViewModel
}
