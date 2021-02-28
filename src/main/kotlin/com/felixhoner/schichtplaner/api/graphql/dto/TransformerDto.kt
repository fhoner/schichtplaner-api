package com.felixhoner.schichtplaner.api.graphql.dto

import com.felixhoner.schichtplaner.api.business.model.Plan
import com.felixhoner.schichtplaner.api.business.model.Production
import com.felixhoner.schichtplaner.api.business.model.Shift
import com.felixhoner.schichtplaner.api.business.model.User
import com.felixhoner.schichtplaner.api.business.model.Worker
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
        roles = user.role.get().toList()
    )

}
