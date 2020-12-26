package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.expediagroup.graphql.annotations.GraphQLName
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
data class ProductionDto @Autowired(required = false) constructor(
	@GraphQLIgnore val id: Long,
	val uuid: String,
	val name: String
) {

	@JsonIgnore
	lateinit var shifts: List<ShiftDto>

}
