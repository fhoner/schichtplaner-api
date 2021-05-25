package com.felixhoner.schichtplaner.api.systemtest

import com.felixhoner.schichtplaner.api.auth.LoginRequest
import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserRole
import com.felixhoner.schichtplaner.api.persistence.repository.PlanRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ProductionRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ShiftRepository
import com.felixhoner.schichtplaner.api.persistence.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import java.time.Instant.parse


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("system-test")
class BaseSystemTest {

    @LocalServerPort
    var serverPort = 0

    @Autowired
    lateinit var planRepository: PlanRepository

    @Autowired
    lateinit var productionRepository: ProductionRepository

    @Autowired
    lateinit var shiftRepository: ShiftRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: BCryptPasswordEncoder

    @Autowired
    lateinit var webClientBuilder: WebClient.Builder

    @Autowired
    lateinit var testClient: WebTestClient

    private lateinit var webClient: WebClient

    @BeforeEach
    fun buildWebClient() {
        webClient = webClientBuilder
            .clone()
            .baseUrl("http://localhost:$serverPort")
            .build()
    }

    fun doSuccessfulLogin(): Mono<Pair<String, String>> {
        return webClient
            .post()
            .uri("/auth/login")
            .bodyValue(LoginRequest("felix.honer@novatec-gmbh.de", "felix"))
            .exchangeToMono {
                Mono.just(
                    Pair(
                        it.cookies()["access_token"]!![0].value,
                        it.cookies()["refresh_token"]!![0].value
                    )
                )
            }
    }

    fun fileContents(filename: String): String = BaseSystemTest::class.java.getResource("/systest/$filename.json")!!.readText()

    fun prepareData() {
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

        val konzertEntranceShift = ShiftEntity(
            startTime = parse("2021-01-01T19:00:00Z"),
            endTime = parse("2021-01-01T20:15:00Z"),
            production = konzertEntrance
        )
        val vtfDrinksShift1 = ShiftEntity(
            startTime = parse("2021-01-01T09:30:00Z"),
            endTime = parse("2021-01-01T14:00:00Z"),
            production = vtfDrinks
        )
        val vtfDrinksShift2 = ShiftEntity(
            startTime = parse("2021-01-01T14:00:00Z"),
            endTime = parse("2021-01-01T18:30:00Z"),
            production = vtfDrinks
        )
        val vtfDrinksShift3 = ShiftEntity(
            startTime = parse("2021-01-01T18:30:00Z"),
            endTime = parse("2021-01-01T23:45:00Z"),
            production = vtfDrinks
        )
        val vtfFriesShift1 = ShiftEntity(
            startTime = parse("2021-01-01T09:30:00Z"),
            endTime = parse("2021-01-01T14:00:00Z"),
            production = vtfFries
        )
        val vtfFriesShift2 = ShiftEntity(
            startTime = parse("2021-01-01T14:00:00Z"),
            endTime = parse("2021-01-01T18:30:00Z"),
            production = vtfFries
        )
        val vtfFriesShift3 = ShiftEntity(
            startTime = parse("2021-01-01T18:30:00Z"),
            endTime = parse("2021-01-01T23:45:00Z"),
            production = vtfFries
        )
        shiftRepository.saveAll(
            listOf(
                konzertEntranceShift, vtfDrinksShift1, vtfDrinksShift2, vtfDrinksShift3, vtfFriesShift1,
                vtfFriesShift2, vtfFriesShift3
            )
        )

        val felix = UserEntity(email = "felix.honer@novatec-gmbh.de", password = passwordEncoder.encode("felix"), role = UserRole.READER)
        userRepository.save(felix)
    }

    fun clearData() {
        this.shiftRepository.deleteAll()
        this.productionRepository.deleteAll()
        this.planRepository.deleteAll()
        this.userRepository.deleteAll()
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
