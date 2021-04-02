package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.util.DatabaseTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalTime.parse

@DatabaseTest
class ShiftRepositoryTest {

    @Autowired
    lateinit var planRepository: PlanRepository

    @Autowired
    lateinit var productionRepository: ProductionRepository

    @Autowired
    lateinit var shiftRepository: ShiftRepository

    @Test
    fun `should find by plans`() {
        val konzert = PlanEntity(name = "Konzert 2021")
        val vtf = PlanEntity(name = "Vatertagsfest 2021")
        val kabarett = PlanEntity(name = "Kabarett 2021")
        planRepository.saveAll(listOf(konzert, vtf, kabarett))

        val konzertEntrance = ProductionEntity(name = "Einlass", plan = konzert)
        val konzertDrinks = ProductionEntity(name = "Getränke", plan = konzert)
        val vtfDrinks = ProductionEntity(name = "Getränke", plan = vtf)
        val vtfFries = ProductionEntity(name = "Pommes", plan = vtf)
        val kabarettEntrance = ProductionEntity(name = "Einlass", plan = kabarett)
        productionRepository.saveAll(listOf(konzertEntrance, konzertDrinks, vtfDrinks, vtfFries, kabarettEntrance))

        val konzertEntranceShift = ShiftEntity(startTime = parse("19:00"), endTime = parse("20:15"), production = konzertEntrance)
        val vtfDrinksShift1 = ShiftEntity(startTime = parse("09:30"), endTime = parse("14:00"), production = vtfDrinks)
        val vtfDrinksShift2 = ShiftEntity(startTime = parse("14:00"), endTime = parse("18:30"), production = vtfDrinks)
        val vtfDrinksShift3 = ShiftEntity(startTime = parse("18:30"), endTime = parse("23:45"), production = vtfDrinks)
        val vtfFriesShift1 = ShiftEntity(startTime = parse("09:30"), endTime = parse("14:00"), production = vtfFries)
        val vtfFriesShift2 = ShiftEntity(startTime = parse("14:00"), endTime = parse("18:30"), production = vtfFries)
        val vtfFriesShift3 = ShiftEntity(startTime = parse("18:30"), endTime = parse("23:45"), production = vtfFries)
        shiftRepository.saveAll(
            listOf(
                konzertEntranceShift, vtfDrinksShift1, vtfDrinksShift2, vtfDrinksShift3, vtfFriesShift1,
                vtfFriesShift2, vtfFriesShift3
            )
        )

        val queried = listOf(konzertEntranceShift, vtfDrinksShift1, vtfDrinksShift2, vtfDrinksShift3)
        val result = shiftRepository.findAllByProductionIds(queried.map { it.production?.id!! })
        result shouldHaveSize 4
        result.map { Pair(it.startTime, it.endTime) } shouldContainExactlyInAnyOrder queried.map { Pair(it.startTime, it.endTime) }
    }

}
