package com.alamkanak.weekview

import com.alamkanak.weekview.util.createResolvedWeekViewEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import org.mockito.Mockito.`when` as whenever

class EventChipTest {

    private val viewState = Mockito.mock(ViewState::class.java)
    private val factory = EventChipsFactory()

    init {
        MockitoAnnotations.openMocks(this)
        whenever(viewState.minHour).thenReturn(0)
        whenever(viewState.maxHour).thenReturn(24)
    }

    @Test
    fun `single-day event is recognized correctly`() {
        val startTime = LocalDate.now().plusDays(1).atTime(6, 0)
        val endTime = startTime.plusHours(10)

        val originalEvent = createResolvedWeekViewEvent(startTime, endTime)

        val eventChips = factory.create(listOf(originalEvent), viewState)
        assertTrue(eventChips.size == 1)

        val child = eventChips.first()
        assertFalse(child.endsOnLaterDay)
        assertFalse(child.startsOnEarlierDay)
    }

    @Test
    fun `two-day event is recognized correctly`() {
        val startTime = LocalDate.now().atTime(14, 0)
        val endTime = startTime.plusDays(1)

        val originalEvent = createResolvedWeekViewEvent(startTime, endTime)
        val eventChips = factory.create(listOf(originalEvent), viewState)
        assertTrue(eventChips.size == 2)

        val first = eventChips.first()
        val last = eventChips.last()

        assertTrue(first.endsOnLaterDay)
        assertTrue(last.startsOnEarlierDay)

        assertFalse(first.startsOnEarlierDay)
        assertFalse(last.endsOnLaterDay)
    }

    @Test
    fun `multi-day event is recognized correctly`() {
        val startTime = LocalDate.now().atTime(14, 0)
        val endTime = startTime.plusDays(2)

        val originalEvent = createResolvedWeekViewEvent(startTime, endTime)
        val eventChips = factory.create(listOf(originalEvent), viewState)
        assertTrue(eventChips.size == 3)

        val (first, second, third) = eventChips

        assertTrue(first.endsOnLaterDay)
        assertTrue(second.startsOnEarlierDay)
        assertTrue(second.endsOnLaterDay)
        assertTrue(third.startsOnEarlierDay)

        assertFalse(first.startsOnEarlierDay)
        assertFalse(third.endsOnLaterDay)
    }

    @Test
    fun `non-colliding events are recognized correctly`() {
        val firstStartTime = LocalDateTime.now()
        val firstEndTime = firstStartTime.plusHours(1)
        val first = createResolvedWeekViewEvent(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime.plusHours(2)
        val secondEndTime = secondStartTime.plusHours(1)
        val second = createResolvedWeekViewEvent(secondStartTime, secondEndTime)

        assertFalse(first.collidesWith(second))
    }

    @Test
    fun `overlapping events are recognized as colliding`() {
        val firstStartTime = LocalDateTime.now()
        val firstEndTime = firstStartTime.plusHours(1)
        val first = createResolvedWeekViewEvent(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime.minusHours(1)
        val secondEndTime = firstEndTime.plusHours(1)
        val second = createResolvedWeekViewEvent(secondStartTime, secondEndTime)

        assertTrue(first.collidesWith(second))
    }

    @Test
    fun `partly-overlapping events are recognized as colliding`() {
        val firstStartTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
        val firstEndTime = firstStartTime.plusHours(1)
        val first = createResolvedWeekViewEvent(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime.withMinute(30)
        val secondEndTime = secondStartTime.plusHours(1)
        val second = createResolvedWeekViewEvent(secondStartTime, secondEndTime)

        assertTrue(first.collidesWith(second))
    }
}
