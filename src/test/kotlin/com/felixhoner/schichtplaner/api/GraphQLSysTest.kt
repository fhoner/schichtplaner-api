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
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith(SpringExtension::class)
@Testcontainers
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
