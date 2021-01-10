package com.felixhoner.schichtplaner.api.graphql.mutation

import com.expediagroup.graphql.spring.operations.Mutation
import com.felixhoner.schichtplaner.api.business.service.UserService
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import com.felixhoner.schichtplaner.api.graphql.dto.*
import com.felixhoner.schichtplaner.api.security.JwtSigner
import org.springframework.stereotype.Component

data class LoginResponse(
	val accessToken: String,
	val refreshToken: String
)

@Component
@Suppress("unused")
class UserMutation(
	private val userService: UserService,
	private val jwtSigner: JwtSigner,
	private val transformer: TransformerDto
): Mutation {

	@Authorized("WRITER")
	fun createUser(email: String, password: String, role: UserRoleDto): UserDto {
		return userService.createUser(email, password, role)
			.let { transformer.toDto(it) }
	}

}
