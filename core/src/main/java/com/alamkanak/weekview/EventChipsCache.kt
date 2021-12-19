package com.alamkanak.weekview

import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal typealias EventChipsCacheProvider = () -> EventChipsCache?

internal class EventChipsCache {

    val allEventChips: List<EventChip>
        get() = chipsByDate.values.flatten()

    private val chipsByDate = ConcurrentHashMap<Long, CopyOnWriteArrayList<EventChip>>()

    fun allEventChipsInDateRange(
        dateRange: List<Calendar>,
    ): List<EventChip> {
        val results = mutableListOf<EventChip>()
        for (date in dateRange) {
            results += chipsByDate[date.atStartOfDay.timeInMillis].orEmpty()
        }
        return results
    }

    fun normalEventChipsByDate(
        date: Calendar,
    ): List<EventChip> = chipsByDate[date.atStartOfDay.timeInMillis].orEmpty().filter { it.item.isNotAllDay }

    fun allDayEventChipsByDate(
        date: Calendar,
    ): List<EventChip> = chipsByDate[date.atStartOfDay.timeInMillis].orEmpty().filter { it.item.isAllDay }

    fun normalEventChipsInDateRange(
        dateRange: List<Calendar>,
    ): List<EventChip> {
        val results = mutableListOf<EventChip>()
        for (date in dateRange) {
            results += normalEventChipsByDate(date)
        }
        return results
    }

    fun allDayEventChipsInDateRange(
        dateRange: List<Calendar>,
    ): List<EventChip> {
        val results = mutableListOf<EventChip>()
        for (date in dateRange) {
            results += allDayEventChipsByDate(date)
        }
        return results
    }

    fun replaceAll(eventChips: List<EventChip>) {
        clear()
        addAll(eventChips)
    }

    fun addAll(eventChips: List<EventChip>) {
        for (eventChip in eventChips) {
            val isExistingChip = allEventChips.any { it.itemId == eventChip.itemId }
            if (isExistingChip) {
                remove(eventId = eventChip.itemId)
            }
        }

        for (eventChip in eventChips) {
            val key = eventChip.startTime.atStartOfDay.timeInMillis
            chipsByDate.add(key, eventChip)
        }
    }

    fun findHitEvent(x: Float, y: Float): EventChip? {
        val candidates = allEventChips.filter { it.isHit(x, y) }
        return when {
            candidates.isEmpty() -> null
            // Two events hit. This is most likely because an all-day event was clicked, but a
            // single event is rendered underneath it. We return the all-day event.
            candidates.size == 2 -> candidates.first { it.item.isAllDay }
            else -> candidates.first()
        }
    }

    fun remove(eventId: Long) {
        val eventChip = allEventChips.firstOrNull { it.itemId == eventId } ?: return
        remove(eventChip)
    }

    fun removeAll(events: List<WeekViewItem>) {
        val eventIds = events.map { it.id }
        val eventChips = allEventChips.filter { it.item.id in eventIds }
        eventChips.forEach(this::remove)
    }

    private fun remove(eventChip: EventChip) {
        val key = eventChip.startTime.atStartOfDay.timeInMillis
        val itemId = eventChip.itemId
        chipsByDate[key]?.removeAll { it.itemId == itemId }
    }

    fun clearSingleEventsCache() {
        allEventChips.filter { it.item.isNotAllDay }.forEach(EventChip::setEmpty)
    }

    fun clear() {
        chipsByDate.clear()
    }
}

private fun ConcurrentHashMap<Long, CopyOnWriteArrayList<EventChip>>.add(
    key: Long,
    eventChip: EventChip,
) {
    val results = getOrElse(key) { CopyOnWriteArrayList() }
    results.add(eventChip)
    this[key] = results
}
