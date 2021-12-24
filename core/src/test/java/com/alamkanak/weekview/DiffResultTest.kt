package com.alamkanak.weekview

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alamkanak.weekview.EventsProcessor.DiffResult
import com.alamkanak.weekview.util.Mocks
import com.alamkanak.weekview.util.withDifferentId
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class DiffResultTest {

    @Test
    fun `DiffResult for empty existing and new entities contains no elements`() {
        val existingEntities = emptyList<WeekViewItem>()
        val newEntities = emptyList<WeekViewItem>()

        val result = DiffResult.calculateDiff(
            existingEntities = existingEntities,
            newEntities = newEntities,
        )

        assertThat(result.itemsToAddOrUpdate).isEmpty()
        assertThat(result.itemsToRemove).isEmpty()
    }

    @Test
    fun `New entities are correctly recognized as new`() {
        val existingEntities = emptyList<WeekViewItem>()
        val newEntities = Mocks.weekViewItems(count = 2)

        val result = DiffResult.calculateDiff(
            existingEntities = existingEntities,
            newEntities = newEntities,
        )

        assertThat(result.itemsToAddOrUpdate).containsExactlyElementsIn(newEntities)
        assertThat(result.itemsToRemove).isEmpty()
    }

    @Test
    fun `Updated entities are correctly recognized as new`() {
        val existingEntity = Mocks.weekViewItem()
        val newEntity = existingEntity.copyWith(
            startTime = existingEntity.duration.startTime,
            endTime = existingEntity.duration.endTime + Hours(1)
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
        val startTime = Calendar.getInstance()
        val endTime = startTime + Hours(1)

        val existingEntity = Mocks.weekViewItem(startTime, endTime)
        val updatedEntity = existingEntity.copyWith(
            startTime = existingEntity.duration.startTime,
            endTime = existingEntity.duration.endTime + Hours(1),
        )
        val newEntity = Mocks.weekViewItem(startTime, endTime)

        val result = DiffResult.calculateDiff(
            existingEntities = listOf(existingEntity),
            newEntities = listOf(updatedEntity, newEntity),
        )

        assertThat(result.itemsToAddOrUpdate).containsExactly(newEntity, updatedEntity)
        assertThat(result.itemsToRemove).isEmpty()
    }

    @Test
    fun `Removed entities are correctly recognized as to-remove`() {
        val entityToRemove = Mocks.weekViewItem()

        val result = DiffResult.calculateDiff(
            existingEntities = listOf(entityToRemove),
            newEntities = emptyList(),
        )

        assertThat(result.itemsToAddOrUpdate).isEmpty()
        assertThat(result.itemsToRemove).containsExactly(entityToRemove)
    }

    @Test
    fun `Otherwise equal entities with different IDs are treated as separate elements`() {
        val existingEntity = Mocks.weekViewItem()
        val newEntity = existingEntity.withDifferentId()

        val result = DiffResult.calculateDiff(
            existingEntities = listOf(existingEntity),
            newEntities = listOf(newEntity),
        )

        assertThat(result.itemsToAddOrUpdate).containsExactly(newEntity)
        assertThat(result.itemsToRemove).containsExactly(existingEntity)
    }
}
