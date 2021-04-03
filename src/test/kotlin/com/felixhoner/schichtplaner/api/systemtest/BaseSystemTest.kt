package com.felixhoner.schichtplaner.api.systemtest

import com.felixhoner.schichtplaner.api.auth.LoginRequest
import com.felixhoner.schichtplaner.api.security.SecurityConfiguration
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono


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

}
