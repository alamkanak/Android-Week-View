package com.alamkanak.weekview

import com.alamkanak.weekview.EventsProcessor.DiffResult
import com.alamkanak.weekview.util.MockFactory
import com.alamkanak.weekview.util.withDifferentId
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class DiffResultTest {

    @Test
    fun `DiffResult for empty existing and new entities contains no elements`() {
        val existingEntities = emptyList<ResolvedWeekViewEntity>()
        val newEntities = emptyList<ResolvedWeekViewEntity>()

        val result = DiffResult.calculateDiff(
            existingEntities = existingEntities,
            newEntities = newEntities,
        )

        assertThat(result.itemsToAddOrUpdate).isEmpty()
        assertThat(result.itemsToRemove).isEmpty()
    }

    @Test
    fun `New entities are correctly recognized as new`() {
        val existingEntities = emptyList<ResolvedWeekViewEntity>()
        val newEntities = MockFactory.resolvedWeekViewEntities(count = 2)

        val result = DiffResult.calculateDiff(
            existingEntities = existingEntities,
            newEntities = newEntities,
        )

        assertThat(result.itemsToAddOrUpdate).containsExactlyElementsIn(newEntities)
        assertThat(result.itemsToRemove).isEmpty()
    }

    @Test
    fun `Updated entities are correctly recognized as new`() {
        val existingEntity = MockFactory.resolvedWeekViewEntity()
        val newEntity = existingEntity.createCopy(
            endTime = existingEntity.startTime + Hours(2)
        )

        val result = DiffResult.calculateDiff(
            existingEntities = listOf(existingEntity),
            newEntities = listOf(newEntity),
        )

        assertThat(result.itemsToAddOrUpdate).containsExactly(newEntity)
        assertThat(result.itemsToRemove).isEmpty()
    }

    @Test
    fun `New and updated entities are correctly recognized together`() {
        val existingEntity = MockFactory.resolvedWeekViewEntity()
        val updatedEntity = existingEntity.createCopy(
            endTime = existingEntity.startTime + Hours(2)
        )
        val newEntity = MockFactory.resolvedWeekViewEntity()

        val result = DiffResult.calculateDiff(
            existingEntities = listOf(existingEntity),
            newEntities = listOf(updatedEntity, newEntity),
        )

        assertThat(result.itemsToAddOrUpdate).containsExactly(newEntity, updatedEntity)
        assertThat(result.itemsToRemove).isEmpty()
    }

    @Test
    fun `Removed entities are correctly recognized as to-remove`() {
        val entityToRemove = MockFactory.resolvedWeekViewEntity()

        val result = DiffResult.calculateDiff(
            existingEntities = listOf(entityToRemove),
            newEntities = emptyList(),
        )

        assertThat(result.itemsToAddOrUpdate).isEmpty()
        assertThat(result.itemsToRemove).containsExactly(entityToRemove)
    }

    @Test
    fun `Otherwise equal entities with different IDs are treated as separate elements`() {
        val existingEntity = MockFactory.resolvedWeekViewEntity()
        val newEntity = existingEntity.withDifferentId()

        val result = DiffResult.calculateDiff(
            existingEntities = listOf(existingEntity),
            newEntities = listOf(newEntity),
        )

        assertThat(result.itemsToAddOrUpdate).containsExactly(newEntity)
        assertThat(result.itemsToRemove).containsExactly(existingEntity)
    }
}
