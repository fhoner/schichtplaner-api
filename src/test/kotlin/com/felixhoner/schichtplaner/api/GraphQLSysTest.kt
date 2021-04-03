package com.felixhoner.schichtplaner.api

import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.repository.PlanRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ProductionRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ShiftRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalTime.parse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith(SpringExtension::class)
@Testcontainers
@ActiveProfiles("system-test")
class GraphQLSysTest {

    @Autowired
    lateinit var planRepository: PlanRepository

    @Autowired
    lateinit var productionRepository: ProductionRepository

    @Autowired
    lateinit var shiftRepository: ShiftRepository

    @Autowired
    lateinit var testClient: WebTestClient

    @Test
    fun `should return correct json`() {
        insertData()
        val query = """
			query {
			  getPlans {
				name
				productions {
				  name
				  shifts {
					startTime
					endTime
				  }
				}
			  }
			}
		"""
            .replace("\n", "")
            .replace("\t", " ")
            .replace(" +".toRegex(), " ")

        testClient.post()
            .uri(GRAPHQL_ENDPOINT)
            .accept(APPLICATION_JSON)
            .contentType(GRAPHQL_MEDIA_TYPE)
            .bodyValue(query)
            .exchange()
    }

    fun insertData() {
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
    }

    companion object {
        @Container
        val container = PostgreSQLContainer<Nothing>("postgres:13").apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.username", container::getUsername)
        }
    }
}
