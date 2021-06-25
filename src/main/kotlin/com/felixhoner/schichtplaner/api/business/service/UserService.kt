package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.exception.InvalidCredentialsException
import com.felixhoner.schichtplaner.api.business.exception.InvalidRefreshTargetException
import com.felixhoner.schichtplaner.api.business.exception.InvalidTokenException
import com.felixhoner.schichtplaner.api.business.exception.NotFoundException
import com.felixhoner.schichtplaner.api.business.model.User
import com.felixhoner.schichtplaner.api.graphql.dto.UserRoleDto
import com.felixhoner.schichtplaner.api.persistence.entity.UserEntity
import com.felixhoner.schichtplaner.api.persistence.repository.UserRepository
import com.felixhoner.schichtplaner.api.security.JwtSigner
import com.felixhoner.schichtplaner.api.security.SchichtplanerUser
import io.jsonwebtoken.security.InvalidKeyException
import mu.KotlinLogging
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

enum class TokenType {
    ACCESS,
    REFRESH
}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtSigner: JwtSigner,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val transformer: TransformerBo
) {

    private val logger = KotlinLogging.logger {}

    fun login(username: String, password: String): Mono<Pair<String, String>> {
        val dbresult = userRepository.findByEmail(username)
        if (dbresult == null || !passwordEncoder.matches(password, dbresult.password)) {
            return Mono.error(InvalidCredentialsException("Username and password do not match"))
        }

        val user = SchichtplanerUser(dbresult)
        val authorities = user.authorities.map { auth -> auth.authority }
        return Mono.just(
            Pair(
                jwtSigner.createAccessToken(user.username, authorities),
                jwtSigner.createRefreshToken(user.username, authorities)
            )
        )
    }

    fun getUserByEmail(email: String): Mono<User> {
        return userRepository.findByEmail(email)
            ?.let(transformer::toBo)
            ?.let { Mono.just(it) } ?: Mono.error(NotFoundException("User with email $email not found"))
    }

    @Suppress("UNCHECKED_CAST")
    fun refreshToken(refreshToken: String, type: TokenType): Mono<String> = try {
        val jws = jwtSigner.validateJwt(refreshToken)
        if (jws.body["type"].toString() != "refresh") {
            throw InvalidRefreshTargetException("Expected token to be of type refresh but wasn't")
        } else {
            when (type) {
                TokenType.ACCESS -> jwtSigner.createAccessToken(jws.body.subject, jws.body["roles"] as List<String>)
                TokenType.REFRESH -> jwtSigner.createRefreshToken(jws.body.subject, jws.body["roles"] as List<String>)
            }.let { Mono.just(it) }
        }
    } catch (ex: InvalidRefreshTargetException) {
        logger.info { ex }
        Mono.error(InvalidTokenException())
    } catch (ex: InvalidKeyException) {
        logger.info { ex }
        Mono.error(InvalidTokenException())
    }

    fun createUser(username: String, password: String, role: UserRoleDto): Mono<User> =
        UserEntity(
            email = username,
            password = passwordEncoder.encode(password),
            role = transformer.toBo(role)
        )
            .let { userRepository.save(it) }
            .let { transformer.toBo(it) }
            .let { Mono.just(it) }

}
