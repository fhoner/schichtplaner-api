package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.expediagroup.graphql.annotations.GraphQLName
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.context.annotation.Scope

@Scope("prototype")
@GraphQLName("Plan")
data class PlanDto(
	@GraphQLIgnore val id: Long,
	val uuid: String,
	val name: String
) {
	@JsonIgnore
	lateinit var productions: List<ProductionDto>
}
