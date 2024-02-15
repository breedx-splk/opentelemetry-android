/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.android.instrumentation.startup;

import java.util.Map;

public interface InitializationEvents {

    void sdkInitializationStarted();

    //TODO: This uses map to not couple directly to config objects.
    void recordConfiguration(Map<String,String> config);

    void currentNetworkProviderInitialized();

    void networkMonitorInitialized();

    void anrMonitorInitialized();

    void slowRenderingDetectorInitialized();

    void crashReportingInitialized();

    InitializationEvents NO_OP =
            new InitializationEvents() {
                @Override
                public void sdkInitializationStarted() {}

                @Override
                public void recordConfiguration(Map<String,String> config) {}

                @Override
                public void currentNetworkProviderInitialized() {}

                @Override
                public void networkMonitorInitialized() {}

                @Override
                public void anrMonitorInitialized() {}

                @Override
                public void slowRenderingDetectorInitialized() {}

                @Override
                public void crashReportingInitialized() {}
            };
}
