package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.*
import com.felixhoner.schichtplaner.api.persistence.entity.*
import org.springframework.stereotype.Component

@Component
class TransformerBo {

	fun toBo(plan: PlanEntity) = Plan(
		id = plan.id!!,
		uuid = plan.uuid,
		name = plan.name
	)

	fun toBo(production: ProductionEntity) = Production(
		id = production.id!!,
		uuid = production.uuid,
		name = production.name
	)

	fun toBo(shift: ShiftEntity) = Shift(
		id = shift.id!!,
		uuid = shift.uuid,
		startTime = shift.startTime,
		endTime = shift.endTime
	)

	fun toBo(worker: WorkerEntity) = Worker(
		id = worker.id!!,
		uuid = worker.uuid,
		firstname = worker.firstname,
		lastname = worker.lastname,
		email = worker.email,
		shiftIds = worker.shifts.map { it.id!! }
	)

}
