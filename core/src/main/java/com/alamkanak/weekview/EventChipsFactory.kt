package com.alamkanak.weekview

import java.util.Calendar

internal class EventChipsFactory {

    fun create(
        items: List<WeekViewItem>,
        viewState: ViewState,
    ): List<EventChip> {
        val (backgroundItems, foregroundItems) = items.partition {
            it.configuration.arrangement == WeekViewItem.Arrangement.Background
        }

        val backgroundChips = createInternal(backgroundItems, viewState)
        val foregroundChips = createInternal(foregroundItems, viewState)

        return backgroundChips + foregroundChips
    }

    private fun createInternal(
        items: List<WeekViewItem>,
        viewState: ViewState,
    ): List<EventChip> {
        val eventChips = convertEventsToEventChips(items, viewState)
        val groups = eventChips.groupedByDate().values

        for (group in groups) {
            computePositionOfEvents(group, viewState)
        }

        return eventChips
    }

    private fun convertEventsToEventChips(
        items: List<WeekViewItem>,
        viewState: ViewState
    ): List<EventChip> {
        return items.sanitize(viewState).toEventChips(viewState)
    }

    private fun List<WeekViewItem>.sanitize(viewState: ViewState): List<WeekViewItem> {
        return map { it.sanitize(viewState) }
    }

    private fun List<WeekViewItem>.toEventChips(viewState: ViewState): List<EventChip> {
        return flatMap { item ->
            val eventParts = item.split(viewState)
            eventParts.mapIndexed { index, eventPart ->
                EventChip(
                    item = item,
                    index = index,
                    startTime = eventPart.timing.startTime,
                    endTime = eventPart.timing.endTime,
                )
            }
        }
    }

    /**
     * Forms [CollisionGroup]s for all event chips and uses them to expand the [EventChip]s to their
     * maximum width.
     *
     * @param eventChips A list of [EventChip]s
     */
    private fun computePositionOfEvents(eventChips: List<EventChip>, viewState: ViewState) {
        val singleEventChips = eventChips.filter { it.item.isNotAllDay }
        val allDayEventChips = eventChips.filter { it.item.isAllDay }

        val singleEventGroups = singleEventChips.toMultiColumnCollisionGroups()
        val allDayGroups = if (viewState.arrangeAllDayEventsVertically) {
            allDayEventChips.toSingleColumnCollisionGroups()
        } else {
            allDayEventChips.toMultiColumnCollisionGroups()
        }

        for (collisionGroup in singleEventGroups) {
            expandEventsToMaxWidth(collisionGroup, viewState)
        }

        for (collisionGroup in allDayGroups) {
            expandEventsToMaxWidth(collisionGroup, viewState)
        }
    }

    private fun List<EventChip>.toSingleColumnCollisionGroups(): List<CollisionGroup> {
        return map { CollisionGroup(it) }
    }

    private fun List<EventChip>.toMultiColumnCollisionGroups(): List<CollisionGroup> {
        val collisionGroups = mutableListOf<CollisionGroup>()

        for (eventChip in this) {
            val collidingGroup = collisionGroups.firstOrNull { it.collidesWith(eventChip) }

            if (collidingGroup != null) {
                collidingGroup.add(eventChip)
            } else {
                collisionGroups += CollisionGroup(eventChip)
            }
        }

        return collisionGroups
    }

    /**
     * Expands all [EventChip]s in a [CollisionGroup] to their maximum width.
     */
    private fun expandEventsToMaxWidth(collisionGroup: CollisionGroup, viewState: ViewState) {
        val columns = mutableListOf<Column>()
        columns += Column(index = 0)

        for (eventChip in collisionGroup.eventChips) {
            val fittingColumns = columns.filter { it.fits(eventChip) }
            when (fittingColumns.size) {
                0 -> {
                    val index = columns.size
                    columns += Column(index, eventChip)
                }
                1 -> {
                    val fittingColumn = fittingColumns.single()
                    fittingColumn.add(eventChip)
                }
                else -> {
                    // This event chip can span multiple columns.
                    val areAdjacentColumns = fittingColumns.map { it.index }.isContinuous
                    if (areAdjacentColumns) {
                        for (column in fittingColumns) {
                            column.add(eventChip)
                        }
                    } else {
                        val leftMostColumn = checkNotNull(fittingColumns.minByOrNull { it.index })
                        leftMostColumn.add(eventChip)
                    }
                }
            }
        }

        val rows = columns.map { it.size }.maxOrNull() ?: 0
        val columnWidth = 1f / columns.size

        for (row in 0 until rows) {
            val zipped = columns.zipWithPrevious()
            for ((previous, current) in zipped) {
                val hasEventInRow = current.size > row
                if (hasEventInRow) {
                    expandColumnEventToMaxWidth(current, previous, row, columnWidth, columns.size)
                }
            }
        }

        for (eventChip in collisionGroup.eventChips) {
            calculateMinutesFromStart(eventChip, viewState)
        }
    }

