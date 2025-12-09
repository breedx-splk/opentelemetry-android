/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.android.demo

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.OpenTelemetryRumBuilder
import io.opentelemetry.android.RumBuilder
import io.opentelemetry.android.config.OtelRumConfig
import io.opentelemetry.android.features.diskbuffering.DiskBufferingConfig
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.logs.LoggerProvider
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter

const val TAG = "otel.demo"

class OtelDemoApplication : Application() {

//    @OptIn(Incubating::class)
    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Initializing the opentelemetry-android-agent")

        try {
            val diskBufferingConfig = DiskBufferingConfig(maxCacheSize = 10_000_000, enabled = true)
            val config =
                OtelRumConfig()
                    .setGlobalAttributes(
                        Attributes.builder()
                            .put(stringKey("app.version.name"), "foo")
                            .put(stringKey("app.version.code"), "1.2.3")
                            .build()
                    )
                    .setDiskBufferingConfig(diskBufferingConfig)

            // 10.0.2.2 is a special binding to the host running the emulator
            val baseUrl = "http://10.0.2.2:4318"

            val spansIngestUrl = "${baseUrl}/v1/traces"
            val logsIngestUrl = "${baseUrl}/v1/logs"

            val otelRumBuilder: OpenTelemetryRumBuilder =
                RumBuilder.builder(this, config)
                    .addSpanExporterCustomizer {
                        OtlpHttpSpanExporter.builder()
                            .setEndpoint(spansIngestUrl)
                            .build()
                    }
                    .addLogRecordExporterCustomizer {
                        OtlpHttpLogRecordExporter.builder()
                            .setEndpoint(logsIngestUrl)
                            .build()
                    }

            rum = otelRumBuilder.build()
            Log.d(TAG, "RUM session started: " + rum?.getRumSessionId())
        } catch (e: Exception) {
            Log.e(TAG, "Oh no!", e)
        }
    }

    companion object {
        var rum: OpenTelemetryRum? = null

        fun tracer(name: String): Tracer? {
            return rum?.openTelemetry?.tracerProvider?.get(name)
        }

        fun counter(name: String): LongCounter? {
            return rum?.openTelemetry?.meterProvider?.get("demo.app")?.counterBuilder(name)
                ?.build()
        }

        fun eventBuilder(scopeName: String, eventName: String): LogRecordBuilder {
            if (rum == null) {
                return LoggerProvider.noop().get("noop").logRecordBuilder()
            }
            val logger = rum!!.openTelemetry.logsBridge.loggerBuilder(scopeName).build()
            return logger.logRecordBuilder().setEventName(eventName)
        }
    }
}
