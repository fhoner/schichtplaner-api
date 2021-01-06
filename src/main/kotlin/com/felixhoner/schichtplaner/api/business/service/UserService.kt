package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.exception.InvalidCredentialsException
import com.felixhoner.schichtplaner.api.business.model.User
import com.felixhoner.schichtplaner.api.graphql.dto.UserRoleDto
import com.felixhoner.schichtplaner.api.persistence.entity.UserEntity
import com.felixhoner.schichtplaner.api.persistence.repository.UserRepository
import com.felixhoner.schichtplaner.api.security.JwtSigner
import com.felixhoner.schichtplaner.api.security.SchichtplanerUser
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(
	private val userRepository: UserRepository,
	private val jwtSigner: JwtSigner,
	private val passwordEncoder: BCryptPasswordEncoder,
	private val transformer: TransformerBo
) {
	fun login(username: String, password: String): Mono<String> {
		val dbresult = userRepository.findByEmail(username)
		if (dbresult == null || !passwordEncoder.matches(password, dbresult.password)) {
			throw InvalidCredentialsException("Username and password do not match")
		}

		val user = SchichtplanerUser(dbresult)
		val authorities = user.authorities.map { auth -> auth.authority }
		val jwt = jwtSigner.createJwt(user.username, authorities)
		return Mono.just(jwt)
	}

	fun createUser(username: String, password: String, role: UserRoleDto): Mono<User> {
		val userEntity = UserEntity(
			email = username,
			password = passwordEncoder.encode(password),
			role = transformer.toBo(role)
		)
		userRepository.save(userEntity)
		return Mono.just(userEntity.let(transformer::toBo))
	}
}
