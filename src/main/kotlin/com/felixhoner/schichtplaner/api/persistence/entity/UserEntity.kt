package com.felixhoner.schichtplaner.api.persistence.entity

import java.util.*
import javax.persistence.*
import javax.validation.constraints.Email

enum class UserRole {
	READER,
	WRITER,
	ADMIN
}

data class UserEntity(
	@Email
	val email: String,
	val password: String,
	val roles: Set<UserRole>
) {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null

	@Column(unique = true)
	val uuid: UUID = UUID.randomUUID()
}
