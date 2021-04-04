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
import com.felixhoner.schichtplaner.api.security.SecurityConfiguration
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import java.time.LocalTime


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@Import(SecurityConfiguration::class)
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

    protected fun doSuccessfulLogin(): Pair<String, String> {
        return webClientBuilder.baseUrl("http://localhost:$serverPort").build().post()
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
            .block()!!
    }

    protected fun fileContents(filename: String): String = BaseSystemTest::class.java.getResource("/systest/$filename.json").readText()

    protected fun prepareData() {
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

        val konzertEntranceShift =
            ShiftEntity(startTime = LocalTime.parse("19:00"), endTime = LocalTime.parse("20:15"), production = konzertEntrance)
        val vtfDrinksShift1 = ShiftEntity(startTime = LocalTime.parse("09:30"), endTime = LocalTime.parse("14:00"), production = vtfDrinks)
        val vtfDrinksShift2 = ShiftEntity(startTime = LocalTime.parse("14:00"), endTime = LocalTime.parse("18:30"), production = vtfDrinks)
        val vtfDrinksShift3 = ShiftEntity(startTime = LocalTime.parse("18:30"), endTime = LocalTime.parse("23:45"), production = vtfDrinks)
        val vtfFriesShift1 = ShiftEntity(startTime = LocalTime.parse("09:30"), endTime = LocalTime.parse("14:00"), production = vtfFries)
        val vtfFriesShift2 = ShiftEntity(startTime = LocalTime.parse("14:00"), endTime = LocalTime.parse("18:30"), production = vtfFries)
        val vtfFriesShift3 = ShiftEntity(startTime = LocalTime.parse("18:30"), endTime = LocalTime.parse("23:45"), production = vtfFries)
        shiftRepository.saveAll(
            listOf(
                konzertEntranceShift, vtfDrinksShift1, vtfDrinksShift2, vtfDrinksShift3, vtfFriesShift1,
                vtfFriesShift2, vtfFriesShift3
            )
        )

        val felix = UserEntity(email = "felix.honer@novatec-gmbh.de", password = passwordEncoder.encode("felix"), role = UserRole.READER)
        userRepository.save(felix)
    }

    protected fun clearData() {
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
