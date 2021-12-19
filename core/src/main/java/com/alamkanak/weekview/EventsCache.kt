package com.alamkanak.weekview

import androidx.collection.ArrayMap

internal typealias EventsCacheProvider = () -> EventsCache?

/**
 * An abstract class that provides functionality to cache [WeekViewItem] elements.
 */
internal abstract class EventsCache {

    abstract val allEvents: List<WeekViewItem>

    abstract fun update(events: List<WeekViewItem>)

    abstract fun update(event: WeekViewItem)

    abstract fun clear()

    operator fun get(id: Long): WeekViewItem? = allEvents.firstOrNull { it.id == id }
}

/**
 * Represents an [EventsCache] that relies on a simple list of [WeekViewItem] objects. When updated
 * with new [WeekViewItem] objects, all existing ones are replaced.
 */
internal class SimpleEventsCache : EventsCache() {

    override val allEvents = mutableListOf<WeekViewItem>()

    override fun update(events: List<WeekViewItem>) {
        allEvents.clear()
        allEvents.addAll(events)
    }

    override fun update(event: WeekViewItem) {
        val index = allEvents.indexOfFirst { it.id == event.id }

        if (index != -1) {
            allEvents.removeAt(index)
            allEvents.add(index, event)
        }
    }

    override fun clear() {
        allEvents.clear()
    }
}

/**
 * Represents an [EventsCache] that caches [WeekViewItem]s for their respective [Period] and allows
 * retrieval based on that [Period].
 */
internal class PaginatedEventsCache : EventsCache() {

    private val eventsByPeriod: ArrayMap<Period, MutableList<WeekViewItem>> = ArrayMap()

    override val allEvents: List<WeekViewItem>
        get() = eventsByPeriod.values.flatten()

    override fun update(events: List<WeekViewItem>) {
        val groupedEvents = events.groupBy { it.period }
        for ((period, periodEvents) in groupedEvents) {
            eventsByPeriod[period] = periodEvents.toMutableList()
        }
    }

    override fun update(event: WeekViewItem) {
        val existingEvent = allEvents.firstOrNull { it.id == event.id } ?: return
        eventsByPeriod[existingEvent.period]?.removeAll { it.id == event.id }
        eventsByPeriod[event.period]?.add(event)
    }

    internal fun determinePeriodsToFetch(range: FetchRange) = range.periods.filter { it !in this }

    operator fun contains(period: Period) = eventsByPeriod.contains(period)

    operator fun contains(range: FetchRange) = eventsByPeriod.containsAll(range.periods)

    fun reserve(period: Period) {
        eventsByPeriod[period] = mutableListOf()
    }

    override fun clear() {
        eventsByPeriod.clear()
    }
}
