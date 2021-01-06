package com.felixhoner.schichtplaner.api.graphql.mutation

import com.expediagroup.graphql.spring.operations.Mutation
import com.felixhoner.schichtplaner.api.business.service.UserService
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import com.felixhoner.schichtplaner.api.graphql.dto.*
import com.felixhoner.schichtplaner.api.security.JwtSigner
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

data class LoginResponse(
	val token: String
)

@Component
@Suppress("unused")
class UserMutation(
	private val userService: UserService,
	private val jwtSigner: JwtSigner,
	private val transformer: TransformerDto
): Mutation {

	fun login(email: String, password: String): Mono<LoginResponse> {
		return userService.login(email, password).map { LoginResponse(it) }
	}

	@Authorized("WRITER")
	fun createUser(email: String, password: String, role: UserRoleDto): Mono<UserDto> {
		return userService.createUser(email, password, role)
			.map { transformer.toDto(it) }
	}

}
