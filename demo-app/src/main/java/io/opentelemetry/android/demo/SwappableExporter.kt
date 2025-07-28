package io.opentelemetry.android.demo

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.atomic.AtomicReference

class SwappableExporter(val delegate: SpanExporter) : SpanExporter {

    private val holder: AtomicReference<SpanExporter> = AtomicReference(delegate)

    fun set(newDelegate: SpanExporter){
        holder.get().flush().whenComplete { shutdown() }
        holder.set(newDelegate)
    }

    override fun export(spans: Collection<SpanData?>): CompletableResultCode? {
        return holder.get().export(spans)
    }

    override fun flush(): CompletableResultCode? {
        return holder.get().flush()
    }

    override fun shutdown(): CompletableResultCode? {
        return holder.get().shutdown()
    }

}