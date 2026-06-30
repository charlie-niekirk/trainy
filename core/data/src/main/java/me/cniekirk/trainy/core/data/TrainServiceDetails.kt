package me.cniekirk.trainy.core.data

data class TrainServiceDetails(
    val origin: String,
    val destination: String,
    val operatorName: String,
    val time: String,
    val stops: List<TrainServiceStop>,
)

data class TrainServiceStop(
    val name: String,
    val time: String,
    val platform: String?,
)
