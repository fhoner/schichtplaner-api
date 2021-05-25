package com.felixhoner.schichtplaner.api.business.model

import java.time.Instant
import java.util.*

data class Shift(
    val id: Long,
    val uuid: UUID,
    val startTime: Instant,
    val endTime: Instant,
)
