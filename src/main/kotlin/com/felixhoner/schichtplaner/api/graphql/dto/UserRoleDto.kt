package com.felixhoner.schichtplaner.api.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLName

@GraphQLName("UserRole")
enum class UserRoleDto {
	READER,
	WRITER,
	ADMIN
}
