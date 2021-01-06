package com.felixhoner.schichtplaner.api.graphql.dto

data class UserDto(
	val uuid: String,
	val email: String,
	val role: UserRoleDto
)
