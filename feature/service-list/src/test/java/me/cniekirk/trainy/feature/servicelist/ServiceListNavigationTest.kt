package me.cniekirk.trainy.feature.servicelist

import junit.framework.TestCase.assertEquals
import me.cniekirk.trainy.core.data.TrainService
import me.cniekirk.trainy.feature.servicedetails.ServiceDetailsRoute
import org.junit.Test

class ServiceListNavigationTest {
    @Test
    fun serviceMapsExactUniqueIdentityToDetailsRoute() {
        val service =
            TrainService(
                id = "gb-nr:L79342:2026-06-19",
                time = "09:20",
                destination = "Exeter St Davids",
                platform = "8",
                isPlatformConfirmed = false,
                operatorName = "South Western Railway",
            )

        assertEquals(
            ServiceDetailsRoute("gb-nr:L79342:2026-06-19"),
            service.toServiceDetailsRoute(),
        )
    }
}
