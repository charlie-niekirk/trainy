package me.cniekirk.trainy.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import me.cniekirk.trainy.core.database.TrackedServiceDao
import me.cniekirk.trainy.core.database.TrackedServiceEntity
import me.cniekirk.trainy.core.network.model.RttProxyBearerToken
import me.cniekirk.trainy.core.network.source.ClientTokensNetworkDataSource
import me.cniekirk.trainy.core.network.source.JourneyDataNetworkDataSource

@Inject
@ContributesBinding(AppScope::class)
class DefaultTrainRepository(
    private val clientTokens: ClientTokensNetworkDataSource,
    private val journeyData: JourneyDataNetworkDataSource,
    private val trackedServiceDao: TrackedServiceDao,
    private val trackedServiceClock: TrackedServiceClock,
) : TrainRepository {
    override suspend fun getServices(query: ServiceListQuery): List<TrainService> {
        val token = RttProxyBearerToken(clientTokens.createClientToken().token)
        val response =
            journeyData.getBoard(
                bearerToken = token,
                code = query.stationCode,
                filterFrom = query.filterStationCode.takeIf { query.isArrivals },
                filterTo = query.filterStationCode.takeUnless { query.isArrivals },
                timeFrom = query.dateTimeMillis.toApiDateTime(),
            )

        return (response.data?.get("services") as? JsonArray).orEmpty().mapNotNull {
            it.toTrainService(query.isArrivals)
        }
    }

    override suspend fun getServiceDetails(serviceId: String): TrainServiceDetails {
        val token = RttProxyBearerToken(clientTokens.createClientToken().token)
        return requireNotNull(
            journeyData
                .getServiceByUniqueIdentity(
                    token,
                    serviceId.removePrefix(NETWORK_RAIL_NAMESPACE_PREFIX),
                )
                .toTrainServiceDetails()
        ) {
            "Service details response did not contain a usable service"
        }
    }

    override fun observeTrackedServices(): Flow<List<TrackedTrainService>> =
        trackedServiceDao.observeAll().map { services ->
            services.map(TrackedServiceEntity::toModel)
        }

    override fun observeTrackedServiceIds(): Flow<Set<String>> =
        trackedServiceDao.observeIds().map { it.toSet() }

    override suspend fun trackService(service: TrainService) {
        trackedServiceDao.upsert(
            service.toTrackedServiceEntity(trackedServiceClock.nowEpochMillis())
        )
    }

    override suspend fun untrackService(serviceId: String) {
        trackedServiceDao.delete(serviceId)
    }
}

private fun TrainService.toTrackedServiceEntity(trackedAtEpochMillis: Long): TrackedServiceEntity =
    TrackedServiceEntity(
        serviceId = id,
        time = time,
        destination = destination,
        platform = platform,
        isPlatformConfirmed = isPlatformConfirmed,
        operatorName = operatorName,
        trackedAtEpochMillis = trackedAtEpochMillis,
    )

private fun TrackedServiceEntity.toModel(): TrackedTrainService =
    TrackedTrainService(
        serviceId = serviceId,
        time = time,
        destination = destination,
        platform = platform,
        isPlatformConfirmed = isPlatformConfirmed,
        operatorName = operatorName,
        trackedAtEpochMillis = trackedAtEpochMillis,
    )

private fun JsonElement.toTrainService(isArrivals: Boolean): TrainService? {
    val service = this as? JsonObject
    val schedule = service?.objectValue("scheduleMetadata")
    val temporal = service?.objectValue("temporalData")
    val call = temporal?.objectValue(if (isArrivals) "arrival" else "departure")
    val platform = service?.objectValue("locationMetadata")?.objectValue("platform")
    val actualPlatform = platform?.stringValue("actual")
    val displayedPlatform =
        actualPlatform ?: platform?.stringValue("forecast") ?: platform?.stringValue("planned")
    val advertisedTime = call?.stringValue("scheduleAdvertised")
    val id = schedule?.stringValue("uniqueIdentity") ?: schedule?.stringValue("identity")
    val destination =
        service
            ?.arrayValue("destination")
            ?.firstOrNull()
            ?.let { it as? JsonObject }
            ?.objectValue("location")
            ?.stringValue("description")
    val operatorName = schedule?.objectValue("operator")?.stringValue("name")

    return if (id == null || advertisedTime == null) {
        null
    } else if (destination == null || operatorName == null) {
        null
    } else {
        TrainService(
            id = id,
            time = advertisedTime.toDisplayTime(),
            destination = destination,
            platform = displayedPlatform,
            isPlatformConfirmed = actualPlatform != null,
            operatorName = operatorName,
        )
    }
}

private fun JsonObject.objectValue(key: String): JsonObject? = get(key) as? JsonObject

private fun JsonObject.arrayValue(key: String): JsonArray? = get(key) as? JsonArray

private fun JsonObject.stringValue(key: String): String? =
    get(key)?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)

private fun String.toDisplayTime(): String =
    substringAfter(ISO_TIME_SEPARATOR, this).take(DISPLAY_TIME_LENGTH).takeIf {
        it.length == DISPLAY_TIME_LENGTH
    } ?: this

private fun Long.toApiDateTime(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.UK)
        .apply { timeZone = TimeZone.getDefault() }
        .format(Date(this))

private const val ISO_TIME_SEPARATOR = 'T'
private const val DISPLAY_TIME_LENGTH = 5
private const val NETWORK_RAIL_NAMESPACE_PREFIX = "gb-nr:"
