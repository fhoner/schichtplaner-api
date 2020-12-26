package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.PostgresContainerTest
import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.transaction.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Transactional
class ProductionRepositoryTest {

	private val db = PostgresContainerTest()

	@Autowired
	lateinit var planRepository: PlanRepository

	@Autowired
	lateinit var productionRepository: ProductionRepository

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

		val queried = listOf(konzert, vtf).map { it.id!! }
		val result = productionRepository.findAllByPlanIds(queried)
		result shouldHaveSize 4
		result.map { it.name } shouldContainExactlyInAnyOrder listOf("Einlass", "Getränke", "Getränke", "Pommes")
	}

}
