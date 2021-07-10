package com.felixhoner.schichtplaner.api.business.model

import java.util.UUID

data class Plan(
    val id: Long,
    val uuid: UUID,
    val name: String
)
