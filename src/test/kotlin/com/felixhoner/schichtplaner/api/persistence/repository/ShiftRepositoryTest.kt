package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.PostgresContainerTest
import com.felixhoner.schichtplaner.api.persistence.entity.*
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalTime
import javax.transaction.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Transactional
class ShiftRepositoryTest {

	private val db = PostgresContainerTest()

	@Autowired
	lateinit var planRepository: PlanRepository

	@Autowired
	lateinit var productionRepository: ProductionRepository

	@Autowired
	lateinit var shiftRepository: ShiftRepository

	@Test
	fun `should find by plans`() {
		val konzert = PlanEntity("Konzert 2021")
		val vtf = PlanEntity("Vatertagsfest 2021")
		val kabarett = PlanEntity("Kabarett 2021")
		planRepository.saveAll(listOf(konzert, vtf, kabarett))

		val konzertEntrance = ProductionEntity("Einlass", konzert)
		val konzertDrinks = ProductionEntity("Getränke", konzert)
		val vtfDrinks = ProductionEntity("Getränke", vtf)
		val vtfFries = ProductionEntity("Pommes", vtf)
		val kabarettEntrance = ProductionEntity("Einlass", kabarett)
		productionRepository.saveAll(listOf(konzertEntrance, konzertDrinks, vtfDrinks, vtfFries, kabarettEntrance))

		val konzertEntranceShift = ShiftEntity(LocalTime.parse("19:00"), LocalTime.parse("20:15"), konzertEntrance)
		val vtfDrinksShift1 = ShiftEntity(LocalTime.parse("09:30"), LocalTime.parse("14:00"), vtfDrinks)
		val vtfDrinksShift2 = ShiftEntity(LocalTime.parse("14:00"), LocalTime.parse("18:30"), vtfDrinks)
		val vtfDrinksShift3 = ShiftEntity(LocalTime.parse("18:30"), LocalTime.parse("23:45"), vtfDrinks)
		val vtfFriesShift1 = ShiftEntity(LocalTime.parse("09:30"), LocalTime.parse("14:00"), vtfFries)
		val vtfFriesShift2 = ShiftEntity(LocalTime.parse("14:00"), LocalTime.parse("18:30"), vtfFries)
		val vtfFriesShift3 = ShiftEntity(LocalTime.parse("18:30"), LocalTime.parse("23:45"), vtfFries)
		shiftRepository.saveAll(
			listOf(
				konzertEntranceShift, vtfDrinksShift1, vtfDrinksShift2, vtfDrinksShift3, vtfFriesShift1,
				vtfFriesShift2, vtfFriesShift3
			)
		)

		val queried = listOf(konzertEntranceShift, vtfDrinksShift1, vtfDrinksShift2, vtfDrinksShift3)
		val result = shiftRepository.findAllByProductionIds(queried.map { it.production.id!! })
		result shouldHaveSize 4
		result.map { Pair(it.startTime, it.endTime) } shouldContainExactlyInAnyOrder queried.map { Pair(it.startTime, it.endTime) }
	}

}
