package com.felixhoner.schichtplaner.api.graphql.dto

import com.felixhoner.schichtplaner.api.business.model.*
import org.springframework.stereotype.Component

@Component
class TransformerDto {

	fun toDto(plan: Plan) = PlanDto(
		id = plan.id,
		uuid = plan.uuid.toString(),
		name = plan.name,
		secret = "secret"
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

	fun toDto(worker: Worker) = WorkerDto(
		id = worker.id,
		uuid = worker.uuid.toString(),
		firstname = worker.firstname,
		lastname = worker.lastname,
		email = worker.email
	)

}