    private fun calculateMinutesFromStart(eventChip: EventChip, viewState: ViewState) {
        if (eventChip.item.isAllDay) {
            return
        }

        eventChip.minutesFromStartHour = viewState.minutesFromStart(eventChip.startTime)
    }

    private fun expandColumnEventToMaxWidth(
        current: Column,
        previous: Column?,
        row: Int,
        columnWidth: Float,
        columns: Int
    ) {
        val index = current.index
        val eventChip = current[row]

        val duplicateInPreviousColumn = previous?.findDuplicate(eventChip)

        if (duplicateInPreviousColumn != null) {
            duplicateInPreviousColumn.relativeWidth += columnWidth
        } else {
            // Every column gets the same width. For instance, if there are four columns,
            // then each column's width is 0.25.
            eventChip.relativeWidth = columnWidth

            // The start position is calculated based on the index of the column. For
            // instance, if there are four columns, the start positions will be 0.0, 0.25, 0.5
            // and 0.75.
            eventChip.relativeStart = index.toFloat() / columns
        }
    }

    /**
     * This class encapsulates [EventChip]s that collide with each other, meaning that
     * they overlap from a time perspective.
     *
     */
    private class CollisionGroup(
        val eventChips: MutableList<EventChip>
    ) {

        constructor(eventChip: EventChip) : this(mutableListOf(eventChip))

        /**
         * Returns whether an [EventChip] collides with any [EventChip] already in the
         * [CollisionGroup].
         *
         * @param eventChip An [EventChip]
         * @return Whether a collision exists
         */
        fun collidesWith(eventChip: EventChip): Boolean {
            return eventChips.any { it.item.collidesWith(eventChip.item) }
        }

        fun add(eventChip: EventChip) {
            eventChips.add(eventChip)
        }
    }

    /**
     * This class encapsulates [EventChip]s that are displayed in the same column.
     */
    private class Column(
        val index: Int,
        val eventChips: MutableList<EventChip> = mutableListOf()
    ) {

        constructor(index: Int, eventChip: EventChip) : this(index, mutableListOf(eventChip))

        val isEmpty: Boolean
            get() = eventChips.isEmpty()

        val size: Int
            get() = eventChips.size

        fun add(eventChip: EventChip) {
            eventChips.add(eventChip)
        }

        fun findDuplicate(eventChip: EventChip) = eventChips.firstOrNull { it == eventChip }

        operator fun get(index: Int): EventChip = eventChips[index]

        fun fits(eventChip: EventChip): Boolean {
            return isEmpty || !eventChips.last().item.collidesWith(eventChip.item)
        }
    }

    private val List<Int>.isContinuous: Boolean
        get() {
            val zipped = sorted().zipWithNext()
            return zipped.all { it.first + 1 == it.second }
        }

    private fun <T> List<T>.zipWithPrevious(): List<Pair<T?, T>> {
        val results = mutableListOf<Pair<T?, T>>()
        for (index in 0 until size) {
            val previous = getOrNull(index - 1)
            val current = get(index)
            results += Pair(previous, current)
        }
        return results
    }

    private fun List<EventChip>.groupedByDate(): Map<Calendar, List<EventChip>> {
        return groupBy { it.startTime.atStartOfDay }
    }
}

private fun WeekViewItem.sanitize(viewState: ViewState): WeekViewItem {
    return if (timing is WeekViewItem.Timing.Bounded) {
        val shouldAdjustEndTime = timing.endTime.isAtStartOfPeriod(viewState.minHour)
        val newEndTime = if (shouldAdjustEndTime) {
            timing.endTime - Millis(1)
        } else {
            timing.endTime
        }

        copy(
            timing = WeekViewItem.Timing.Bounded(
                startTime = timing.startTime,
                endTime = newEndTime,
            )
        )
    } else {
        this
    }
}
