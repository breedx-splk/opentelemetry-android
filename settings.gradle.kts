rootProject.name = "opentelemetry-android"

include(":common")
include(":android-agent")
include(":instrumentation:activity")
include(":instrumentation:anr")
include(":instrumentation:common-api")
include(":instrumentation:crash")
include(":instrumentation:fragment")
include(":instrumentation:lifecycle")
include(":instrumentation:okhttp:okhttp-3.0:agent")
include(":instrumentation:okhttp:okhttp-3.0:library")
include(":instrumentation:okhttp:okhttp-3.0:testing")
include(":instrumentation:network")
include(":instrumentation:slowrendering")
include(":instrumentation:startup")
include(":instrumentation:volley:library")
includeBuild("demo-app") {
    dependencySubstitution {
        // I don't think this works yet, creates a circular dep or something
        substitute(module("io.opentelemetry.android:android-agent")).using(project(":"))
    }
}