package com.felixhoner.schichtplaner.api.security

import com.felixhoner.schichtplaner.api.persistence.entity.UserEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserRole.READER
import com.felixhoner.schichtplaner.api.persistence.entity.UserRole.WRITER
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService {

	fun findByUsername(username: String): Mono<SchichtplanerUser> {
		return Mono.just(
			SchichtplanerUser(
				UserEntity(
					email = "felix@honer.de",
					password = "password",
					roles = setOf(READER, WRITER)
				)
			)
		)
	}
}
