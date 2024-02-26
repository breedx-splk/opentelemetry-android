/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.android;

import static io.opentelemetry.android.common.RumConstants.PREVIOUS_SESSION_ID_KEY;

import io.opentelemetry.api.trace.Tracer;

final class SessionIdChangeTracer implements SessionIdChangeListener {

    private final Tracer tracer;

    SessionIdChangeTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void onChange(String oldSessionId, String newSessionId) {
        tracer.spanBuilder("sessionId.change")
                .setAttribute(PREVIOUS_SESSION_ID_KEY, oldSessionId)
                .startSpan()
                .end();
    }
}
