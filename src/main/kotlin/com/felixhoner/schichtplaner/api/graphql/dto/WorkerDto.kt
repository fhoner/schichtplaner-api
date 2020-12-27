package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore

data class WorkerDto(
	@GraphQLIgnore val id: Long,
	val uuid: String,
	val firstname: String,
	val lastname: String,
	val email: String
)
