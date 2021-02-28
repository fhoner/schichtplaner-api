package com.felixhoner.schichtplaner.api.business.model

import java.util.*

data class Worker(
    val id: Long,
    val uuid: UUID,
    val firstname: String,
    val lastname: String,
    val email: String,
    val shiftIds: List<Long>
)
