package com.felixhoner.schichtplaner.api.graphql.directive

import com.expediagroup.graphql.annotations.GraphQLDirective

@GraphQLDirective(name = "authorized", description = "The given roles are required")
annotation class Authorized(vararg val roles: String)
