package com.alamkanak.weekview

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alamkanak.weekview.util.Mocks
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever

@RunWith(AndroidJUnit4::class)
class WeekViewEventTest {

    private val viewState = Mockito.mock(ViewState::class.java)
    private val factory = EventChipsFactory()

    init {
        MockitoAnnotations.openMocks(this)
        whenever(viewState.minHour).thenReturn(0)
        whenever(viewState.maxHour).thenReturn(24)
    }

    @Test
    fun `single-day event is recognized correctly`() {
        val startTime = (today().plusDays(1)).withHour(6).withMinutes(0)
        val endTime = startTime.plusHours(10)

        val originalEvent = Mocks.weekViewItem(startTime, endTime)

        val eventChips = factory.create(listOf(originalEvent), viewState)
        assertTrue(eventChips.size == 1)

        val child = eventChips.first()
        assertFalse(child.endsOnLaterDay)
        assertFalse(child.startsOnEarlierDay)
    }

    @Test
    fun `two-day event is recognized correctly`() {
        val startTime = (today().plusDays(1)).withHour(14).withMinutes(0)
        val endTime = (today().plusDays(2)).withHour(14).withMinutes(0)

        val originalEvent = Mocks.weekViewItem(startTime, endTime)
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
        val startTime = (today().plusDays(1)).withHour(14).withMinutes(0)
        val endTime = (today().plusDays(3)).withHour(1).withMinutes(0)

        val originalEvent = Mocks.weekViewItem(startTime, endTime)
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
        val firstStartTime = now()
        val firstEndTime = firstStartTime.plusHours(1)
        val first = Mocks.weekViewItem(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime.plusHours(2)
        val secondEndTime = secondStartTime.plusHours(1)
        val second = Mocks.weekViewItem(secondStartTime, secondEndTime)

        assertFalse(first.collidesWith(second))
    }

    @Test
    fun `overlapping events are recognized as colliding`() {
        val firstStartTime = now()
        val firstEndTime = firstStartTime.plusHours(1)
        val first = Mocks.weekViewItem(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime.minusHours(1)
        val secondEndTime = firstEndTime.plusHours(1)
        val second = Mocks.weekViewItem(secondStartTime, secondEndTime)

        assertTrue(first.collidesWith(second))
    }

    @Test
    fun `partly-overlapping events are recognized as colliding`() {
        val firstStartTime = now().withMinutes(0)
        val firstEndTime = firstStartTime.plusHours(1)
        val first = Mocks.weekViewItem(firstStartTime, firstEndTime)

        val secondStartTime = firstStartTime.withMinutes(30)
        val secondEndTime = secondStartTime.plusHours(1)
        val second = Mocks.weekViewItem(secondStartTime, secondEndTime)

        assertTrue(first.collidesWith(second))
    }
}
