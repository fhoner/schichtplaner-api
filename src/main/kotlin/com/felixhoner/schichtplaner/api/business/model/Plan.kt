package com.felixhoner.schichtplaner.api.business.model

import java.util.*

data class Plan(
    val id: Long,
    val uuid: UUID,
    val name: String
)
