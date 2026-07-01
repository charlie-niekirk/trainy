package me.cniekirk.trainy.core.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import me.cniekirk.trainy.core.network.generated.model.ServiceResponse

internal fun ServiceResponse.toTrainServiceDetails(): TrainServiceDetails? {
    val service = data.objectValue("service")
    val operatorName =
        service?.objectValue("scheduleMetadata")?.objectValue("operator")?.stringValue("name")
    val stops =
        service?.arrayValue("locations").orEmpty().mapNotNull(JsonElement::toTrainServiceStop)

    return if (service != null && operatorName != null && stops.isNotEmpty()) {
        TrainServiceDetails(
            origin = service.routeLocation("origin") ?: stops.first().name,
            destination = service.routeLocation("destination") ?: stops.last().name,
            operatorName = operatorName,
            time = stops.first().time,
            stops = stops,
        )
    } else {
        null
    }
}

private fun JsonElement.toTrainServiceStop(): TrainServiceStop? {
    val stop = this as? JsonObject
    val temporalData = stop?.objectValue("temporalData")
    val displayAs = temporalData?.stringValue("displayAs")
    val name = stop?.objectValue("location")?.stringValue("description")
    val time =
        temporalData?.objectValue("departure")?.stringValue("scheduleAdvertised")
            ?: temporalData?.objectValue("arrival")?.stringValue("scheduleAdvertised")
    val platform = stop?.objectValue("locationMetadata")?.objectValue("platform")
    val isPublicStop = displayAs != null && displayAs != PASS_DISPLAY_TYPE

    return if (!isPublicStop || name == null || time == null) {
        null
    } else {
        TrainServiceStop(
            name = name,
            time = time.toDisplayTime(),
            platform =
                platform?.stringValue("actual")
                    ?: platform?.stringValue("forecast")
                    ?: platform?.stringValue("planned"),
        )
    }
}

private fun JsonObject.routeLocation(key: String): String? =
    arrayValue(key)
        ?.firstOrNull()
        ?.let { it as? JsonObject }
        ?.objectValue("location")
        ?.stringValue("description")

private fun Map<String, JsonElement>.objectValue(key: String): JsonObject? = get(key) as? JsonObject

private fun JsonObject.objectValue(key: String): JsonObject? = get(key) as? JsonObject

private fun JsonObject.arrayValue(key: String): JsonArray? = get(key) as? JsonArray

private fun JsonObject.stringValue(key: String): String? =
    get(key)?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)

private fun String.toDisplayTime(): String =
    substringAfter(ISO_TIME_SEPARATOR, this).take(DISPLAY_TIME_LENGTH).takeIf {
        it.length == DISPLAY_TIME_LENGTH
    } ?: this

private const val PASS_DISPLAY_TYPE = "PASS"
private const val ISO_TIME_SEPARATOR = 'T'
private const val DISPLAY_TIME_LENGTH = 5
