/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.android.instrumentation.fragment

import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Build
import com.google.auto.service.AutoService
import io.opentelemetry.android.instrumentation.AndroidInstrumentation
import io.opentelemetry.android.instrumentation.common.Constants.INSTRUMENTATION_SCOPE
import io.opentelemetry.android.instrumentation.common.ScreenNameExtractor
import io.opentelemetry.android.internal.services.ServiceManager
import io.opentelemetry.android.internal.services.visiblescreen.fragments.RumFragmentActivityRegisterer
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer

@AutoService(AndroidInstrumentation::class)
class FragmentLifecycleInstrumentation : AndroidInstrumentation {
    private var screenNameExtractor = ScreenNameExtractor.DEFAULT
    private var tracerCustomizer: (Tracer) -> Tracer = { it }

    fun setTracerCustomizer(customizer: (Tracer) -> Tracer) {
        tracerCustomizer = customizer
    }

    fun setScreenNameExtractor(screenNameExtractor: ScreenNameExtractor) {
        this.screenNameExtractor = screenNameExtractor
    }

    override fun install(
        application: Application,
        openTelemetry: OpenTelemetry,
    ) {
        application.registerActivityLifecycleCallbacks(buildFragmentRegisterer(openTelemetry))
    }

    private fun buildFragmentRegisterer(openTelemetry: OpenTelemetry): ActivityLifecycleCallbacks {
        val visibleScreenService = ServiceManager.get().getVisibleScreenService()
        val delegateTracer: Tracer = openTelemetry.getTracer(INSTRUMENTATION_SCOPE)
        val fragmentLifecycle =
            RumFragmentLifecycleCallbacks(
                tracerCustomizer.invoke(delegateTracer),
                visibleScreenService::previouslyVisibleScreen,
                screenNameExtractor,
            )
        return if (Build.VERSION.SDK_INT < 29) {
            RumFragmentActivityRegisterer.createPre29(fragmentLifecycle)
        } else {
            RumFragmentActivityRegisterer.create(fragmentLifecycle)
        }
    }
}
