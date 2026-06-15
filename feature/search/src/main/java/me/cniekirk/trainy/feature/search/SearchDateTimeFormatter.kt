package me.cniekirk.trainy.feature.search

import dev.zacsweers.metro.Inject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SearchDateTimeFormatter internal constructor(
    private val locale: Locale = Locale.UK,
    private val timeZone: TimeZone = TimeZone.getDefault(),
) {
    @Inject
    constructor() : this(Locale.UK, TimeZone.getDefault())

    fun format(timestampMillis: Long): SearchDateTime =
        SearchDateTime(
            date = formatter(DATE_PATTERN).format(Date(timestampMillis)),
            time = formatter(TIME_PATTERN).format(Date(timestampMillis)),
        )

    fun parse(
        date: String,
        time: String,
    ): Long? =
        try {
            formatter("$DATE_PATTERN $TIME_PATTERN").parse("${date.trim()} ${time.trim()}")?.time
        } catch (_: ParseException) {
            null
        }

    private fun formatter(pattern: String): SimpleDateFormat =
        SimpleDateFormat(pattern, locale).apply {
            isLenient = false
            timeZone = this@SearchDateTimeFormatter.timeZone
        }

    private companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val TIME_PATTERN = "HH:mm"
    }
}
