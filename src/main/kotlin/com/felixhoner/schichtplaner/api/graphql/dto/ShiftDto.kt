package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore

data class ShiftDto(
	@GraphQLIgnore val id: Long,
	val uuid: String,
	val startTime: String,
	val endTime: String
)
