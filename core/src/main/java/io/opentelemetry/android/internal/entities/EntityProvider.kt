package io.opentelemetry.android.internal.entities

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.resources.Resource
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class EntityProvider(initialEntities: List<Entity> = emptyList()) {

    private val listeners: MutableList<EntityListener> = mutableListOf()
    private var resource: AtomicReference<Resource> = AtomicReference(Resource.empty())
    private val lock: Lock = ReentrantLock()
    private val listenerLock: Lock = ReentrantLock()

    private val entities: MutableList<Entity> = mutableListOf()
    private val idToIndex: MutableMap<String, Int> = hashMapOf()

    init {
        initialEntities.forEach { addEntity(it.id, it.name, it.attributes) }
    }

    fun addListener(listener: EntityListener) {
        listenerLock.withLock { listeners.add(listener) }
    }

    fun getResource(): Resource {
        return resource.get()
    }

    fun addEntity(id: String, name: String, attr: Attributes) {
        lock.withLock {
            val index = idToIndex.remove(id)
            if (index != null) {
                entities.removeAt(index)
            }
            val entity = Entity(id, name, attr)
            entities.add(entity)
            idToIndex[id] = entities.size - 1

            updateResourceAndNotifyListeners { listener, newResource ->
                listener.onEntityState(entity, newResource)
            }
        }
    }

    fun updateEntity(id: String, newAttributes: Attributes) {
        lock.withLock {
            val index = idToIndex[id] ?: return
            val updatedEntity = entities[index].withAttributes(newAttributes)
            entities[index] = updatedEntity
            updateResourceAndNotifyListeners { listener, newResource ->
                listener.onEntityState(updatedEntity, newResource)
            }
        }
    }

    fun deleteEntity(id: String) {
        lock.withLock {
            val index = idToIndex.remove(id) ?: return
            val entity = entities.removeAt(index)
            // Need to reindex everything after index
            for (i in index until entities.size) {
                idToIndex[entities[i].id] = i
            }
            updateResourceAndNotifyListeners { listener, newResource ->
                listener.onEntityDelete(entity, newResource)
            }
        }
    }

    private fun updateResourceAndNotifyListeners(fn: (EntityListener, Resource) -> Unit) {
        val newResource = rebuildResource()
        listenerLock.withLock {
            listeners.forEach { fn.invoke(it, newResource) }
        }
    }

    private fun rebuildResource(): Resource {
        val newResource =
            when (entities.isEmpty()) {
                true -> Resource.empty()
                else -> {
                    val builder = Resource.builder()
                    entities.forEach {
                        builder.putAll(it.attributes)
                    }
                    builder.build()
                }
            }
        resource.set(newResource)
        return newResource
    }
}