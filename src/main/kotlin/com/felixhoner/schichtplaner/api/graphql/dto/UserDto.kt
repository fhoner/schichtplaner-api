package com.felixhoner.schichtplaner.api.graphql.dto

data class UserDto(
    val uuid: String,
    val email: String,
    val roles: List<String>
)
