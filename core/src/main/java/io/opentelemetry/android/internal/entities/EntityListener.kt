package io.opentelemetry.android.internal.entities

import io.opentelemetry.sdk.resources.Resource

interface EntityListener {

    fun onEntityState(state: Entity, resource: Resource)
    fun onEntityDelete(state: Entity, resource: Resource)
}