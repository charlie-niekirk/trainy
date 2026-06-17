package me.cniekirk.trainy

import android.app.Application
import dev.zacsweers.metro.createGraph
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication
import me.cniekirk.trainy.di.AppGraph

class TrainyApplication : Application(), MetroApplication {

    private val appGraph by lazy { createGraph<AppGraph>() }

    override val appComponentProviders: MetroAppComponentProviders
        get() = appGraph
}
