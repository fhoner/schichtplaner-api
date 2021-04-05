package com.felixhoner.schichtplaner.api.graphql.dataloader

import com.felixhoner.schichtplaner.api.business.model.Shift
import com.felixhoner.schichtplaner.api.business.service.ShiftService
import com.felixhoner.schichtplaner.api.graphql.dto.ShiftDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.dataloader.DataLoader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalTime.parse
import java.util.*

@Suppress("unchecked_cast")
class ShiftsDataLoaderTest {

    private val shiftService: ShiftService = mockk()
    private val transformer: TransformerDto = mockk()

    val shifts = listOf(
        listOf(
            Shift(id = 1, uuid = UUID.randomUUID(), startTime = parse("14:00"), endTime = parse("16:00")),
            Shift(id = 2, uuid = UUID.randomUUID(), startTime = parse("16:00"), endTime = parse("18:00"))
        ),
        emptyList(),
        listOf(
            Shift(id = 3, uuid = UUID.randomUUID(), startTime = parse("12:00"), endTime = parse("13:00"))
        )
    )
    private val shiftsDto = listOf(
        listOf(
            shifts[0][0].run { ShiftDto(id, uuid.toString(), startTime.toString(), endTime.toString()) },
            shifts[0][1].run { ShiftDto(id, uuid.toString(), startTime.toString(), endTime.toString()) }
        ),
        emptyList(),
        listOf(
            shifts[2][0].run { ShiftDto(id, uuid.toString(), startTime.toString(), endTime.toString()) }
        )
    )

    private val cut = ShiftsDataLoader(
        shiftService,
        transformer
    )

    @BeforeEach
    fun `mock response`() {
        every { shiftService.getByProductions(any()) } returns shifts
        every { transformer.toDto(shifts[0][0]) } returns shiftsDto[0][0]
        every { transformer.toDto(shifts[0][1]) } returns shiftsDto[0][1]
        every { transformer.toDto(shifts[2][0]) } returns shiftsDto[2][0]
    }

    @Test
    fun `should create dataloader with batch loader`() {
        cut.getInstance()
        verify(exactly = 0) { shiftService.getByProductions(any()) }
    }

    @Test
    fun `should have name productionLoader`() {
        cut.name shouldBe "shiftLoader"
    }

    @Test
    fun `should load shifts correctly`() {
        val loader = cut.getInstance() as DataLoader<Long, List<ShiftDto>>
        loader.apply {
            load(1)
            load(2)
            load(3)
        }
        val result = runBlocking { loader.dispatchAndJoin() }
        result shouldHaveSize 3
        result[0] shouldBe listOf(shiftsDto[0][0], shiftsDto[0][1])
        result[1] shouldBe emptyList()
        result[2] shouldBe listOf(shiftsDto[2][0])
        verify(exactly = 1) { shiftService.getByProductions(listOf(1, 2, 3)) }
        verify(exactly = 3) { transformer.toDto(any<Shift>()) }
    }

}
