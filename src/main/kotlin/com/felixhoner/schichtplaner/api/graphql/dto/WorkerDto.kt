package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.felixhoner.schichtplaner.api.security.Writer

data class WorkerDto(
	@GraphQLIgnore val id: Long,
	val uuid: String,
	val firstname: String,
	val lastname: String,

	@property:Writer
	val email: String
)
