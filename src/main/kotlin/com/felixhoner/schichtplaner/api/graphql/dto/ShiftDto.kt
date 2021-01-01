package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonIgnore
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized

data class ShiftDto(
	@GraphQLIgnore val id: Long,
	val uuid: String,
	val startTime: String,
	val endTime: String
) {

	@JsonIgnore
	@Authorized("MANAGER")
	lateinit var workers: List<WorkerDto>

}
