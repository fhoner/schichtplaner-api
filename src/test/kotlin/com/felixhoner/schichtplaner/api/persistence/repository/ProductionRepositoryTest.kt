package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.util.DatabaseTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

@DatabaseTest
class ProductionRepositoryTest {

    @Autowired
    lateinit var planRepository: PlanRepository

    @Autowired
    lateinit var productionRepository: ProductionRepository

    @Autowired
    lateinit var shiftRepository: ShiftRepository

    private val konzert = PlanEntity(name = "Konzert 2021")
    private val vtf = PlanEntity(name = "Vatertagsfest 2021")
    private val kabarett = PlanEntity(name = "Kabarett 2021")

    private val konzertEntrance = ProductionEntity(name = "Einlass", plan = konzert)
    private val konzertDrinks = ProductionEntity(name = "Getränke", plan = konzert)
    private val vtfDrinks = ProductionEntity(name = "Getränke", plan = vtf)
    private val vtfFries = ProductionEntity(name = "Pommes", plan = vtf)
    private val kabarettEntrance = ProductionEntity(name = "Einlass", plan = kabarett)

    @BeforeEach
    fun setup() {
        planRepository.saveAll(listOf(konzert, vtf, kabarett))
        productionRepository.saveAll(listOf(konzertEntrance, konzertDrinks, vtfDrinks, vtfFries, kabarettEntrance))
    }

    @Test
    fun `should find by plans`() {
        val queried = listOf(konzert, vtf).map { it.id!! }
        val result = productionRepository.findAllByPlanIds(queried)
        result shouldHaveSize 4
        result.map { it.name } shouldContainExactlyInAnyOrder listOf("Einlass", "Getränke", "Getränke", "Pommes")
    }

    @Nested
    inner class FindByUuid {

        @BeforeEach
        fun insertShifts() {
            val drinksShifts =
                listOf(ShiftEntity(startTime = Instant.parse("2021-01-01T11:00:00Z"), endTime = Instant.parse("2021-01-01T12:00:00Z")))
            val entranceShifts = listOf(
                ShiftEntity(startTime = Instant.parse("2021-01-01T14:00:00Z"), endTime = Instant.parse("2021-01-01T15:00:00Z")),
                ShiftEntity(startTime = Instant.parse("2021-01-01T15:00:00Z"), endTime = Instant.parse("2021-01-01T16:00:00Z"))
            )
            shiftRepository.saveAll(drinksShifts + entranceShifts)

            konzertDrinks.shifts.addAll(drinksShifts)
            konzertEntrance.shifts.addAll(entranceShifts)
            productionRepository.save(konzertEntrance)
        }

        @Test
        fun `should find by uuid and fetch shifts`() {
            val result = productionRepository.findByUuid(konzertEntrance.uuid)
            result shouldNotBe null
            result!!.shifts shouldHaveSize 2
            result.shifts[0].apply {
                startTime shouldBe Instant.parse("2021-01-01T14:00:00Z")
                endTime shouldBe Instant.parse("2021-01-01T15:00:00Z")
            }
            result.shifts[1].apply {
                startTime shouldBe Instant.parse("2021-01-01T15:00:00Z")
                endTime shouldBe Instant.parse("2021-01-01T16:00:00Z")
            }
        }

        @Test
        fun `should find by uuid if no shift exists`() {
            val result = productionRepository.findByUuid(vtfFries.uuid)
            result shouldNotBe null
            result!!.shifts shouldHaveSize 0
        }

    }
}
