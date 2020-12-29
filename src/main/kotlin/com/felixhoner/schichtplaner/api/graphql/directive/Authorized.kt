package com.felixhoner.schichtplaner.api.graphql.directive

import com.expediagroup.graphql.annotations.GraphQLDirective
import graphql.introspection.Introspection.DirectiveLocation.FIELD_DEFINITION

@GraphQLDirective(
	name = "authorized", description = "The given roles are required", locations = [FIELD_DEFINITION]
)
annotation class Authorized(vararg val roles: String)
