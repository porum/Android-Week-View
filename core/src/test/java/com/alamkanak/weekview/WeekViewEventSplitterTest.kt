package com.alamkanak.weekview

import com.alamkanak.weekview.util.Event
import com.alamkanak.weekview.util.createResolvedWeekViewEvent
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import org.mockito.Mockito.`when` as whenever

class WeekViewEventSplitterTest {

    private val viewState: ViewState = mock()

    @Test
    fun `single-day event is not split`() {
        whenever(viewState.minHour).thenReturn(0)
        whenever(viewState.maxHour).thenReturn(24)

        val startTime = LocalDate.now().atTime(11, 0)
        val endTime = startTime.plusHours(2)
        val event = createResolvedWeekViewEvent(startTime, endTime)

        val results = event.split(viewState)
        val expected = listOf(event)

        assertEquals(expected, results)
    }

    @Test
    fun `single-day event before range is ignored`() {
        whenever(viewState.minHour).thenReturn(7)
        whenever(viewState.maxHour).thenReturn(21)

        val event = createResolvedWeekViewEvent(
            startTime = LocalDate.now().atTime(1, 0),
            endTime = LocalDate.now().atTime(2, 0),
        )
        val results = event.split(viewState)

        assertEquals(emptyList<ResolvedWeekViewEntity>(), results)
    }

    @Test
    fun `single-day event after range is ignored`() {
        whenever(viewState.minHour).thenReturn(7)
        whenever(viewState.maxHour).thenReturn(21)

        val event = createResolvedWeekViewEvent(
            startTime = LocalDate.now().atTime(22, 0),
            endTime = LocalDate.now().plusDays(1).atTime(6, 0)
        )

        val results = event.split(viewState)

        assertEquals(emptyList<ResolvedWeekViewEntity>(), results)
    }

    @Test
    fun `early single-day event partially out of range is adjusted correctly`() {
        whenever(viewState.minHour).thenReturn(10)
        whenever(viewState.maxHour).thenReturn(20)

        val startTime = LocalDate.now().atTime(8, 0)
        val endTime = LocalDate.now().atTime(12, 0)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(viewState)

        val expected = listOf(
            Event(
                startTime = LocalDate.now().atTime(10, 0),
                endTime = LocalDate.now().atTime(12, 0),
            )
        )

        val expectedTimes = expected.map { it.startTime to it.endTime }
        val resultTimes = results.map { it.startTime to it.endTime }

        assertEquals(expectedTimes, resultTimes)
    }

    @Test
    fun `late single-day event partially out of range is adjusted correctly`() {
        whenever(viewState.minHour).thenReturn(10)
        whenever(viewState.maxHour).thenReturn(20)

        val startTime = LocalDate.now().atTime(18, 0)
        val endTime = LocalDate.now().atTime(23, 0)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(viewState)

        val expected = listOf(
            Event(
                startTime = LocalDate.now().atTime(18, 0),
                endTime = LocalDate.now().atTime(19, 59, 59, 999_999_999),
            )
        )

        val expectedTimes = expected.map { it.startTime to it.endTime }
        val resultTimes = results.map { it.startTime to it.endTime }

        assertEquals(expectedTimes, resultTimes)
    }

    @Test
    fun `two-day event in range is split correctly`() {
        whenever(viewState.minHour).thenReturn(0)
        whenever(viewState.maxHour).thenReturn(24)

        val startTime = LocalDate.now().atTime(11, 0)
        val endTime = (startTime.plusDays(1)).withHour(2)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(viewState)

        val expected = listOf(
            Event(startTime, startTime.atEndOfDay()),
            Event(endTime.atStartOfDay(), endTime)
        )

        val expectedTimes = expected.map { it.startTime to it.endTime }
        val resultTimes = results.map { it.startTime to it.endTime }

        assertEquals(expectedTimes, resultTimes)
    }

    @Test
    fun `two-day event out of range is split correctly`() {
        val minHour = 7
        val maxHour = 21

        whenever(viewState.minHour).thenReturn(minHour)
        whenever(viewState.maxHour).thenReturn(maxHour)

        val startTime = LocalDate.now().atTime(5, 0)
        val endTime = (startTime.plusDays(2)).withHour(23)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(viewState)

        val tomorrow = LocalDate.now().plusDays(1)

        val expected = listOf(
            Event(
                startTime = startTime.withHour(minHour),
                endTime = startTime.withTimeAtEndOfPeriod(maxHour),
            ),
            Event(
                startTime = tomorrow.atTime(minHour, 0),
                endTime = tomorrow.atTime(maxHour, 0).minusNanos(1),
            ),
            Event(
                startTime = endTime.withHour(minHour),
                endTime = endTime.withTimeAtEndOfPeriod(maxHour),
            )
        )

        val expectedTimes = expected.map { it.startTime to it.endTime }
        val resultTimes = results.map { it.startTime to it.endTime }

        assertEquals(expectedTimes, resultTimes)
    }

    @Test
    fun `two-day event ending before range start is split correctly`() {
        val minHour = 7
        val maxHour = 21

        whenever(viewState.minHour).thenReturn(minHour)
        whenever(viewState.maxHour).thenReturn(maxHour)

        val startTime = LocalDate.now().atTime(8, 0)
        val endTime = startTime.plusDays(1).withHour(5)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(viewState)

        val expected = listOf(
            Event(startTime, startTime.withTimeAtEndOfPeriod(maxHour))
        )

        val expectedTimes = expected.map { it.startTime to it.endTime }
        val resultTimes = results.map { it.startTime to it.endTime }

        assertEquals(expectedTimes, resultTimes)
    }

    @Test
    fun `two-day event starting after range end is split correctly`() {
        val minHour = 7
        val maxHour = 21

        whenever(viewState.minHour).thenReturn(minHour)
        whenever(viewState.maxHour).thenReturn(maxHour)

        val startTime = LocalDate.now().atTime(22, 0)
        val endTime = startTime.plusDays(1).withHour(9)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(viewState)

        val expected = listOf(
            Event(endTime.withHour(minHour), endTime)
        )

        val expectedTimes = expected.map { it.startTime to it.endTime }
        val resultTimes = results.map { it.startTime to it.endTime }

        assertEquals(expectedTimes, resultTimes)
    }

    @Test
    fun `three-day event is split correctly`() {
        whenever(viewState.minHour).thenReturn(0)
        whenever(viewState.maxHour).thenReturn(24)

        val startTime = LocalDate.now().atTime(11, 0)
        val endTime = startTime.plusDays(2).withHour(2)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(viewState)

        val intermediateDate = startTime.plusDays(1)
        val expected = listOf(
            Event(startTime, startTime.atEndOfDay()),
            Event(intermediateDate.atStartOfDay(), intermediateDate.atEndOfDay()),
            Event(endTime.atStartOfDay(), endTime)
        )

        val expectedTimes = expected.map { it.startTime to it.endTime }
        val resultTimes = results.map { it.startTime to it.endTime }

        assertEquals(expectedTimes, resultTimes)
    }
}
