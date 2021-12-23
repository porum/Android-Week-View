package com.alamkanak.weekview.util

import com.alamkanak.weekview.Hours
import com.alamkanak.weekview.WeekViewItem
import com.alamkanak.weekview.plus
import java.util.Calendar
import kotlin.random.Random

internal object Mocks {

    fun weekViewItems(count: Int): List<WeekViewItem> {
        return (0 until count).map { weekViewItem() }
    }

    fun weekViewItem(
        startTime: Calendar = Calendar.getInstance(),
        endTime: Calendar = Calendar.getInstance() + Hours(1),
    ): WeekViewItem {
        val id = Random.nextLong()
        return WeekViewItem(
            id = id,
            title = "Title $id",
            timing = WeekViewItem.Timing.Bounded(
                startTime = startTime,
                endTime = endTime,
            ),
            subtitle = null,
            data = Event(startTime, endTime),
        )
    }
}

internal fun WeekViewItem.withDifferentId() = copy(id = Random.nextLong())
