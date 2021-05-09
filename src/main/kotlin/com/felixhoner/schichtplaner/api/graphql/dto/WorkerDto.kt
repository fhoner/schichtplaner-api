package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized

data class WorkerDto(
    @GraphQLIgnore val id: Long,
    val uuid: String,
    val firstname: String,
    val lastname: String,

    @property:Authorized("WRITER")
    val email: String
)
