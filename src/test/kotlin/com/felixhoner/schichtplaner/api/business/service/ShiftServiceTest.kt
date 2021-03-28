package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.exception.DuplicateShiftTimeException
import com.felixhoner.schichtplaner.api.business.model.Shift
import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.repository.ProductionRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ShiftRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalTime.parse


class ShiftServiceTest {

    private val shiftRepository: ShiftRepository = mockk()
    private val productionRepository: ProductionRepository = mockk()
    private val transformer: TransformerBo = mockk()
    private val plan: PlanEntity = mockk()

    private val cut = ShiftService(
        shiftRepository,
        productionRepository,
        transformer
    )

    @Test
    fun `should create shift if timeslot is not booked yet`() {
        val productionEntity = ProductionEntity(name = "Name", plan = plan, shifts = mutableListOf())
        val createdShift = ShiftEntity(
            startTime = parse("14:00"),
            endTime = parse("16:00"),
            production = productionEntity
        )
        val savedShift = createdShift.copy(id = 4711)
        val createdShiftBo = Shift(
            id = 4711,
            uuid = createdShift.uuid,
            startTime = parse("14:00"),
            endTime = parse("16:00"),
        )
        val savedShiftSLot = slot<ShiftEntity>()
        every { productionRepository.findByUuid(any()) } returns productionEntity
        every { shiftRepository.save(capture(savedShiftSLot)) } returns savedShift
        every { transformer.toBo(any<ShiftEntity>()) } returns createdShiftBo

        val result = cut.createShift(productionEntity.uuid, "14:00", "16:00")
        result shouldBe createdShiftBo

        savedShiftSLot.captured.apply {
            production shouldBe productionEntity
            startTime shouldBe parse("14:00")
            endTime shouldBe parse("16:00")
        }
        verify {
            productionRepository.findByUuid(productionEntity.uuid)
            transformer.toBo(savedShift)
        }
    }

    @Test
    fun `should throw if time slot is taken by another shift`() {
        val productionEntity = ProductionEntity(
            name = "Name",
            plan = plan,
            shifts = mutableListOf(
                ShiftEntity(
                    startTime = parse("14:00"),
                    endTime = parse("16:00"),
                )
            )
        )
        every { productionRepository.findByUuid(any()) } returns productionEntity

        shouldThrow<DuplicateShiftTimeException> {
            cut.createShift(productionEntity.uuid, "14:00", "16:00")
        }

        verify { shiftRepository.save(any()) wasNot called }
    }

}
