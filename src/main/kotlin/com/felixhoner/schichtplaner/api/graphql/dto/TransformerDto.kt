package com.felixhoner.schichtplaner.api.graphql.dto

import com.felixhoner.schichtplaner.api.business.model.*
import com.felixhoner.schichtplaner.api.persistence.entity.UserRole
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

	fun toDto(worker: Worker) = WorkerDto(
		id = worker.id,
		uuid = worker.uuid.toString(),
		firstname = worker.firstname,
		lastname = worker.lastname,
		email = worker.email
	)

	fun toDto(user: User) = UserDto(
		uuid = user.uuid.toString(),
		email = user.email,
		role = toDto(user.role)
	)

	fun toDto(role: UserRole) = when (role) {
		UserRole.READER -> UserRoleDto.READER
		UserRole.WRITER -> UserRoleDto.WRITER
		UserRole.ADMIN  -> UserRoleDto.ADMIN
	}

}
