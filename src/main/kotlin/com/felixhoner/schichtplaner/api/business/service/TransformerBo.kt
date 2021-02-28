package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.Plan
import com.felixhoner.schichtplaner.api.business.model.Production
import com.felixhoner.schichtplaner.api.business.model.Shift
import com.felixhoner.schichtplaner.api.business.model.User
import com.felixhoner.schichtplaner.api.business.model.Worker
import com.felixhoner.schichtplaner.api.graphql.dto.UserRoleDto
import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserRole
import com.felixhoner.schichtplaner.api.persistence.entity.WorkerEntity
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

    fun toBo(user: UserEntity) = User(
        id = user.id!!,
        uuid = user.uuid,
        email = user.email,
        password = user.password,
        role = user.role
    )

    fun toBo(role: UserRoleDto) = when (role) {
        UserRoleDto.READER -> UserRole.READER
        UserRoleDto.WRITER -> UserRole.WRITER
        UserRoleDto.ADMIN -> UserRole.ADMIN
    }

}
