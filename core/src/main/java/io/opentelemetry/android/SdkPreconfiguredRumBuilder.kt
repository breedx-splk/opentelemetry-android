/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.android

import android.app.Application
import io.opentelemetry.android.config.OtelRumConfig
import io.opentelemetry.android.instrumentation.AndroidInstrumentation
import io.opentelemetry.android.instrumentation.AndroidInstrumentationLoader
import io.opentelemetry.android.instrumentation.InstallationContext
import io.opentelemetry.android.internal.services.Services
import io.opentelemetry.android.internal.session.SessionIdTimeoutHandler
import io.opentelemetry.android.session.SessionManager
import io.opentelemetry.sdk.OpenTelemetrySdk

class SdkPreconfiguredRumBuilder
    @JvmOverloads
    internal constructor(
        private val application: Application,
        private val sdk: OpenTelemetrySdk,
        private val timeoutHandler: SessionIdTimeoutHandler,
        private val sessionManager: SessionManager,
        private val config: OtelRumConfig,
        private val services: Services,
    ) {
        private val instrumentations = mutableListOf<AndroidInstrumentation>()
        private val appLifecycleService by lazy { services.appLifecycle }

        /**
         * Adds an instrumentation to be applied as a part of the [build] method call.
         *
         * @return `this`
         */
        fun addInstrumentation(instrumentation: AndroidInstrumentation): SdkPreconfiguredRumBuilder {
            instrumentations.add(instrumentation)
            return this
        }

        /**
         * Creates a new instance of [OpenTelemetryRum] with the settings of this [ ].
         *
         *
         * This method uses a preconfigured OpenTelemetry SDK and install built-in system
         * instrumentations in the passed Android [Application].
         *
         * @return A new [OpenTelemetryRum] instance.
         */
        fun build(): OpenTelemetryRum {
            // the app state listeners need to be run in the first ActivityLifecycleCallbacks since they
            // might turn off/on additional telemetry depending on whether the app is active or not
            appLifecycleService.registerListener(timeoutHandler)

            val openTelemetryRum = OpenTelemetryRumImpl(sdk, sessionManager)

            // Install instrumentations
            val ctx = InstallationContext(application, openTelemetryRum.openTelemetry, sessionManager)
            for (instrumentation in getEnabledInstrumentations()) {
                instrumentation.install(ctx)
            }

            return openTelemetryRum
        }

        private fun getEnabledInstrumentations(): List<AndroidInstrumentation> =
            getInstrumentations().filter { inst -> !config.isSuppressed(inst.name) }

        private fun getInstrumentations(): List<AndroidInstrumentation> {
            if (config.shouldDiscoverInstrumentations()) {
                instrumentations.addAll(AndroidInstrumentationLoader.get().getAll())
            }

            return instrumentations
        }
    }
