package io.opentelemetry.android.internal.entities

import io.mockk.Runs
import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

class EntityProviderTest {

    val k1 = stringKey("a1.k")
    val k2 = stringKey("a2.k")
    val a1 = Attributes.of(k1, "boop")
    val a2 = Attributes.of(k2, "bleep")
    val e1 = Entity("foo", "fooname", a1)
    val e2 = Entity("bar", "barname", a2)

    @Test
    fun `empty list gives constructor gives resource`() {
        val res = EntityProvider().getResource()
        assertThat(res).isSameAs(Resource.empty())
    }

    @Test
    fun `constructor gives resource`() {
        val entities = listOf(e1, e2)
        val res = EntityProvider(entities).getResource()
        assertThat(res.attributes).containsOnly(
            entry<AttributeKey<String>, String>(k1, "boop"),
            entry<AttributeKey<String>, String>(k2, "bleep")
        )
    }

    @Test
    fun `construct with id collisions`() {
        val e1 = Entity("x", "fooname", a1)
        val e2 = Entity("x", "barname", a2)
        val entities = listOf(e1, e2)
        val res = EntityProvider(entities).getResource()
        assertThat(res.attributes).containsOnly(
            entry<AttributeKey<String>, String>(k2, "bleep")
        )
    }

    @Test
    fun `add new entity`() {
        val newAttr = Attributes.of(stringKey("new key"), "newval")
        val listener = mockk<EntityListener>()
        val entitySlot = slot<Entity>()
        val resourceSlot = slot<Resource>()
        every {
            listener.onEntityState(capture(entitySlot), capture(resourceSlot))
        } just Runs

        val entityProvider = EntityProvider(listOf(e1))
        entityProvider.addListener(listener)

        entityProvider.addEntity("new id", "new name", newAttr)

        assertThat(entitySlot.captured.id).isEqualTo("new id")
        assertThat(entitySlot.captured.name).isEqualTo("new name")
        assertThat(entityProvider.getResource().attributes).containsOnly(
            entry<AttributeKey<String>, String>(k1, "boop"),
            entry<AttributeKey<String>, String>(stringKey("new key"), "newval")
        )
    }

    @Test
    fun `add with existing id overrides entity`() {
        val newAttr = Attributes.of(stringKey("jibro"), "newval")

        val listener = mockk<EntityListener>(relaxed = true)
        val entitySlot = slot<Entity>()
        val resourceSlot = slot<Resource>()
        every {
            listener.onEntityState(capture(entitySlot), capture(resourceSlot))
        } just Runs

        val entityProvider = EntityProvider(listOf(e1))
        entityProvider.addListener(listener)

        entityProvider.addEntity(e1.id, "new name", newAttr)

        assertThat(entitySlot.captured.id).isEqualTo(e1.id)
        assertThat(entitySlot.captured.name).isEqualTo("new name")
        assertThat(resourceSlot.captured).isSameAs(entityProvider.getResource())
        assertThat(entityProvider.getResource().attributes).containsOnly(
            entry<AttributeKey<String>, String>(stringKey("jibro"), "newval")
        )
    }

    @Test
    fun `update not found no listeners`() {
        val listener = mockk<EntityListener>()

        val entityProvider = EntityProvider(listOf(e1))

        entityProvider.addListener(listener)

        val resource = entityProvider.getResource()
        entityProvider.updateEntity("notfound", Attributes.empty())
        verify { listener wasNot called }
        assertThat(entityProvider.getResource()).isSameAs(resource)
    }

    @Test
    fun `update entity`() {
        val newKey = stringKey("jibro")
        val newAttr = Attributes.of(newKey, "newval")
        val listener = mockk<EntityListener>(relaxed = true)

        val entityProvider = EntityProvider(listOf(e1))

        entityProvider.addListener(listener)

        val entitySlot = slot<Entity>()
        val resourceSlot = slot<Resource>()

        every {
            listener.onEntityState(capture(entitySlot), capture(resourceSlot))
        } just runs

        entityProvider.updateEntity(e1.id, newAttr)

        assertThat(entitySlot.captured.id).isEqualTo(e1.id)
        assertThat(entitySlot.captured.name).isEqualTo(e1.name)
        assertThat(entitySlot.captured.attributes).isEqualTo(newAttr)
        assertThat(entityProvider.getResource()).isSameAs(resourceSlot.captured)
        assertThat(resourceSlot.captured.attributes).containsOnly(
            entry<AttributeKey<String>, String>(newKey, "newval")
        )
    }

    @Test
    fun `delete entity`() {
        val listener = mockk<EntityListener>(relaxed = true)

        val entityProvider = EntityProvider(listOf(e1, e2))

        entityProvider.addListener(listener)

        val entitySlot = slot<Entity>()
        val resourceSlot = slot<Resource>()

        every {
            listener.onEntityDelete(capture(entitySlot), capture(resourceSlot))
        } just runs

        entityProvider.deleteEntity(e1.id)

        assertThat(entitySlot.captured.id).isEqualTo(e1.id)
        assertThat(entitySlot.captured.name).isEqualTo(e1.name)
        assertThat(entityProvider.getResource()).isSameAs(resourceSlot.captured)
        assertThat(resourceSlot.captured.attributes).containsOnly(
            entry<AttributeKey<String>, String>(k2, "bleep")
        )
    }

    @Test
    fun `delete entity not found`() {
        val listener = mockk<EntityListener>(relaxed = true)

        val entityProvider = EntityProvider(listOf(e1, e2))

        val resource = entityProvider.getResource()
        entityProvider.addListener(listener)
        entityProvider.deleteEntity("NO WAY")
        verify { listener wasNot called }
        assertThat(entityProvider.getResource()).isSameAs(resource)
    }

    @Test
    fun `delete last returns empty resource`() {
        val entityProvider = EntityProvider(listOf(e1, e2))
        assertThat(entityProvider.getResource().attributes.size()).isPositive()
        entityProvider.deleteEntity(e1.id)
        entityProvider.deleteEntity(e2.id)
        assertThat(entityProvider.getResource().attributes.size()).isZero()
        assertThat(entityProvider.getResource()).isSameAs(Resource.empty())
    }
}