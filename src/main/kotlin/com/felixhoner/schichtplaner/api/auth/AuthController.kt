package com.felixhoner.schichtplaner.api.auth

import com.felixhoner.schichtplaner.api.business.service.TokenType
import com.felixhoner.schichtplaner.api.business.service.TokenType.ACCESS
import com.felixhoner.schichtplaner.api.business.service.TokenType.REFRESH
import com.felixhoner.schichtplaner.api.business.service.UserService
import com.felixhoner.schichtplaner.api.graphql.mutation.LoginResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

data class LoginRequest(
	val email: String,
	val password: String
)

data class TokenRefreshRequest(
	val token: String
)

@RestController
@RequestMapping("/auth")
class AuthController(
	private val userService: UserService
) {

	@PostMapping("/login")
	fun createToken(@RequestBody request: LoginRequest, response: ServerHttpResponse): Mono<Void> {
		return userService.login(request.email, request.password)
			.map {
				LoginResponse(
					accessToken = it.first,
					refreshToken = it.second
				)
			}
			.doOnNext {
				response.apply {
					addCookie(createCookie("access_token", it.accessToken, "/graphql"))
					addCookie(createCookie("refresh_token", it.refreshToken, "/auth/refresh"))
					response.statusCode = HttpStatus.CREATED
				}
			}
			.flatMap { Mono.empty<Void>() }
			.onErrorResume {
				response.statusCode = HttpStatus.UNAUTHORIZED
				Mono.empty()
			}
	}

	@PostMapping("/refresh")
	fun refreshToken(
		@RequestBody request: TokenRefreshRequest,
		@RequestParam target: TokenType,
		response: ServerHttpResponse
	): Mono<Void> {
		return userService.refreshToken(request.token, target)
			.doOnNext {
				when (target) {
					ACCESS  -> response.addCookie(createCookie("access_token", it, "/graphql"))
					REFRESH -> response.addCookie(createCookie("refresh_token", it, "/auth/refresh"))
				}
				response.statusCode = HttpStatus.CREATED
			}
			.flatMap { Mono.empty<Void>() }
			.onErrorResume {
				response.statusCode = HttpStatus.UNAUTHORIZED
				Mono.empty()
			}
	}

	private fun createCookie(name: String, value: String, path: String) = ResponseCookie
		.from(name, value)
		.path(path)
		.sameSite("Strict")
		.secure(true)
		.httpOnly(true)
		.build()

}
