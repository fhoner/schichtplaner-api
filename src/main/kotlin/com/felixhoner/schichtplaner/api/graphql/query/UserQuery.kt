package com.felixhoner.schichtplaner.api.graphql.query

import com.expediagroup.graphql.spring.operations.Query
import com.felixhoner.schichtplaner.api.business.service.UserService
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import com.felixhoner.schichtplaner.api.graphql.dto.UserDto
import com.felixhoner.schichtplaner.api.graphql.execution.GraphQLSecurityContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Scope("prototype")
@Suppress("unused")
class UserQuery(
	private val userService: UserService,
	private val transformer: TransformerDto
): Query {

	fun getMyProfile(context: GraphQLSecurityContext): Mono<UserDto> =
		context.securityContext
			.flatMap { userService.getUserByEmail(it.authentication.principal.toString()) }
			.map { transformer.toDto(it) }
}
