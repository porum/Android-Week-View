package com.alamkanak.weekview.util

import com.alamkanak.weekview.WeekViewItem
import com.alamkanak.weekview.plusMinutes
import java.util.Calendar
import kotlin.random.Random

internal object Mocks {

    fun weekViewItems(count: Int): List<WeekViewItem> {
        return (0 until count).map { weekViewItem() }
    }

    fun weekViewItem(
        startTime: Calendar = Calendar.getInstance(),
        endTime: Calendar = Calendar.getInstance().plusMinutes(60),
    ): WeekViewItem {
        val id = Random.nextLong()
        return WeekViewItem(
            id = id,
            title = "Title $id",
            duration = WeekViewItem.Duration.Bounded(
                startTime = startTime,
                endTime = endTime,
            ),
            subtitle = null,
            data = Event(startTime, endTime),
        )
    }
}

internal fun WeekViewItem.withDifferentId() = copy(id = Random.nextLong())
