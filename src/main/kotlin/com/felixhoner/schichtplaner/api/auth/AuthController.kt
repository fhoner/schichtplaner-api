package com.felixhoner.schichtplaner.api.auth

import com.felixhoner.schichtplaner.api.business.service.TokenType
import com.felixhoner.schichtplaner.api.business.service.TokenType.ACCESS
import com.felixhoner.schichtplaner.api.business.service.TokenType.REFRESH
import com.felixhoner.schichtplaner.api.business.service.UserService
import com.felixhoner.schichtplaner.api.graphql.mutation.LoginResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * REST controller to issue httpOnly cookies. This should be used by webclients
 * so that refresh and access tokens are not leaked by potential XSS vulnerabilities.
 * The cookies do not have a max-age but the tokens itself have, as they both are jwt
 * which are validated server side every time.
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
) {

    /** Creates both refresh and access tokens and responds set-cookie headers properly. */
    @PostMapping("/login")
    fun createAccessAndRefreshToken(@RequestBody request: LoginRequest, response: ServerHttpResponse): Mono<Void> {
        return userService.login(request.email, request.password)
            .map {
                LoginResponse(
                    accessToken = it.first,
                    refreshToken = it.second
                )
            }
            .doOnNext {
                response.apply {
                    addCookie(createCookie("access_token", it.accessToken, "/api/graphql"))
                    addCookie(createCookie("refresh_token", it.refreshToken, "/api/auth"))
                    response.statusCode = HttpStatus.CREATED
                }
            }
            .flatMap { Mono.empty<Void>() }
            .onErrorResume {
                response.statusCode = HttpStatus.UNAUTHORIZED
                Mono.empty()
            }
    }

    /** To remove the cookies on the client, the cookies are set to blanks with max-age to expire immediately. */
    @PostMapping("/logout")
    fun logout(response: ServerHttpResponse): Mono<Void> {
        response.addCookie(
            createCookie(
                name = "access_token",
                value = "",
                path = "/api/graphql",
                maxAge = 0
            )
        )
        response.addCookie(
            createCookie(
                name = "refresh_token",
                value = "",
                path = "/api/auth",
                maxAge = 0
            )
        )
        return Mono.empty()
    }

    /**
     * Used to get a new access or refresh token. For security reasons, the access
     * token expires rather soon, so call the endpoint to retrieve a new one using
     * a still valid refresh token.
     */
    @PostMapping("/refresh")
    fun refreshToken(
        @CookieValue(name = "refresh_token") refreshToken: String,
        @RequestParam target: TokenType,
        response: ServerHttpResponse
    ): Mono<Void> {
        return userService.refreshToken(refreshToken, target)
            .doOnNext {
                when (target) {
                    ACCESS -> response.addCookie(createCookie("access_token", it, "/api/graphql"))
                    REFRESH -> response.addCookie(createCookie("refresh_token", it, "/api/auth"))
                }
                response.statusCode = HttpStatus.CREATED
            }
            .flatMap { Mono.empty<Void>() }
            .onErrorResume {
                response.statusCode = HttpStatus.UNAUTHORIZED
                Mono.empty()
            }
    }

    private fun createCookie(name: String, value: String, path: String, maxAge: Long = -1) = ResponseCookie
        .from(name, value)
        .path(path)
        .maxAge(maxAge)
        .sameSite("Strict")
        .httpOnly(true)
        .build()

}
