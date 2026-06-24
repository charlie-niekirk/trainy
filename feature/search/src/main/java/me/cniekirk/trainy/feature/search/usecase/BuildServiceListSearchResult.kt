package me.cniekirk.trainy.feature.search.usecase

import me.cniekirk.trainy.feature.search.SearchValidationError
import me.cniekirk.trainy.feature.servicelist.ServiceListSearch

sealed interface BuildServiceListSearchResult {
    data class Success(val search: ServiceListSearch) : BuildServiceListSearchResult

    data class Error(
        val targetStationError: SearchValidationError? = null,
        val dateTimeError: SearchValidationError? = null,
    ) : BuildServiceListSearchResult
}
