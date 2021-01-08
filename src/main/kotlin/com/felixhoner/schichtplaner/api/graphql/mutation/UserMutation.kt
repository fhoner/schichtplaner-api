package com.felixhoner.schichtplaner.api.graphql.mutation

import com.expediagroup.graphql.spring.operations.Mutation
import com.felixhoner.schichtplaner.api.business.exception.*
import com.felixhoner.schichtplaner.api.business.service.UserService
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import com.felixhoner.schichtplaner.api.graphql.dto.*
import com.felixhoner.schichtplaner.api.graphql.errorhandling.GraphQLException
import com.felixhoner.schichtplaner.api.security.JwtSigner
import org.springframework.stereotype.Component

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

	fun login(email: String, password: String): LoginResponse = try {
		LoginResponse(userService.login(email, password))
	} catch (ex: InvalidCredentialsException) {
		throw GraphQLException(ErrorCodes.LOGIN_FAILED, ex)
	}

	@Authorized("WRITER")
	fun createUser(email: String, password: String, role: UserRoleDto): UserDto {
		return userService.createUser(email, password, role)
			.let { transformer.toDto(it) }
	}

}
