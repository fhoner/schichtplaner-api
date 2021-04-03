package com.felixhoner.schichtplaner.api.auth

import com.felixhoner.schichtplaner.api.SecurityTestConfiguration
import com.felixhoner.schichtplaner.api.business.exception.InvalidCredentialsException
import com.felixhoner.schichtplaner.api.business.service.TokenType
import com.felixhoner.schichtplaner.api.business.service.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.Duration

@ExtendWith(SpringExtension::class)
@WebFluxTest(AuthController::class)
@Import(SecurityTestConfiguration::class)
@ActiveProfiles("integration-test")
class AuthControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var userService: UserService

    @Nested
    inner class Login {

        @Test
        fun `should create tokens on valid credentials`() {
            val loginRequest = LoginRequest(email = "max@mustermann", password = "pw")
            every { userService.login(any(), any()) } returns Mono.just(Pair("123", "789"))

            webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(loginRequest), LoginRequest::class.java)
                .exchange()
                .expectCookie().valueEquals("access_token", "123")
                .expectCookie().path("access_token", "/api/graphql")
                .expectCookie().httpOnly("access_token", true)
                .expectCookie().sameSite("access_token", "Strict")
                .expectCookie().valueEquals("refresh_token", "789")
                .expectCookie().path("refresh_token", "/api/auth")
                .expectCookie().httpOnly("refresh_token", true)
                .expectCookie().sameSite("refresh_token", "Strict")
                .expectBody().isEmpty
                .status.is2xxSuccessful
            verify { userService.login("max@mustermann", "pw") }
        }

        @Test
        fun `should not create tokens on invalid credentials`() {
            val loginRequest = LoginRequest(email = "max@mustermann", password = "pw")
            every { userService.login(any(), any()) } returns Mono.error(InvalidCredentialsException(""))

            webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(loginRequest), LoginRequest::class.java)
                .exchange()
                .expectCookie().doesNotExist("access_token")
                .expectCookie().doesNotExist("refresh_token")
                .expectBody().isEmpty
                .status.is4xxClientError
            verify { userService.login("max@mustermann", "pw") }
        }

    }

    @Test
    fun `should remove tokens on logout`() {
        webTestClient.post()
            .uri("/auth/logout")
            .exchange()
            .expectCookie().valueEquals("access_token", "")
            .expectCookie().path("access_token", "/api/graphql")
            .expectCookie().httpOnly("access_token", true)
            .expectCookie().sameSite("access_token", "Strict")
            .expectCookie().maxAge("access_token", Duration.ofSeconds(0))
            .expectCookie().valueEquals("refresh_token", "")
            .expectCookie().path("refresh_token", "/api/auth")
            .expectCookie().httpOnly("refresh_token", true)
            .expectCookie().sameSite("refresh_token", "Strict")
            .expectCookie().maxAge("refresh_token", Duration.ofSeconds(0))
            .expectStatus().is2xxSuccessful
    }

    @Nested
    inner class RefreshToken {

        @Test
        fun `should refresh accessToken successfully`() {
            every { userService.refreshToken(any(), any()) } returns Mono.just("newaccesstoken")

            webTestClient.post()
                .uri {
                    it.path("/auth/refresh")
                        .queryParam("target", TokenType.ACCESS)
                        .build()
                }
                .cookie("refresh_token", "refreshtoken")
                .exchange()
                .expectCookie().valueEquals("access_token", "newaccesstoken")
                .expectCookie().path("access_token", "/api/graphql")
                .expectCookie().httpOnly("access_token", true)
                .expectCookie().sameSite("access_token", "Strict")
                .expectCookie().doesNotExist("refresh_token")
                .expectBody().isEmpty
                .status.is2xxSuccessful
            verify { userService.refreshToken("refreshtoken", TokenType.ACCESS) }
            verify(exactly = 0) { userService.refreshToken(any(), TokenType.REFRESH) }
        }

        @Test
        fun `should refresh refreshToken successfully`() {
            every { userService.refreshToken(any(), any()) } returns Mono.just("newrefreshtoken")

            webTestClient.post()
                .uri {
                    it.path("/auth/refresh")
                        .queryParam("target", TokenType.REFRESH)
                        .build()
                }
                .cookie("refresh_token", "oldrefreshtoken")
                .exchange()
                .expectCookie().valueEquals("refresh_token", "newrefreshtoken")
                .expectCookie().path("refresh_token", "/api/auth")
                .expectCookie().httpOnly("refresh_token", true)
                .expectCookie().sameSite("refresh_token", "Strict")
                .expectCookie().doesNotExist("access_token")
                .expectBody().isEmpty
                .status.is2xxSuccessful
            verify { userService.refreshToken("oldrefreshtoken", TokenType.REFRESH) }
            verify(exactly = 0) { userService.refreshToken(any(), TokenType.ACCESS) }
        }

    }

}
