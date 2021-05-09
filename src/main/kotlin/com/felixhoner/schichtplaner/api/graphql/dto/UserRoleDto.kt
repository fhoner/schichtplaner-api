package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.generator.annotations.GraphQLName

@GraphQLName("UserRole")
enum class UserRoleDto {
    READER,
    WRITER,
    ADMIN
}
