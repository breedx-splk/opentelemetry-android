package io.opentelemetry.android.internal.entities

import io.opentelemetry.api.common.Attributes

data class Entity(
    val id: String,
    val name: String,
    val attributes: Attributes) {

    fun withAttributes(newAttributes: Attributes): Entity {
        return Entity(id, name, newAttributes)
    }
}