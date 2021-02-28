package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonIgnore

data class ShiftDto(
    @GraphQLIgnore val id: Long,
    val uuid: String,
    val startTime: String,
    val endTime: String
) {

    @JsonIgnore
    lateinit var workers: List<WorkerDto>

}
