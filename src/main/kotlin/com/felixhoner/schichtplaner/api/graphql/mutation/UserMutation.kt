package com.felixhoner.schichtplaner.api.graphql.mutation

import com.expediagroup.graphql.spring.operations.Mutation
import com.felixhoner.schichtplaner.api.business.service.UserService
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import com.felixhoner.schichtplaner.api.graphql.dto.UserDto
import com.felixhoner.schichtplaner.api.graphql.dto.UserRoleDto
import com.felixhoner.schichtplaner.api.security.JwtSigner
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

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
) : Mutation {

    @Authorized("WRITER")
    fun createUser(email: String, password: String, role: UserRoleDto): CompletableFuture<UserDto> {
        return userService.createUser(email, password, role)
            .map { transformer.toDto(it) }
            .toFuture()
    }

}
