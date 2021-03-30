package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.exception.DuplicateShiftTimeException
import com.felixhoner.schichtplaner.api.business.exception.InvalidStartEndTimeException
import com.felixhoner.schichtplaner.api.business.exception.NotFoundException
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalTime.parse
import java.util.*


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

    @Nested
    inner class GetProductions {

        private val shiftEntityMock = ShiftEntity(startTime = mockk(), endTime = mockk())
        private val shiftBusinessMock = Shift(id = 0, uuid = UUID.randomUUID(), startTime = mockk(), endTime = mockk())
        private val productionMock = ProductionEntity(plan = mockk(), name = "")

        @Test
        fun `should get production by ids in correct order`() {
            val shiftEntities = listOf(
                shiftEntityMock.copy(id = 11, production = productionMock.copy(id = 1)),
                shiftEntityMock.copy(id = 12, production = productionMock.copy(id = 1)),
                shiftEntityMock.copy(id = 21, production = productionMock.copy(id = 2)),
                shiftEntityMock.copy(id = 31, production = productionMock.copy(id = 3))
            )
            val shiftsBusiness = listOf(
                shiftBusinessMock.copy(id = 11),
                shiftBusinessMock.copy(id = 12),
                shiftBusinessMock.copy(id = 21),
                shiftBusinessMock.copy(id = 31)
            )
            every { shiftRepository.findAllByProductionIds(any()) } returns shiftEntities
            every { transformer.toBo(any<ShiftEntity>()) } answers { shiftBusinessMock.copy(id = (args[0] as ShiftEntity).id!!) }

            val result = cut.getByProductions(listOf(1, 3, 2))
            result shouldBe listOf(
                listOf(shiftsBusiness[0], shiftsBusiness[1]),
                listOf(shiftsBusiness[3]),
                listOf(shiftsBusiness[2])
            )
        }

        @Test
        fun `should return empty list for productions without any shift`() {
            val shiftEntities = listOf(shiftEntityMock.copy(id = 11, production = productionMock.copy(id = 1)))
            val shiftsBusiness = listOf(shiftBusinessMock.copy(id = 11))
            every { shiftRepository.findAllByProductionIds(any()) } returns shiftEntities
            every { transformer.toBo(any<ShiftEntity>()) } answers { shiftBusinessMock.copy(id = (args[0] as ShiftEntity).id!!) }

            val result = cut.getByProductions(listOf(1, 4711))
            result shouldBe listOf(
                shiftsBusiness,
                emptyList()
            )
        }

    }

    @Nested
    inner class CreateShift {

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
        fun `should throw no production was found by uuid`() {
            val uuid = UUID.randomUUID()
            every { productionRepository.findByUuid(any()) } returns null
            val exception = shouldThrow<NotFoundException> {
                cut.createShift(uuid, "14:00", "14:00")
            }
            exception.message shouldBe "No production with uuid [$uuid] was found"
        }

        @Test
        fun `should throw if start time is equal to end time`() {
            val productionEntity = ProductionEntity(name = "Name", plan = plan, shifts = mutableListOf())
            every { productionRepository.findByUuid(any()) } returns productionEntity

            val exception = shouldThrow<InvalidStartEndTimeException> {
                cut.createShift(productionEntity.uuid, "14:00", "14:00")
            }
            exception.message shouldBe "Start time must be before end time"
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

            val exception = shouldThrow<DuplicateShiftTimeException> {
                cut.createShift(productionEntity.uuid, "14:00", "16:00")
            }
            exception.message shouldBe "The production already contains a shift with [startTime, endTime] = [14:00, 16:00]"

            verify { shiftRepository.save(any()) wasNot called }
        }

    }

}
