/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.agent.okhttp.v3_0;

import static io.opentelemetry.instrumentation.library.okhttp.v3_0.internal.OkHttp3Singletons.CALLBACK_CONTEXT_INTERCEPTOR;
import static io.opentelemetry.instrumentation.library.okhttp.v3_0.internal.OkHttp3Singletons.CONNECTION_ERROR_INTERCEPTOR;
import static io.opentelemetry.instrumentation.library.okhttp.v3_0.internal.OkHttp3Singletons.RESEND_COUNT_CONTEXT_INTERCEPTOR;
import static io.opentelemetry.instrumentation.library.okhttp.v3_0.internal.OkHttp3Singletons.TRACING_INTERCEPTOR;

import io.opentelemetry.instrumentation.library.okhttp.v3_0.internal.OkHttp3Singletons;
import net.bytebuddy.asm.Advice;
import okhttp3.OkHttpClient;

public class OkHttpClientAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(0) OkHttpClient.Builder builder) {
        if (!builder.interceptors().contains(CALLBACK_CONTEXT_INTERCEPTOR)) {
            builder.interceptors().add(0, CALLBACK_CONTEXT_INTERCEPTOR);
            builder.interceptors().add(1, RESEND_COUNT_CONTEXT_INTERCEPTOR);
            if(CONNECTION_ERROR_INTERCEPTOR != null) {
                builder.interceptors().add(2, CONNECTION_ERROR_INTERCEPTOR);
            }
        }
        if (TRACING_INTERCEPTOR != null && !builder.networkInterceptors().contains(TRACING_INTERCEPTOR)) {
            builder.addNetworkInterceptor(TRACING_INTERCEPTOR);
        }
    }
}
