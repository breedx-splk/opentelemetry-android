# Android Instrumentation for OkHttp version 3.0 and higher

## Status: development

Provides OpenTelemetry instrumentation for [okhttp3](https://square.github.io/okhttp/).

## Quickstart

### Add these dependencies to your project

Replace `BYTEBUDDY_VERSION` with the [latest
release](https://central.sonatype.com/artifact/net.bytebuddy/byte-buddy-gradle-plugin/versions).

#### Byte buddy compilation plugin

This plugin leverages
Android's [Transform API](https://developer.android.com/reference/tools/gradle-api/current/com/android/build/api/variant/ScopedArtifactsOperation#toTransform(com.android.build.api.artifact.ScopedArtifact,kotlin.Function1,kotlin.Function1,kotlin.Function1))
to instrument bytecode at compile time. You can find more info on
its [repo page](https://github.com/raphw/byte-buddy/tree/master/byte-buddy-gradle-plugin/android-plugin).

```groovy
plugins {
    id 'net.bytebuddy.byte-buddy-gradle-plugin' version 'BYTEBUDDY_VERSION'
}
```

#### Project dependencies

```kotlin
implementation("io.opentelemetry.android.instrumentation:okhttp3-library:0.15.0-alpha")
byteBuddy("io.opentelemetry.android.instrumentation:okhttp3-agent:0.15.0-alpha")
```

After adding the plugin and the dependencies to your project, your OkHttp requests will be traced
automatically.

### Configuration

You can configure the automatic instrumentation by using the setters
from
the [OkHttpInstrumentation](library/src/main/java/io/opentelemetry/instrumentation/library/okhttp/v3_0/OkHttpInstrumentation.kt)
instance provided via the AndroidInstrumentationLoader as shown below:

```java
OkHttpInstrumentation instrumentation = AndroidInstrumentationLoader.getInstrumentation(OkHttpInstrumentation.class);

// Call `instrumentation` setters.
```

> [!NOTE]
> You must make sure to apply any configurations **before** initializing your OpenTelemetryRum
> instance (i.e. calling OpenTelemetryRum.builder()...build()). Otherwise your configs won't be
> taken into account during the RUM initialization process.
