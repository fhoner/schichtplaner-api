package com.felixhoner.schichtplaner.api.graphql.dto

import com.felixhoner.schichtplaner.api.business.model.*
import org.springframework.stereotype.Component

@Component
class TransformerDto {

	fun toDto(plan: Plan) = PlanDto(
		id = plan.id,
		uuid = plan.uuid.toString(),
		name = plan.name
	)

	fun toDto(production: Production) = ProductionDto(
		id = production.id,
		uuid = production.uuid.toString(),
		name = production.name
	)

	fun toDto(shift: Shift) = ShiftDto(
		id = shift.id,
		uuid = shift.uuid.toString(),
		startTime = shift.startTime.toString(),
		endTime = shift.endTime.toString()
	)

}
