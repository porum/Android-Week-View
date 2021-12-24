package com.alamkanak.weekview

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A helper class that processes the submitted [WeekViewEntity] objects and creates [EventChip]s
 * on a background thread.
 */
internal class EventsProcessor(
    private val context: Context,
    private val eventsCache: EventsCache,
    private val eventChipsFactory: EventChipsFactory,
    private val eventChipsCache: EventChipsCache,
    private val backgroundExecutor: Executor = Executors.newSingleThreadExecutor(),
    private val mainThreadExecutor: Executor = ContextCompat.getMainExecutor(context),
) {

    /**
     * Updates the [EventsCache] with the provided [WeekViewItem] object and creates [EventChip]s.
     *
     * @param items The list of new [WeekViewItem] objects
     * @param viewState The current [ViewState] of [WeekView]
     * @param onFinished Callback to inform the caller whether [WeekView] should invalidate.
     */
    fun submit(
        items: List<WeekViewItem>,
        viewState: ViewState,
        onFinished: () -> Unit
    ) {
        backgroundExecutor.execute {
            submitItems(items, viewState)
            mainThreadExecutor.execute {
                onFinished()
            }
        }
    }

    /**
     * Updates the [EventsCache] with the provided [WeekViewItem] object and creates [EventChip]s.
     *
     * @param items The list of new [WeekViewItem] objects
     * @param viewState The current [ViewState] of [WeekView]
     * @param onFinished Callback to inform the caller whether [WeekView] should invalidate.
     */
    @Deprecated(
        message = "Remove once WeekViewEntity is fully deprecated.",
    )
    fun submitEntities(
        items: List<WeekViewEntity>,
        viewState: ViewState,
        onFinished: () -> Unit,
    ) {
        backgroundExecutor.execute {
            submitEntities(items, viewState)
            mainThreadExecutor.execute {
                onFinished()
            }
        }
    }

    internal fun updateDraggedItem(
        updatedItem: WeekViewItem,
        viewState: ViewState,
    ) {
        eventsCache.update(updatedItem)

        val newEventChips = eventChipsFactory.create(listOf(updatedItem), viewState)
        eventChipsCache.remove(eventId = updatedItem.id)
        eventChipsCache.addAll(newEventChips)
    }

    @WorkerThread
    private fun submitItems(
        items: List<WeekViewItem>,
        viewState: ViewState,
    ) {
        val processedItems = processItems(items)
        eventsCache.update(processedItems)

        if (eventsCache is SimpleEventsCache) {
            submitEntitiesToSimpleCache(processedItems, viewState)
        } else {
            submitEntitiesToPagedCache(processedItems, viewState)
        }
    }

    @WorkerThread
    private fun submitEntities(
        entities: List<WeekViewEntity>,
        viewState: ViewState,
    ) {
        val processedItems = entities
            .map { it.resolve(context).toWeekViewItem() }

        eventsCache.update(processedItems)

        if (eventsCache is SimpleEventsCache) {
            submitEntitiesToSimpleCache(processedItems, viewState)
        } else {
            submitEntitiesToPagedCache(processedItems, viewState)
        }
    }

    private fun processItems(items: List<WeekViewItem>): List<WeekViewItem> {
        return items.map { item ->
            item.copy(
                title = item.title.processed,
                subtitle = item.subtitle?.processed,
                duration = when (val duration = item.duration) {
                    is WeekViewItem.Duration.AllDay -> duration // Keep all-day events as-is
                    is WeekViewItem.Duration.Bounded -> WeekViewItem.Duration.Bounded(
                        startTime = duration.startTime.withLocalTimeZone(),
                        endTime = duration.endTime.withLocalTimeZone(),
                    )
                }
            )
        }
    }

    private fun submitEntitiesToSimpleCache(
        items: List<WeekViewItem>,
        viewState: ViewState,
    ) {
        val eventChips = eventChipsFactory.create(items, viewState)
        eventChipsCache.replaceAll(eventChips)
    }

    private fun submitEntitiesToPagedCache(
        items: List<WeekViewItem>,
        viewState: ViewState,
    ) {
        val diffResult = performDiff(items)
        eventChipsCache.removeAll(diffResult.itemsToRemove)

        val eventChips = eventChipsFactory.create(diffResult.itemsToAddOrUpdate, viewState)
        eventChipsCache.addAll(eventChips)
    }

    private fun performDiff(items: List<WeekViewItem>): DiffResult {
        val existingItems = eventChipsCache.allEventChips.map { it.item }
        return DiffResult.calculateDiff(
            existingEntities = existingItems,
            newEntities = items,
        )
    }

    data class DiffResult(
        val itemsToAddOrUpdate: List<WeekViewItem>,
        val itemsToRemove: List<WeekViewItem>,
    ) {
        companion object {
            fun calculateDiff(
                existingEntities: List<WeekViewItem>,
                newEntities: List<WeekViewItem>,
            ): DiffResult {
                val existingEntityIds = existingEntities.map { it.id }

                val submittedEntityIds = newEntities.map { it.id }
                val addedEvents = newEntities.filter { it.id !in existingEntityIds }
                val deletedEvents = existingEntities.filter { it.id !in submittedEntityIds }

                val updatedEvents = newEntities.filter { it.id in existingEntityIds }
                val changed = updatedEvents.filter { it !in existingEntities }

                return DiffResult(
                    itemsToAddOrUpdate = addedEvents + changed,
                    itemsToRemove = deletedEvents,
                )
            }
        }
    }
}
