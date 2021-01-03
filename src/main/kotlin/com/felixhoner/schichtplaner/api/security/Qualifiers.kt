package com.felixhoner.schichtplaner.api.security

import com.felixhoner.schichtplaner.api.graphql.directive.Authorized

@Authorized("READER", "WRITER", "ADMIN")
annotation class Reader

@Authorized("WRITER", "ADMIN")
annotation class Writer

@Authorized("ADMIN")
annotation class Admin
