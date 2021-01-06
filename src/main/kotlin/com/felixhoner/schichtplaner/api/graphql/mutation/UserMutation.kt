package com.felixhoner.schichtplaner.api.graphql.mutation

import com.expediagroup.graphql.spring.operations.Mutation
import com.felixhoner.schichtplaner.api.security.JwtSigner
import com.felixhoner.schichtplaner.api.security.UserService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

data class LoginResponse(
	val token: String
)

@Component
@Suppress("unused")
class UserMutation(
	private val userService: UserService,
	private val jwtSigner: JwtSigner
): Mutation {

	fun login(email: String, password: String): Mono<LoginResponse> {
		return userService.findByUsername(email)
			.map {
				val authorities = it.authorities.map { auth -> auth.authority }
				val jwt = jwtSigner.createJwt(it.username, authorities)
				LoginResponse(jwt)
			}
	}

}
