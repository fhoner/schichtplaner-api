package com.felixhoner.schichtplaner.api.graphql.mutation

import com.expediagroup.graphql.spring.operations.Mutation
import com.felixhoner.schichtplaner.api.business.service.ShiftService
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import com.felixhoner.schichtplaner.api.graphql.dto.ShiftDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import org.springframework.stereotype.Component
import java.util.*

@Component
@Suppress("unused")
class ShiftMutation(
    private val shiftService: ShiftService,
    private val transformerDto: TransformerDto
) : Mutation {

    @Authorized("ADMIN")
    fun createShift(productionUuid: String, startTime: String, endTime: String): ShiftDto =
        shiftService.createShift(UUID.fromString(productionUuid), startTime, endTime)
            .let(transformerDto::toDto)

}
